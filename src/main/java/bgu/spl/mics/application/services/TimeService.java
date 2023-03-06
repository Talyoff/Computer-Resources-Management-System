package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.PublishTerminateBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	MessageBusImpl msgBus = MessageBusImpl.getInstance();
	int tickTime;
	int duration;
	int ticksPassed = 0;

	public TimeService(int tickTime, int duration) {
		super("TimeService");
		this.tickTime = tickTime;
		this.duration = duration;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(PublishTerminateBroadcast.class, (PublishTerminateBroadcast) -> {

		});
		TickBroadcast tickBroadcast = new TickBroadcast(ticksPassed, tickTime);
		msgBus.sendBroadcast(tickBroadcast);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				TickBroadcast tickBroadcast = new TickBroadcast(ticksPassed, tickTime);
				if(ticksPassed > duration) {
					timer.cancel();
					sendBroadcast(new PublishTerminateBroadcast());
				}
				else {
					msgBus.sendBroadcast(tickBroadcast);
					ticksPassed++;
				}
			}
		}, 0, tickTime);
	}
}
