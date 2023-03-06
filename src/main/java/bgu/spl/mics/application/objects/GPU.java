package bgu.spl.mics.application.objects;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080};

    private Type type;
    private Model model;
    private Cluster cluster;
    private int tick;
    private Vector<DataBatch> unprocessed_db;
    private Vector<DataBatch> processed_db;

    private int time_used = 0;


    /**
     * cunstructor : initialize a new GPU
     *
     * @param type    - the type of the GPU (from GPU Type enum)
     * @param cluster - the cluster which the GPU belong to.
     * @param tick    - the current tick (time) of the system.
     */
    public GPU(Type type, Cluster cluster, int tick) {
        this.type = type;
        this.cluster = cluster;
        model = null;
        this.tick = tick;
        processed_db = new Vector<DataBatch>();
        unprocessed_db = new Vector<DataBatch>();
    }

    /**
     * simple getter
     * @return the gpu time used, in ticks.
     */
    public int getTime_used() {
        return time_used;
    }

    /**
     * simple getter
     * @return the unprocessed data batches vector
     */

    public Vector<DataBatch> getUnprocessed_db() {
        return unprocessed_db;
    }

    /**
     * simple getter
     * @return the processed data batches vector
     */


    public Vector<DataBatch> getProcessed_db() {
        return processed_db;
    }


    /**
     * simple getter
     * @return this.model
     */
    public Model getModel(){
        return this.model;
    }

    /**
     * simple getter
     * @return this.tick
     */
    public int getTick(){
        return tick;
    }

    /**
     * simple getter
     * @return this.type
     */
    public Type getType(){
        return this.type;
    }

    /**
     * this method changes the GPU type to @param type
     * used for testing purpose only
     * @param type
     * @post this.getType() == @param type
     */
    public void  setType(Type type){
        this.type = type;

    }

    /**
     * @post this.getTick() = @pre(this.getTick()) + 1
     */
    public void setTick(){
        this.tick += 1;
    }

    /**
     * @return return the time it take the GPU to process a single data batches
     */
    public int ticksToTrainDataBatches(){
       switch (type){
           case RTX3090:
               return 1;
           case RTX2080:
               return 2;
           case GTX1080:
               return 4;
       }
        return 4;
    }

    /**
     * @return return the numbers of data batches that the GPU can store
     */
    public int ProcessedDataBatchesStorageLim(){
        switch (type){
            case RTX3090:
                return 32;
            case RTX2080:
                return 16;
        }
        return 8;
    }

    /**
     * this method divided the model data into Data Batches it can send to the CPUS
     * @pre this.model is not null
     * @pre this.model.getStatus != trained,tested
     * @post this.getUnprocessed_db().isEmpty() == False;
     */
    public void divideIntoDataBatches(){
        Data data = model.getData();
        int size = data.getSize();
        int num_of_db = size/1000;
        for (int i=0; i<num_of_db ; i++){
            DataBatch db = new DataBatch(data);
            unprocessed_db.add(db);
        }

    }
    /**
     * this method gets a processed data batch, and add it to the processed_db vector.
     * @param db - a processed data batch.
     * @pre this.model is not null
     * @post this.getProcessed().size() = @(this.getProcessed().size()) + 1
     * @post this.getProcessed.contains(db) == True
     * @inv this.getProcessed().size() <= this.ProcessedDataBatchesStorageLim()
     */
    public synchronized void addProcessedDataBatch(DataBatch db){
        if (ProcessedDataBatchesStorageLim() == processed_db.size())
            throw new IllegalStateException("There is not enough space in the gpu VRAM");
        if (getModel() == null)
            throw new IllegalStateException("The gpu is not working on any model now");
        processed_db.add(db);
    }

    /**
     *
     * @param m the model to set
     * @pre this.getModel()== null
     * @post this.getModel() == m
     */
    public void setModel(Model m){
        if (m == null)
            throw new IllegalArgumentException("The model given as parameter cannot be null");
        if (model != null){
            throw new IllegalStateException("The gpu already have a model");
        }
        this.model = m;

    }

    /**
     *
     * @param model_to_test to model which needs to be tested, not null.
     * @return the tested model
     * @pre this.getModel() == Null
     * @pre (@param model_to_test.getStatus() == trained)
     * @post (@return model.getResult() == Good / Bad)
     * @post (@return model.getStatus() == Tested)
     * @post this.getModel() = null
     */
    public Model testModel(Model model_to_test){
        if (model_to_test == null || model_to_test.getStatus() != Model.Status.Trained) {
            throw new IllegalArgumentException("The model have not been trained yet");
        }
        if (model != null){
            throw new IllegalStateException("The gpu is already working on  a model");
        }
        model_to_test.setResult(Model.Result.Bad);
        Random rnd = new Random();
        double probability = rnd.nextDouble();
        if (model_to_test.getStudent().getStatus() == Student.Degree.MSc){
            if (probability <= 0.6)
                model_to_test.setResult(Model.Result.Good);
        }
        if (model_to_test.getStudent().getStatus() == Student.Degree.PhD){
            if (probability <= 0.8)
                model_to_test.setResult(Model.Result.Good);
        }
        model_to_test.setStatus(Model.Status.Tested);
        return model_to_test;
    }

    /**
     * traines a processed data batches, according to the GPU train time.
     * this method is called from the GPU micro service
     * @pre this.getProcessed_db().size > 0
     * @post this.getProcessed_db().size + 1 = @pre(this.getProcessed_db().size)
     */
    public void trainDataBatch(){
        Data modelData = model.getData();
        if (processed_db.size() == 0)
            throw new NoSuchElementException("no data batch to train");
        time_used += ticksToTrainDataBatches();
        cluster.updateTimeUsed(false,ticksToTrainDataBatches());
        processed_db.remove(0);
        modelData.updateProcessed();

    }

    /**
     * @pre this.getUnprocessed_db.size() > 0
     * @post this.getUnprocessed_db.size() + 1 = @pre(this.getUnprocessed_db.size())
     * used naive approach - send db to process only if have place to store them. //todo - search for better soulution
     */
    public void sendDataBatchToProcess(){
        if (unprocessed_db.size() == 0){
            throw new IllegalStateException("There is no data batch to process");
        }
        cluster.sendDbToProcess(unprocessed_db.remove(0),this);

    }

    /** this method is called when the GPU complete training the model, is sets the model status to trained,
     * and return the model to the GPUmicroService
     * @pre this.getProcessed_db().isEmpty() == true
     * @pre this.getUnprocessed_db().isEmpty() == true
     * @return model - the trained model.
     * @post  @return(model.getStatus() == Trained).
     * @post this.getModel() == null
     */
    public Model completeTraining(){
        if (processed_db.size() > 0 || unprocessed_db.size() > 0)
            throw new IllegalStateException("Cannot complete training, there are still untrained data batches");
        model.setStatus(Model.Status.Trained);
        Model ret = model;
        model = null;
        return ret;
    }

    /**
     *this methods clears the vectors of the GPU
     * used for testing purpose.
     */
    public void clearVector(){
        this.processed_db.clear();
        this.unprocessed_db.clear();

    }

    public int unProcessedSize(){
        return unprocessed_db.size();
    }

    public  int getProcessedSize(){
        return processed_db.size();
    }

    public int getModelDataProcessedNum(){
        return model.getData().getProcessed();
    }

    public int getModelDataSize(){
        return model.getData().getSize();
    }

    public boolean isTraining(){
        return model != null;
    }

}