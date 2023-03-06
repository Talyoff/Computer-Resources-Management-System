package bgu.spl.mics;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class FutureTest  extends TestCase {

    private Future <Integer> future;

    @Before
    public void setUp() throws Exception {
        future = new Future<>();
    }

    @After
    public void tearDown() throws Exception {
        future = null;
    }

    @Test
    public void testResolve() {
        assertFalse(future.isDone());
        assertEquals(null,future.result);
        future.resolve(5);
        assertTrue(future.isDone());
        assertEquals(Integer.valueOf(5),future.result);
    }

    @Test
    public void testTestGet(){
        assertNull(future.get(1, TimeUnit.SECONDS));
        future.resolve(5);
        assertEquals(Integer.valueOf(5),future.get(10, TimeUnit.SECONDS));
    }
}