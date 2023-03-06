package bgu.spl.mics;

import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageBusTest {

    private MessageBusImpl bus;
    private ExampleEvent testEvent;
    private MicroService service;
    private ExampleBroadcast testBroadcast;

    @Before
    public void setUp() throws Exception {
        bus = new MessageBusImpl();
        testEvent = new ExampleEvent("EventTester");
        service = new StudentService(new Student("Student", "dep", null));
        testBroadcast = new ExampleBroadcast("BroadcastTester");
    }

    @After
    public void tearDown() throws Exception {
        bus = null;
        testEvent = null;
        service = null;
        testBroadcast = null;
    }

    @Test
    public void testSubscribeEvent() {
        assertThrows("Should throw exception (MicroService is Null)", Exception.class,
                () -> bus.subscribeEvent(testEvent.getClass(), null));
        assertThrows("Should throw exception (MicroService doesn't have a queue in MessageBus queue)", Exception.class,
                () -> bus.subscribeEvent(testEvent.getClass(), service));
        bus.register(service);
        assertFalse(bus.isMicroServiceSubscribed(testEvent.getClass(), service));
        bus.subscribeEvent(testEvent.getClass(), service);
        assertTrue(bus.isMicroServiceSubscribed(testEvent.getClass(), service));
    }

    @Test
    public void testSubscribeBroadcast() {
        assertThrows("Should throw exception (MicroService is Null)", Exception.class,
                () -> bus.subscribeBroadcast(testBroadcast.getClass(), null));
        assertThrows("Should throw exception (MicroService doesn't have a queue in MessageBus queue)", Exception.class,
                () -> bus.subscribeBroadcast(testBroadcast.getClass(), service));
        bus.register(service);
        assertFalse(bus.isMicroServiceSubscribed(testBroadcast.getClass(), service));
        bus.subscribeBroadcast(testBroadcast.getClass(), service);
        assertTrue(bus.isMicroServiceSubscribed(testBroadcast.getClass(), service));
    }

    @Test
    public void testComplete() throws InterruptedException {
        bus.register(service);
        bus.subscribeEvent(testEvent.getClass(), service);
        Future<String> future = bus.sendEvent(testEvent);
        assertThrows("should throw exception (event is null)", Exception.class,
                () -> bus.complete(null, "completed"));
        assertFalse(future.isDone());
        assertNull(future.result);
        bus.complete(testEvent, "completed");
        assertTrue(future.isDone());
        assertEquals(future.get(), "completed");
    }

    @Test
    public void testSendBroadcast() {
        bus.register(service);
        bus.subscribeBroadcast(testBroadcast.getClass(), service);
        assertThrows("should throw exception (null broadcast)", Exception.class,
                () -> bus.sendBroadcast(null));
        assertFalse(bus.isBroadcastAdded(testBroadcast, service));
        bus.sendBroadcast(testBroadcast);
        assertTrue(bus.isBroadcastAdded(testBroadcast, service));
    }

    @Test
    public void testSendEvent() {
        bus.register(service);
        bus.subscribeEvent(testEvent.getClass(), service);
        assertThrows("should throw exception (null event)", Exception.class,
                () -> bus.sendEvent(null));
        assertFalse(bus.isEventAdded(testEvent, service));
        Future<String> future = bus.sendEvent(testEvent);
        assertFalse(future.isDone());
        assertNull(future.result);
        assertTrue(bus.isEventAdded(testEvent, service));
    }

    @Test
    public void testRegister() {
        assertThrows("Should throw exception (MicroService is Null)", Exception.class,
                () -> bus.register(null));
        assertFalse(bus.isMicroServiceRegistered(service));
        bus.register(service);
        assertTrue(bus.isMicroServiceRegistered(service));
    }

    @Test
    public void testUnregister() {
        assertThrows("Should throw exception (MicroService is Null)", Exception.class,
                () -> bus.unregister(null));
        bus.register(service);
        StudentService microServiceTwo = new StudentService(new Student("tal", "Computer Science", Student.Degree.PhD));
        bus.register(microServiceTwo);
        bus.subscribeBroadcast(testBroadcast.getClass(), microServiceTwo);
        service.sendBroadcast(testBroadcast);
        assertFalse(bus.isMicroServiceUnRegistered(service));
        bus.unregister(service);
        assertTrue(bus.isMicroServiceUnRegistered(service));
    }

    @Test
    public void testAwaitMessage() throws InterruptedException {
        assertThrows("should throw exception (IllegalStateException)", Exception.class,
                () -> bus.awaitMessage(service));
        bus.register(service);
        bus.subscribeEvent(testEvent.getClass(), service);
        bus.sendEvent(testEvent);
        assertEquals(((ExampleEvent)(bus.awaitMessage(service))).getSenderName(),"EventTester");
    }
}