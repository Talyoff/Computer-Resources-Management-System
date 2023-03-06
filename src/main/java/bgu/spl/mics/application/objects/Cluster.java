package bgu.spl.mics.application.objects;


import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {


	private static Cluster instance = null;
	LinkedList<GPU> gpus ;
	LinkedList<CPU> cpus ;

	Integer gpus_time_used;

	Integer cpus_time_used;
	Integer db_processed;
	final Object lockAddingDataBatchesToCpu;

	private Cluster(){
		gpus = new LinkedList<GPU>();
		cpus = new LinkedList<CPU>();
		lockAddingDataBatchesToCpu = new Object();
		gpus_time_used = 0;
		cpus_time_used = 0;
		db_processed = 0;

	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		if(instance == null)
			instance = new Cluster();
		return instance;
	}

	public void addCPU(CPU cpu){
		cpus.add(cpu);
	}

	public void addGPU(GPU gpu){
		gpus.add(gpu);
	}
	/**
	 * this method gets a container of data batches and send it to the cpus
	 * @param db
	 * @param sender
	 */

	public void sendDbToProcess(DataBatch db,GPU sender){
		synchronized (lockAddingDataBatchesToCpu){
				db.setGpu_parent(sender);
				int min_busy_idx = 0;
				int min_busy_amount = 100000;
			for (int i = 0; i < cpus.size(); i++) {
				CPU targetCPU = cpus.get(i);
				if (targetCPU.data_size() < min_busy_amount) {
					min_busy_idx = i;
					min_busy_amount = targetCPU.data_size();
				}
			}
				synchronized (cpus.get(min_busy_idx)){
					cpus.get(min_busy_idx).addDataBatch(db);
				}
			}
	}

	public void deliverProcessedDBtoGPU(DataBatch db){
		GPU parent = db.getGpu_parent();
		synchronized (parent){
			parent.addProcessedDataBatch(db);
		}
		synchronized (db_processed){
			db_processed++;
		}
	}

	public synchronized void updateTimeUsed(boolean cpu,int time_used){
		if (cpu){
			cpus_time_used += time_used;
		}
		else {
			gpus_time_used += time_used;
		}

	}

	public Integer getGpus_time_used() {
		return gpus_time_used;
	}

	public Integer getCpus_time_used() {
		return cpus_time_used;
	}

	public Integer getDb_processed() {
		return db_processed;
	}




}
