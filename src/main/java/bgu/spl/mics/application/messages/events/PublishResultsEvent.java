package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {

    private Model model;

    public PublishResultsEvent(Model modelToPublish){
        model = modelToPublish;
    }

    public Model getModel(){
        return model;
    }

}
