package bgu.spl.mics.application.objects;
import static org.junit.Assert.*;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import java.util.Vector;

public class GPUTest extends TestCase {

    private GPU gpu;
    private Cluster cluster;
    private Data data;
    private Model m;
    private Student student;
    private DataBatch db;
    private DataBatch db1;
    private CPU cpu;

    @Before
    public void setUp() throws Exception {
        cluster = Cluster.getInstance();
        gpu = new GPU(GPU.Type.RTX3090,cluster,0);
        data = new Data(Data.Type.Images,50000);
        student = new Student("Tal","CS", Student.Degree.MSc);
        db = new DataBatch(data);
        db1 = new DataBatch(data);
        cpu = new CPU(4,cluster);
        cluster.addCPU(cpu);
        m = new Model("Yuval",data,student);
    }

    @After
    public void tearDown() throws Exception {
        gpu = new GPU(GPU.Type.RTX3090,cluster,0);
        this.m = new Model("Yuval",data,student);
    }

    public void testSetTick() {
        int pre_tick = gpu.getTick();
        gpu.setTick();
        assertEquals(pre_tick+1,gpu.getTick());
    }
    public void testSetType() {
        gpu.setType(GPU.Type.GTX1080);
        assertEquals(GPU.Type.GTX1080,gpu.getType());
        gpu.setType(GPU.Type.RTX2080);
        assertEquals(GPU.Type.RTX2080,gpu.getType());
        gpu.setType(GPU.Type.RTX3090);
        assertEquals(GPU.Type.RTX3090,gpu.getType());
    }

    public void testTicksToTrainDataBatches() {
        assertEquals(1,gpu.ticksToTrainDataBatches());
        gpu.setType(GPU.Type.RTX2080);
        assertEquals(2,gpu.ticksToTrainDataBatches());
        gpu.setType(GPU.Type.GTX1080);
        assertEquals(4,gpu.ticksToTrainDataBatches());
    }

    public void testProcessedDataBatchesStorageLim() {
        assertEquals(32,gpu.ProcessedDataBatchesStorageLim());
        gpu.setType(GPU.Type.RTX2080);
        assertEquals(16,gpu.ProcessedDataBatchesStorageLim());
        gpu.setType(GPU.Type.GTX1080);
        assertEquals(8,gpu.ProcessedDataBatchesStorageLim());
    }

    public void testDivideIntoDataBatches() {
        assertThrows("the GPU model is null",Exception.class,() -> gpu.divideIntoDataBatches());
        gpu.setModel(m);
        assertNotEquals(Model.Status.Tested,gpu.getModel().getStatus());
        assertNotEquals(Model.Status.Trained,gpu.getModel().getStatus());
        gpu.divideIntoDataBatches();
        assertFalse(gpu.getUnprocessed_db().isEmpty());


    }

    public void testAddProcessedDataBatch() {
        assertThrows("the GPU model is null",Exception.class,() -> gpu.addProcessedDataBatch(db));
        Vector<DataBatch> processed_db_before = gpu.getProcessed_db();
        int size_before = processed_db_before.size();
        gpu.setModel(m);
        int processed_number_in_model_before = gpu.getModel().getData().getProcessed();
        gpu.addProcessedDataBatch(db);
        assertEquals(size_before+1,gpu.getProcessed_db().size());
        assertTrue(gpu.getProcessed_db().contains(db));
        //checking the inv
        assertTrue(gpu.getProcessed_db().size() <= gpu.ProcessedDataBatchesStorageLim());
    }

    public void testSetModel() {
        assertThrows("model cannot be null",IllegalArgumentException.class,()->gpu.setModel(null));
        assertNull(gpu.getModel());
        gpu.setModel(m);
        assertEquals(m,gpu.getModel());
    }

    public void testTestModel() {
        gpu.setModel(m);
        assertThrows("model must be null before this method",
                Exception.class,()-> gpu.testModel(m));
        assertThrows("the model that was given as parameter cannot be null",
                IllegalArgumentException.class,()->gpu.testModel(null));
        m.setStatus(Model.Status.PreTrained);
        assertThrows("the model that was given as parameter should be at status trained",
                IllegalArgumentException.class,()->gpu.testModel(m));
        m.setStatus(Model.Status.Tested);
        assertThrows("the model that was given as parameter should be at status trained",
                IllegalArgumentException.class,()->gpu.testModel(m));
        m.setStatus(Model.Status.Training);
        assertThrows("the model that was given as parameter should be at status trained",
                IllegalArgumentException.class,()->gpu.testModel(m));
        m.setStatus(Model.Status.Trained);
        gpu = new GPU(GPU.Type.RTX3090,cluster,0);
        Model returned = gpu.testModel(m);
        assertTrue(returned.getStatus() == Model.Status.Tested );
        assertTrue(returned.getResult() == Model.Result.Good || returned.getResult() == Model.Result.Bad );
        assertNull(gpu.getModel());
    }

    public void testTrainDataBatche() {
        assertThrows("not processed data batch to train",Exception.class,
                ()->gpu.trainDataBatch());
        gpu.setModel(m);
        gpu.addProcessedDataBatch(db);
        Vector<DataBatch> processed_db_before = gpu.getProcessed_db();
        int sizeBefore = processed_db_before.size();
        gpu.trainDataBatch();
        assertEquals(sizeBefore,gpu.getProcessed_db().size() + 1);

    }

    public void testSendDataBatchToProcess() {
        assertThrows("no data batch to process",Exception.class,()->gpu.sendDataBatchToProcess());
        gpu.setModel(m);
        gpu.divideIntoDataBatches();
        int num_of_unprocessed_db = gpu.getUnprocessed_db().size();
        gpu.sendDataBatchToProcess();
        assertEquals(num_of_unprocessed_db,gpu.getUnprocessed_db().size() + 1);
    }

    public void testCompleteTraining() {
        gpu.setModel(m);
        gpu.divideIntoDataBatches();
        gpu.addProcessedDataBatch(db);
        assertThrows("didn't finish process,train all data batches",
                Exception.class,()->gpu.completeTraining());
        gpu.clearVector();
        Model ret_m = gpu.completeTraining();
        assertNull(gpu.getModel());
        assertEquals(Model.Status.Trained,ret_m.getStatus());

    }

    public void testClearVector(){
        gpu.setModel(m);
        gpu.divideIntoDataBatches();
        gpu.addProcessedDataBatch(db);
        gpu.clearVector();
        assertTrue(gpu.getProcessed_db().isEmpty());
        assertTrue(gpu.getUnprocessed_db().isEmpty());
    }


}