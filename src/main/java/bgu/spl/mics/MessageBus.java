package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 */
public interface MessageBus {

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     * @pre m isn't null
     * @pre m must have a queue in MessageBus
     * //Events is a general data structure that contains the events and theirs subscribers
     * @post For each i < Subscriptions.at(type).size(), Subscriptions.at(type).at(i) = @pre(Subscriptions.at(type).at(i))
     * @post Subscriptions.at(type).last = m
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     * @pre m isn't null
     * @pre m must have a queue in MessageBus
     * //Broadcasts is a general data structure that contains the broadcasts and theirs subscribers
     * @post For each i < Subscriptions.at(type).size(), Subscriptions.at(type).at(i) = @pre(Subscriptions.at(type).at(i))
     * @post Subscriptions.at(type).last = m
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @pre result isn't null
     * @pre the Future object associated with Event e isn't resolved
     * @post the Future object associated with Event e is resolved
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b 	The message to added to the queues.
     * //Subscriptions is a general data structure that contains the Broadcasts type and their subscribers.
     * //MsgBusQue is a general queue (data structure) that contains the queue's for each MicroService
     * @pre b isn't null
     * @post for every MicroService (m) in Subscriptions.at(b), MsgBusQue.at(m).last = b
     * @post for every MicroService (m) in Subscriptions.at(b), for i < MsgBusQue.at(m).size(), MsgBusQue.at(m).at(i) = @pre(MsgBusQue.at(m).at(i))
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     * //Events is a general data structure that contains the Events type and their subscribers.
     * //MsgBusQue is a general queue (data structure) that contains the queue's for each MicroService
     * @pre b isn't null
     * @post for every MicroService (m) in Subscriptions.at(b), MsgBusQue.at(m).last = b
     * @post for every MicroService (m) in Subscriptions.at(b), for i < MsgBusQue.at(m).size(), MsgBusQue.at(m).at(i) = @pre(MsgBusQue.at(m).at(i))
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     * // MsgBusQue is the general data structure for the MessageBus MicroServices queue's
     * @pre  m isn't null
     * @post MsgBusQue.size() = @pre(MsgBugQue.size()) + 1
     * @post for every i < MsgBusQue.size(), MsgBusQue.at(i) = @pre(MsgBusQue.at(i))
     * @post MsgBusQue.last = m //TODO: notice msgBus is a map, not a queue
     * @post MsgBusQue.at(m).isEmpty() = true
     */
    void register(MicroService m);

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     * // MsgBusQue is the general data structure for the MessageBus MicroServices queue's
     * // Events is a general data structure that contains the Events type and their subscribers.
     * // Broadcasts is a general data structure that contains the Broadcasts type and their subscribers.
     * @pre m exists in MsgBusQue
     * @pre m isn't null
     * @post No references to the MicroService m in MsgBusQue/Subscriptions
     * @post MsgBusQue.size() = @pre(MsgBusQue.size()) - 1
     * @post m doesn't exists in MsgBusQue
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     * // MsgBusQue is the general data structure for the MessageBus MicroServices queue's
     * @pre m exists in MsgBusQue, or else throw IllegalStateException
     * if no InterruptedException occurred: // There was a message in the queue
     * @post MsgBusQue.at(m).size() = @pre(MsgBusQue.at(m).size()) - 1
     * @post return value = @pre(MsgBusQue.at(m).first)
     * if InterruptedException occurred: // There wasn't a message in the queue
     * @post MsgBusQue.at(m).size() = @pre(MsgBusQue.at(m).size())
     * @post the return value equal to the message that was added to the MsgBusQue.at(m) during the InterruptedException.
     */
    Message awaitMessage(MicroService m) throws InterruptedException;

    // Returns true if the MicroService is subscribed to receive type Messages
    boolean isMicroServiceSubscribed(Class<? extends Message> type, MicroService m);

    // Returns true if a Broadcast Message was added to the queue's of the MicroServices
    boolean isBroadcastAdded(Message msg, MicroService m);

    // Returns true if an Event Message was added to the queue's of the MicroServices
    <T> boolean isEventAdded(Message msg, MicroService m);

    // Returns true if a MicroService is in the MessageBus queue
    boolean isMicroServiceRegistered(MicroService m);

    //Returns true if a MicroService isn't in a MessageBus queue, and if there are no references to the MicroService at MessageBus.
    boolean isMicroServiceUnRegistered(MicroService m);
}
