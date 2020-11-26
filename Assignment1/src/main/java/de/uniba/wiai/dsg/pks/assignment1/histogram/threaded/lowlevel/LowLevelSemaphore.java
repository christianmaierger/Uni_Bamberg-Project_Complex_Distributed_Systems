package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import net.jcip.annotations.GuardedBy;

import java.util.concurrent.Semaphore;

public class LowLevelSemaphore extends Semaphore {
    @GuardedBy(value = "lock")
    private int capacity;
    private final Object lock = new Object();

    public LowLevelSemaphore(int capacity){
        super(capacity);
        this.capacity = capacity;
    }

    @Override
    public void acquire() {
        synchronized (lock){
            while(capacity == 0){
                try{
                    lock.wait();
                }
                catch (InterruptedException ie){
                    continue;
                }
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