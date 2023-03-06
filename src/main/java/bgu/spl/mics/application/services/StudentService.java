package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcast.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcast.PublishTerminateBroadcast;
import bgu.spl.mics.application.messages.broadcast.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    private final Student student;
    private Future<Model> future;
    private int modelCounter = 0;

    public StudentService(Student student) {
        super("Student " + student.getName());
        this.student = student;
        this.future = null;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishTerminateBroadcast.class, (PublishTerminateBroadcast) -> {

        });
        subscribeBroadcast(PublishConferenceBroadcast.class, (PublishConferenceBroadcast) -> {
            LinkedList<Model> models = PublishConferenceBroadcast.getModels();
            for(Model model : models){
                if(model.getStudent().getName().equals(student.getName()))
                    student.increasePublications();
                else
                    student.increasePapersRead();
            }
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast) -> {
            Vector<Model> modelsVector = student.getModels();
            if(modelCounter < modelsVector.size()){
            if(future == null){
              sendNextEvent(modelsVector.get(modelCounter));
            }
            else{ // future != null
                Model model = future.get(TickBroadcast.getTickTime() / 2, TimeUnit.MILLISECONDS);

                // if model == null, do nothing
                if(model != null){
                    modelsVector.setElementAt(model, modelCounter);
                    sendNextEvent(modelsVector.get(modelCounter));
                }
            }
        }
    });
    }

    private void sendNextEvent(Model model) {
        if(model.getStatus() == Model.Status.PreTrained){
            TrainModelEvent trainModelEvent = new TrainModelEvent(model);
            future = this.sendEvent(trainModelEvent);
        }
        else{
            if(model.getStatus() == Model.Status.Trained) {
                TestModelEvent testModelEvent = new TestModelEvent(model);
                future = this.sendEvent(testModelEvent);
                student.getTrainedModels().add(model);
            }
            else{
                if(model.getStatus() == Model.Status.Tested){
                    if(model.getResult() == Model.Result.Good) {
                        PublishResultsEvent publishResultsEvent = new PublishResultsEvent(model);
                        this.sendEvent(publishResultsEvent);
                    }
                    future = null;
                    modelCounter++;
                }
            }
        }
    }
}
