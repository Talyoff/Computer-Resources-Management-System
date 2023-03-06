package bgu.spl.mics;

import bgu.spl.mics.application.messages.broadcast.PublishTerminateBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.services.ConferenceService;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    private static MessageBusImpl instance = null;
    private final Map<MicroService, LinkedBlockingDeque<Message>> busQueue;
    private final Map<Class<? extends Message>, Vector<MicroService>> subscriptions;
    private final Map<Event<?>, Future<?>> futureMap;
    private int subscriptionsSize;
    final private Object registration_lock;
    final private Object subscription_lock;

    public MessageBusImpl() { //TODO remember constructor is public
        instance = this;
        busQueue = new HashMap<>();
        subscriptions = new HashMap<>();
        futureMap = new ConcurrentHashMap<>();
        registration_lock = new Object();
        subscription_lock = new Object();
        subscriptionsSize = 0;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (m == null)
            throw new NullPointerException();
        if(busQueue.get(m) == null)
            throw new IllegalArgumentException("The microservice doesn't have a queue");
        synchronized (subscription_lock) {
            if (!subscriptions.containsKey(type)) {
                subscriptions.put(type, new Vector<>());
            }
            subscriptions.get(type).add(m);
            subscriptionsSize++;
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (m == null)
            throw new NullPointerException();
        if(busQueue.get(m) == null)
            throw new IllegalArgumentException("The microservice doesn't have a queue");
        synchronized (subscription_lock){
        if (!subscriptions.containsKey(type)) {
            subscriptions.put(type, new Vector<>());
        }
        subscriptions.get(type).add(m);
        subscriptionsSize++;
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) futureMap.get(e);
        synchronized (future) {
            future.resolve(result);
            future.notifyAll();
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        if (b == null)
            throw new NullPointerException();
//        for (MicroService m : subscriptions.get(b.getClass())) {
        subscriptions.get(b.getClass()).forEach((m) -> {
            if(b.getClass() == PublishTerminateBroadcast.class)
                busQueue.get(m).addFirst(b);
            else
                busQueue.get(m).add(b);
            synchronized (busQueue.get(m)) {
                busQueue.get(m).notifyAll();
            }
        });
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        if (e == null)
            throw new NullPointerException();
        MicroService m;
        synchronized (subscription_lock) {
            m = subscriptions.get(e.getClass()).remove(0);
            subscriptions.get(e.getClass()).add(m);
        }
        busQueue.get(m).add(e);
        Future<T> future = new Future<>();
        synchronized (busQueue.get(m)) {
            futureMap.put(e, future);
            busQueue.get(m).notifyAll();
        }
        return future;
    }

    @Override
    public void register(MicroService m) {
        synchronized (registration_lock) {
            if (m == null)
                throw new NullPointerException();
            busQueue.put(m,new LinkedBlockingDeque<>());
//            System.out.println("micro service named - " + m.getName() + " subscribed");
        }

    }

    @Override
    public void unregister(MicroService m) {
        if(m == null)
            throw new NullPointerException("Microservice is null");
        if(!isMicroServiceRegistered(m))
            return;
        // remove m from subscriptions
        for (Map.Entry<Class<? extends Message>, Vector<MicroService>> entry : subscriptions.entrySet()) {
            entry.getValue().remove(m);
        }
        if(m.getClass() == ConferenceService.class){
            for(Message msg : busQueue.get(m)){
                if(msg.getClass() == PublishResultsEvent.class)
                    sendEvent((Event<?>) msg);
            }
        }
        // remove m from busQueue
        busQueue.remove(m);
         //TODO remove the microservice from future map?
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if (!isMicroServiceRegistered(m)) {
            System.out.println(m.getName());
            throw new IllegalArgumentException("This micro service is not subscribed");
        }
        LinkedBlockingDeque<Message> queue = busQueue.get(m);
        try {
            while (queue.peek() == null) {
                synchronized (queue) {
                    busQueue.get(m).wait();
                }
            }
            return queue.poll();
        } catch (InterruptedException exception) {
            throw new InterruptedException("interrupted while waiting for message");
        }
    }

    public static MessageBusImpl getInstance() {
        if (instance == null) {
            synchronized (MessageBusImpl.class) {
                if (instance == null) {
                    instance = new MessageBusImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean isMicroServiceSubscribed(Class<? extends Message> type, MicroService m) {
        if(subscriptions.get(type) == null)
            return false;
        return subscriptions.get(type).contains(m);
    }

    @Override
    public boolean isBroadcastAdded(Message msg, MicroService m) {
        return busQueue.get(m).contains(msg);
    }

    @Override
    public <T> boolean isEventAdded(Message msg, MicroService m) {
        return busQueue.get(m).contains(msg);
    }

    @Override
    public boolean isMicroServiceRegistered(MicroService m) {
        return busQueue.containsKey(m);
    }

    @Override
    public boolean isMicroServiceUnRegistered(MicroService m) {
        boolean isUnregistered = true;
        if (busQueue.containsKey(m))
            isUnregistered = false;
        else {
            for (Class<? extends Message> msg : subscriptions.keySet()) {
                if (subscriptions.get(msg).contains(m)) {
                    isUnregistered = false;
                    break;
                }
            }
        }
        return isUnregistered;
    }

    public int getSubscriptionsSize(){
        return subscriptionsSize;
    }
}
