package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.Message;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputServiceThreadWrapper;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel.HighLevelWorker;
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
    private final Service syncType;
    private final Semaphore threadSemaphore;
    private final Semaphore booleanSemaphore;
    private final OutputServiceThreadWrapper out;
    private final List<Thread> threads;

    public MasterThread(String rootFolder, String fileExtension, Histogram histogram, Service type, double blockingCoefficient){
       super("MasterThread");
       this.fileExtension = fileExtension;
       this.histogram = histogram;
       this.syncType = type;
       this.rootFolder = rootFolder;
       this.out = new OutputServiceThreadWrapper(type);
       this.threads = new ArrayList<>();

       int kernels = Runtime.getRuntime().availableProcessors();
       int maxNumberOfThreads = (int) Math.ceil(kernels / (1 - blockingCoefficient));

       if(Service.LOW_LEVEL.equals(type)){
           this.threadSemaphore = new LowLevelSemaphore(maxNumberOfThreads);
           this.booleanSemaphore = new LowLevelSemaphore(1);
       } else {
           this.threadSemaphore = new Semaphore(maxNumberOfThreads);
           this.booleanSemaphore = new Semaphore(1);
       }
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public String getRootFolder() {
        return rootFolder;
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

    public OutputServiceThreadWrapper getOut() {
        return out;
    }

    @Override
    public void run(){
        try {
            out.start();
            traverseDirectory(rootFolder);

            //Iterator
            for (Thread worker : threads) {
                worker.join();
            }
            out.put(new Message(MessageType.FINISH));
            out.join();

        } catch (IOException | InterruptedException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * Starts the processing. Starts one thread per directory, but the current number of
     * threads should not be more than the maximal number of threads.
     * @param rootFolder
     */
    public void traverseDirectory(String rootFolder) throws IOException, InterruptedException {
        //look for directories in folder
        Path folder = Paths.get(rootFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if(Thread.currentThread().isInterrupted()){
                    shutDown();
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
        Thread worker = new HighLevelWorker(rootFolder, this);
        threads.add(worker);
        worker.start();
    }

    private void shutDown() throws InterruptedException {
        out.put(new Message(MessageType.FINISH));
        for (Thread worker: threads) {
            worker.interrupt();
        }
    }

    public void removeThreadFromList(Thread thread){
        threads.remove(thread);
    }

    @Override
    public String toString() {
        return "MasterThread";
    }

}
