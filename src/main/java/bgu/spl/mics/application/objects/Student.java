package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private Vector<Model> models;
    private Vector<Model> trainedModels;

    public Student(String name, String department, Degree status) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.publications = 0;
        this.papersRead = 0;
        this.models = new Vector<>();
        this.trainedModels = new Vector<>();
    }

    public String getName() {
        return name;
    }

    public Vector<Model> getModels(){
        return models;
    }

    public Vector<Model> getTrainedModels() {
        return trainedModels;
    }

    public void increasePublications() {
        publications++;
    }

    public void increasePapersRead() {
        papersRead++;
    }

    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    public void setModels(Vector<Model> models) {
        this.models = models;
    }

    public Degree getStatus() {
        return status;
    }

    public String toString(){
        String str = "";
        str += "\t name: " + getName();
        str += "\n\t department: " + department;
        str += "\n\t status: " + status;
        str += "\n\t publication: " + publications;
        str += "\n\t papersRead: " + papersRead + "\n";
        str += "\t trainedModels: \n";
        for (int i = 0; i < trainedModels.size(); i++) {
            str += trainedModels.elementAt(i).toString() + "\n";
        }
        return str + "\n";
    }

}
