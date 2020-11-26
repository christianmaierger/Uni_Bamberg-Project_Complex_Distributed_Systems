package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import net.jcip.annotations.GuardedBy;

public class LowLevelSemaphore {
    @GuardedBy(value = "lock")
    private int capacity;
    private final Object lock = new Object();

    public LowLevelSemaphore(int capacity){
        this.capacity = capacity;
    }

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

    public void release() {
        synchronized (lock){
            capacity++;
            lock.notifyAll();
        }
    }

}