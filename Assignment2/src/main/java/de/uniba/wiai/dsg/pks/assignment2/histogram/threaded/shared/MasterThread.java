package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.Service;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;



@NotThreadSafe
public class MasterThread extends Thread{
    @GuardedBy(value = "booleanSemaphore")
    private final Histogram histogram;
    private final String rootFolder;
    private final String fileExtension;

    @GuardedBy(value = "itself")
    private final Semaphore threadSemaphore;

    @GuardedBy(value = "itself")
    private final Semaphore booleanSemaphore;

    private final OutputServiceThread outputThread;
    private final List<Thread> threads;
    private final HistogramService histogramService;

    public MasterThread(String rootFolder, String fileExtension, Histogram histogram, Service type, double blockingCoefficient, HistogramService histogramService){
       super("MasterThread");
       this.fileExtension = fileExtension;
       this.histogram = histogram;
       this.rootFolder = rootFolder;
       this.outputThread = new OutputServiceThread(type);
       this.threads = new ArrayList<>();
       this.histogramService = histogramService;

       int kernels = Runtime.getRuntime().availableProcessors();
       int maxNumberOfConcurrentThreads = (int) Math.ceil(kernels / (1 - blockingCoefficient));

       if(Service.LOW_LEVEL.equals(type)){
           this.threadSemaphore = new LowLevelSemaphore(maxNumberOfConcurrentThreads);
           this.booleanSemaphore = new LowLevelSemaphore(1);
       } else {
           this.threadSemaphore = new Semaphore(maxNumberOfConcurrentThreads);
           this.booleanSemaphore = new Semaphore(1);
       }
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public Semaphore getThreadSemaphore() {
        return threadSemaphore;
    }

    public Semaphore getBooleanSemaphore() {
        return booleanSemaphore;
    }

    public OutputServiceThread getOutputThread() {
        return outputThread;
    }

    @Override
    public void run(){
        try {
            outputThread.start();
            traverseDirectory(rootFolder);

            for (Thread worker : threads) {
                worker.join();
            }
            outputThread.put(new Message(MessageType.FINISH));
            outputThread.join();

        } catch (IOException io) {
            histogramService.setIoExceptionThrown(true);
            shutDown();
        } catch (InterruptedException exception){
            shutDown();
        }
    }

    /**
     * Scans through the root folder and looks for directories. After the root folder has been fully scanned,
     * the files in it are processed.
     * @param rootFolder folder to scan through
     * @throws IOException if I/O error occurred during processing of the folder
     * @throws InterruptedException if Thread is interrupted
     */
    public void traverseDirectory(String rootFolder) throws IOException, InterruptedException {
        Path folder = Paths.get(rootFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if(Thread.currentThread().isInterrupted()){
                    shutDown();
                    return;
                }
                if (Files.isDirectory(path)){
                    traverseDirectory(path.toString());
                }
            }
        }
        processFilesInFolder(rootFolder);
    }

    /**
     * Starts a Worker to process the files in a given root folder.
     * @param rootFolder folder to process
     * @throws InterruptedException if Thread is interrupted
     */
    private void processFilesInFolder(String rootFolder) throws InterruptedException {
        threadSemaphore.acquire();
        Thread worker = new Worker(rootFolder, this);
        threads.add(worker);
        worker.start();
    }

    /**
     * Interrupts all worker threads that have been started so far as well as the OutputThread.
     */
    public void shutDown() {
        outputThread.interrupt();
        for (Thread worker: threads) {
            worker.interrupt();
        }
    }

    @Override
    public String toString() {
        return "MasterThread";
    }

    public HistogramService getHistogramService(){
        return this.histogramService;
    }
}
