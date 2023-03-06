package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    public Data(Type type, int size) {
        this.type = type;
        this.processed = 0;
        this.size = size;
    }

    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;

    private int size;

    public int getProcessed() {
        return processed;
    }

    public Type getType() {
        return type;
    }

    public int getSize() {
        return size;
    }
    public void updateProcessed(){
        processed++;
    }

    public String toString(){
        String str = "\n";
        str += "\t\ttype: " + type + "\n";
        str += "\t\tsize: " + size;
        return str;
    }
}
