package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.PublishTerminateBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the {@link //DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    private CPU cpuObj;
    int duration_of_current_db_process = 0;

    public CPUService(String name, CPU cpu) {
        super(name);
        cpuObj = cpu;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishTerminateBroadcast.class, (PublishTerminateBroadcast) -> {
        });
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) -> {
            process_data();
        } );
    }

    private void process_data() {
        // if there is a data batch to process, check if enough tick passed to complete this process
        // if the process can be complete, complete it and set the duration of current db process to 0.
        if (cpuObj.data_size() > 0) {
            duration_of_current_db_process++;
            if (duration_of_current_db_process == cpuObj.tickToProcess()) {
                cpuObj.processDB();
                duration_of_current_db_process = 0;
            }
        }
    }
}
