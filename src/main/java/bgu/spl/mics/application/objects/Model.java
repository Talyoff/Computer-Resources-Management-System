package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    public enum Status {PreTrained,Training,Trained,Tested}
    public enum Result {None, Good , Bad}
    private String name;
    private Data data;
    private Status status;
    private Result result;
    private Student student;

    public Model(String name, Data data, Student student){
        this.name = name;
        this.data = data;
        this.student = student;
        this.status = Status.PreTrained;
        this.result = Result.None;

    }
    public void setResult(Result result) {
        this.result = result;
    }


    public String getName() {
        return name;
    }

    public Result getResult() {
        return result;
    }

    public Student getStudent() {
        return student;
    }

    public Status getStatus(){
        return this.status;
    }

    public Data getData(){
        return data;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public String toString(){
        String str = "\n";
        str += "\t\tname: " + getName();
        str += data + "\n";
        str += "\t\tstatus: " + status +"\n";
        str += "\t\tresult: " + result;
        return str;
    }

}
