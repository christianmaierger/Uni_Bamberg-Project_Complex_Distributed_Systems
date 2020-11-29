package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelSemaphore;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Der Master-Thread, der die einzelnen Verzeichnisthreads startet.
 */
public class MasterThread extends Thread{
    private final Histogram histogram;
    private final String rootFolder;
    private final String fileExtension;
    private final Semaphore threadSemaphore;
    private final Semaphore booleanSemaphore;
    private final OutputServiceThread outputThread;
    private final List<Thread> threads;

    public MasterThread(String rootFolder, String fileExtension, Histogram histogram, Service type, double blockingCoefficient){
       super("MasterThread");
       this.fileExtension = fileExtension;
       this.histogram = histogram;
       this.rootFolder = rootFolder;
       this.outputThread = new OutputServiceThread(type);
       this.threads = new ArrayList<>();

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

        } catch (IOException | InterruptedException ignored1) {
            shutDown();
        }
    }

    /**
     * Starts the processing. Starts one thread per directory, but the current number of
     * threads should not be more than the maximal number of threads.
     * @param rootFolder
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

    private void processFilesInFolder(String rootFolder) throws InterruptedException {
        threadSemaphore.acquire();
        Thread worker = new Worker(rootFolder, this);
        threads.add(worker);
        worker.start();
    }

    private void shutDown() {
        outputThread.interrupt();
        for (Thread worker: threads) {
            worker.interrupt();
        }
    }

    @Override
    public String toString() {
        return "MasterThread";
    }
}
