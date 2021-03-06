package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.Semaphore;


/**
 * Low level implementation of an unfair Semephore with arbitrary capacity.
 */
@ThreadSafe
public class LowLevelSemaphore extends Semaphore {
    @GuardedBy(value = "lock")
    private int capacity;
    private final Object lock = new Object();

    public LowLevelSemaphore(int capacity){
        super(0);
        this.capacity = capacity;
    }

    @Override
    public void acquire() throws InterruptedException {
        synchronized (lock){
            while(capacity == 0){
                lock.wait();
            }
            capacity--;
        }
    }

    @Override
    public void release() {
        synchronized (lock){
            capacity++;
            lock.notifyAll();
        }
    }
}