package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcast.PublishTerminateBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.objects.ConferenceInformation;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConferenceInformation conferenceInformation;

    public ConferenceService(String name, ConferenceInformation conferenceInformation) {
        super(name);
        this.conferenceInformation = conferenceInformation;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishTerminateBroadcast.class, (PublishTerminateBroadcast) -> {
        });
        subscribeEvent(PublishResultsEvent.class, (PublishResultsEvent) ->{
                conferenceInformation.addModel(PublishResultsEvent.getModel());
        } );
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) -> {
            if (TickBroadcast.getTicksPassed() == conferenceInformation.getDate()) {
                PublishConferenceBroadcast pcb = new PublishConferenceBroadcast(conferenceInformation.getModels());
                sendBroadcast(pcb);
                Thread.currentThread().interrupt();
            }
        } );
    }
}
