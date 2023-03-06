package bgu.spl.mics.application.objects;
import static org.junit.Assert.*;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

public class CPUTest extends TestCase {
    private CPU cpu;
    private GPU gpu;
    private Cluster cluster;
    private Data data;
    private DataBatch db;
    private Model m;
    private Student student;
@Before
    public void setUp() throws Exception {
        student = new Student("Tal","CS", Student.Degree.MSc);
        gpu = new GPU(GPU.Type.RTX3090,Cluster.getInstance(),0);
        cluster = Cluster.getInstance();
        cluster.addGPU(gpu);
        cpu = new CPU(4,cluster);
        data = new Data(Data.Type.Images,50000);
        db = new DataBatch(data);
        db.setGpu_parent(gpu);
        m = new Model("Yuval",data,student);
        gpu.setModel(m);
    }
@After
    public void tearDown() throws Exception {
        cluster = Cluster.getInstance();
        cpu = new CPU(4,cluster);
    }

    public void testTickToProcess() {
        //images - with 4 cores should take 32 ticks.
        assertEquals(32,cpu.tickToProcess(Data.Type.Images));
        //Text - with 4 cores should take 16 ticks.
        assertEquals(16,cpu.tickToProcess(Data.Type.Text));
        //Tabular - with 4 cores should take 8 ticks.
        assertEquals(8,cpu.tickToProcess(Data.Type.Tabular));
    }

    public void testAddDataBatch() {
        assertThrows("db cannot be null"
                ,IllegalArgumentException.class,()->cpu.addDataBatch(null));
        int size_before = cpu.data_size();
        cpu.addDataBatch(db);
        assertEquals(size_before + 1,cpu.data_size());
        assertEquals(db,cpu.getData().getLast());
    }

    public void testProcessDB() {
        assertThrows("Data Batch container cannot be empty",Exception.class,
                ()->cpu.processDB());
        cpu.addDataBatch(db);
        int size_before = cpu.data_size();
        cpu.processDB();
        assertEquals(size_before - 1 ,cpu.data_size());

    }
}