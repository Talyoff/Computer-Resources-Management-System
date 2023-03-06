package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {

    private String name;
    private int date;
    private LinkedList<Model> models;

    public ConferenceInformation(String name, Integer date) {
        this.name = name;
        this.date = date;
        models = new LinkedList<>();
    }

    public LinkedList<Model> getModels(){
        return models;
    }

    public void addModel(Model model){
        models.add(model);
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

    public String toString(){
        String str = "";
        str += "\t name: " + getName();
        str += "\n\t date: " + date;
        str += "\n\t publication: " + "\n";
        for (Model m : models){
            str += m + "\n";
        }
        return str;
    }
}
