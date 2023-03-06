package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    public Data getData() {
        return data;
    }

    private Data data;
    private int start_index;
    private GPU gpu_parent;



    public DataBatch(Data data) {
        this.data = data;
        start_index = 0;
        gpu_parent = null;
    }
    public GPU getGpu_parent() {
        return gpu_parent;
    }

    public void setGpu_parent(GPU gpu_parent) {
        this.gpu_parent = gpu_parent;
    }

}
