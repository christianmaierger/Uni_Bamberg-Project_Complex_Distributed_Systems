package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MessageType;
import net.jcip.annotations.GuardedBy;


import java.util.Arrays;
import java.util.Formatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * A Runnable responsible for printing received Messages to console. It offers a public put method to enqueue Messages.
 * It will print all Messages in the order in which they are placed until it either receives a poison pill
 * (a Message of type MessageType.FINISH) or is interrupted.
 *
 */
public class PrintService implements Runnable{
    private final static int MESSAGE_CAPACITY = 5000;
    private int lineNumber = 1;

    @GuardedBy(value = "itself")
    private final BlockingQueue<Message> queue;

    public PrintService(){
        this.queue = new ArrayBlockingQueue<>(MESSAGE_CAPACITY, true);
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
                    logProcessedFile(message.getPath());
                } else{
                    logProcessedDirectory(message.getPath(), message.getHistogram());
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

    /**
     * Prints a message to console that tells the user that "directoryPath" has been finished and also outputs the
     * intermediary result of the histogram.
     * @param directoryPath path of the directory that has been processed
     */
    private void logProcessedDirectory(String directoryPath, Histogram histogram){
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("N: %d - ", lineNumber);
        formatter.format("Directory %s finished ", directoryPath);
        formatter.format("[distr = %s, ", Arrays.toString(histogram.getDistribution()));
        formatter.format("lines=%d, ", histogram.getLines());
        formatter.format("files=%d, ", histogram.getFiles());
        formatter.format("processedFiles=%d, ", histogram.getProcessedFiles());
        formatter.format("directories=%d]", histogram.getDirectories());
        System.out.println(stringBuilder);
        lineNumber++;
    }

    /**
     * Prints a message to console that tells the user that "filePath" has been finished.
     * @param filePath path of the file that has been finished
     */
    private void logProcessedFile(String filePath){
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("N: %d - ", lineNumber);
        formatter.format("File %s finished !", filePath);
        System.out.println(stringBuilder);
        lineNumber++;
    }

}
