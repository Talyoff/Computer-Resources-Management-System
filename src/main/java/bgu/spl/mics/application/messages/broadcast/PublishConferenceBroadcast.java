package bgu.spl.mics.application.messages.broadcast;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

public class PublishConferenceBroadcast implements Broadcast {

    LinkedList<Model> models;

    public PublishConferenceBroadcast(LinkedList<Model> models){
        this.models = models;
    }

    public LinkedList<Model> getModels(){
        return models;
    }

}
