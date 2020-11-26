package de.uniba.wiai.dsg.pks.assignment1.histogram.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OutputServiceThreaded extends Thread{
    private final BlockingQueue<Histogram> queue;

    public OutputServiceThreaded(){
        this.queue = new ArrayBlockingQueue<Histogram>(100);
    }

    @Override
    public void start(){

    }

    public void put(Histogram histogram) throws InterruptedException {
        queue.put(histogram);
    }
}
