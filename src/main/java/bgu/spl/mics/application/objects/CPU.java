package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private int db_processed = 0;
    private int time_used = 0;
    private int num_of_cores;
    private LinkedList<DataBatch> data;
    Cluster cluster;


    public CPU(int cores,Cluster cluster){
        num_of_cores = cores;
        this.cluster = cluster;
        data = new LinkedList<DataBatch>();
    }

    /**
     * simple getter
     * @return the amount of db that been processed by this cpu
     */

    public int getDb_processed() {
        return db_processed;
    }

    /**
     * simple getter
     * @return the total time (in ticks) which this cpu worked.
     */

    public int getTime_used() {
        return time_used;
    }


    /**
     * simple getter
     * @return data list.
     */
    public LinkedList<DataBatch> getData(){
        return data;
    }

    /**
     *
     * @return the size of the data list.
     */
    public int data_size(){
        return data.size();
    }

    /**
     *
     * @param type - the type of the data.
     * @return returns - the number of ticks that are needed to process a single data batch of type @param type.
     */
    public int tickToProcess(Data.Type type){
        switch (type){
            case Images:
                return (32/num_of_cores)*4;
            case Text:
                return (32/num_of_cores)*2;
            case Tabular:
                return (32 / num_of_cores);
        }
        return (32 / num_of_cores);
    }

    /**
     * @return returns - the number of ticks that are needed to process a single data batch of type data.getfirst()
     */
    public int tickToProcess(){
        DataBatch db = data.getFirst();
        Data.Type type = db.getData().getType();
        return tickToProcess(type);
    }

    /**
     * this method adds Data Batch to the list data.
     * @param db the Data Batch which we add to the data vector.
     *        db is not null.
     * @post data.size() = @pre(data.size()) + 1
     * @post data.getLast() = @param db
     */
    public void addDataBatch(DataBatch db){
        if (db == null){
            throw new IllegalArgumentException("parameter data batch is null, can't add it to the cpu");
        }
        data.addLast(db);

    }

    /**
     * this method process 1 Data Batch from the list data, and return it.
     * @pre this.data.isEmpty() == false
     * @post this.data.size() + 1 = @pre(this.data.size())
     * @return the processed Data Batch
     */
    public synchronized void processDB(){
        if (data_size() == 0)
            throw new IllegalStateException("There is no data to process");
        time_used = time_used + tickToProcess();
        cluster.updateTimeUsed(true,tickToProcess());
        DataBatch ret = data.removeFirst();
        db_processed ++;
        cluster.deliverProcessedDBtoGPU(ret);
    }

    public boolean isBusy(){
        return data_size() > 0;
    }


}