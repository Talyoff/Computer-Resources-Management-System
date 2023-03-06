package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.PublishTerminateBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;
import java.util.Queue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private Event<Model> currentEvent;
    public LinkedList<Event<Model>> waitingEvents;
    private GPU gpuObj;
    int duration_of_current_db_training = 0;

    public GPUService(String name, GPU gpu) {
        super(name);
        gpuObj = gpu;
        waitingEvents = new LinkedList<>();
        currentEvent = null;
    }

    private void startTraining(Model model) {
        gpuObj.setModel(model);
        gpuObj.divideIntoDataBatches();
        int VRAMlim = gpuObj.ProcessedDataBatchesStorageLim();
        int limit = Math.min(VRAMlim, gpuObj.unProcessedSize());
        // sending data batch to process, how many? the minimum between gpu storage limit, and the number of unProcessed data batch.
        for (int i = 0; i < limit; i++) {
            gpuObj.sendDataBatchToProcess();
        }

    }

    private void workAfterTick() {
        if (currentEvent == null)
            return; // nothing to do
        if (gpuObj.getProcessedSize() > 0) {
            duration_of_current_db_training++;
            if (duration_of_current_db_training == gpuObj.ticksToTrainDataBatches()) {
                gpuObj.trainDataBatch();
                duration_of_current_db_training = 0;
            }
            if (duration_of_current_db_training == 0) {
                if (gpuObj.unProcessedSize() > 0) {
                    //started training new model, send one to process.
                    gpuObj.sendDataBatchToProcess();
                    return; //nothing else to do
                }
            }
        }
        if (gpuObj.isTraining() && gpuObj.getModelDataProcessedNum() * 1000 == gpuObj.getModelDataSize()) {
            Model result = gpuObj.completeTraining();
            complete(currentEvent, result);
            dealWithNextEvent();
        }
    }

    /**
     * this method is called when finishing handling an event (after tick)
     */
    private void dealWithNextEvent() {
        if (waitingEvents.peek() == null) {
            currentEvent = null;
            return; // no other event to handle;
        } else {
            Event<Model> nextEvent = waitingEvents.removeFirst();
            currentEvent = nextEvent;
            if (nextEvent.getClass() == TrainModelEvent.class)
                startTraining(((TrainModelEvent) nextEvent).getModel());
            if (nextEvent.getClass() == TestModelEvent.class) {
//                System.out.println(this.getName() +  "&&&&&&&&&& Tested the model of " + ((TestModelEvent)(currentEvent)).getModel().getStudent().getName());
                Model tested_model = gpuObj.testModel(((TestModelEvent) nextEvent).getModel());
                complete(nextEvent, tested_model);
                currentEvent = null; // finished with this event
                dealWithNextEvent(); // recursive call - handle next event
            }
        }

    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishTerminateBroadcast.class, (PublishTerminateBroadcast) -> {

        });
        subscribeEvent(TrainModelEvent.class, (TrainModelEvent) -> {
            if (currentEvent == null) {
                currentEvent = TrainModelEvent;
                startTraining(TrainModelEvent.getModel());
            } else waitingEvents.addLast(TrainModelEvent);
        });
        subscribeEvent(TestModelEvent.class, (TestModelEvent) -> {
            if (currentEvent == null) {
                Model tested_model = gpuObj.testModel(TestModelEvent.getModel());
                complete(TestModelEvent, tested_model);
            } else waitingEvents.addFirst(TestModelEvent);

        });
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) -> {
            workAfterTick();
        });
    }
}
