package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared;


import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * A Thread responsible for printing received Messages to console. It offers a public put method to enqueue Messages.
 * It will print all Messages in the order in which they are placed until it either receives a poison pill,
 * a Message of type MessageType.FINISH or is interrupted.
 *
 * It can be instantiated as using only high level or only low level methods via the "Service type" parameter
 * of the constructor. It is not threadsafe and only one instance should be used at a time.
 */
@NotThreadSafe
public class OutputServiceCallable implements Runnable{
    private final OutputService outputService;
    @GuardedBy(value = "itself")
    private final BlockingQueue<Message> queue;



    public OutputServiceCallable(){
       queue = new ArrayBlockingQueue<>(500, true);
        this.outputService = new OutputService();
    }



    @Override
    public void run(){
        boolean finished = false;
        while(!finished){
            try {
                Message message = queue.take();
                if (MessageType.FINISH.equals(message.getType())) {
                    finished = true;
                } else if(MessageType.FILE.equals(message.getType())){
                    outputService.logProcessedFile(message.getPath());
                } else{
                    outputService.logProcessedDirectory(message.getPath(), message.getHistogram());
                }
            } catch (InterruptedException exception) {
                finished = true;
            }
        }
    }

    /**
     * Puts Message into the pipeline. They will be printed to console in the order in which they are put.
     * @param message message to be printed. MessageType.FILE will print out that the file has been finished.
     *                MessageType.FOLDER will additionally print out a current intermediary result of the Histogram
     *                processing. MessageType.FINISH will terminate this Thread.
     * @throws InterruptedException if Thread is interrupted
     */
    public void put(Message message) throws InterruptedException {
        queue.put(message);
    }
}
