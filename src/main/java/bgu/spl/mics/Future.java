package bgu.spl.mics;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	boolean completed;
	protected T result;
	
	/**
	 * This should be the only public constructor in this class.
	 */
	public Future() {
		completed = false;
		result = null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     */
	public T get() throws InterruptedException {
		if(!isDone()){
			try{
				synchronized (this) {
					this.wait();
				}
				return result;
			} catch (InterruptedException e) {
				return result;
			}
		}
		return result;
	}
	
	/**
	 * @param result - the result of the event
	 * @pre this.isDone() == false
	 * @pre this.result == null
	 * @post this.isDone() == true
	 * @post this.result = result
     * Resolves the result of this Future object.
     */
	public void resolve (T result) {
		this.result = result;
		completed = true;
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return completed;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public T get(long timeout, TimeUnit unit) {
		long waitTime = unit.toMillis(timeout);
		if(!isDone()){
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException ignored) {

			}
		}
		return result;
	}

}
