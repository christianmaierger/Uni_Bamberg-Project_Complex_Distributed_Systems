package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared;


import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

import java.util.Arrays;
import java.util.Formatter;
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
public class PrintService implements Runnable{

    private int lineNumber = 1;

    @GuardedBy(value ="itself")
    private final PrintService printService;
    @GuardedBy(value = "itself")
    private final BlockingQueue<Message> queue;



    public PrintService(){
        queue = new ArrayBlockingQueue<>(500, true);
        this.printService = new PrintService();
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
                    printService.logProcessedFile(message.getPath());
                } else{
                    printService.logProcessedDirectory(message.getPath(), message.getHistogram());
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
    public void logProcessedDirectory(String directoryPath, Histogram histogram){
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
    public void logProcessedFile(String filePath){
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("N: %d - ", lineNumber);
        formatter.format("File %s finished !", filePath);
        System.out.println(stringBuilder);
        lineNumber++;
    }
}
