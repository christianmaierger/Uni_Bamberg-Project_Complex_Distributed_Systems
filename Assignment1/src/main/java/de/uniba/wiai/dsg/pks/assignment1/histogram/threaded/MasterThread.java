package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputServiceSequential;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel.HighLevelWorker;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelSemaphore;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelWorker;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

/**
 * Der Master-Thread, der die einzelnen Verzeichnisthreads startet.
 */
public class MasterThread extends Thread{
    private final Histogram histogram;
    private final String rootFolder;
    private final String fileExtension;
    private final Service syncType;
    private final Semaphore semaphore;
    private final OutputServiceSequential out;

    public MasterThread(String rootFolder, String fileExtension, Histogram histogram, Service type, double blockingCoefficient){
       this.fileExtension = fileExtension;
       this.histogram = histogram;
       this.syncType = type;
       this.rootFolder = rootFolder;
       this.out = new OutputServiceSequential();
       int kernels = Runtime.getRuntime().availableProcessors();
       int maxNumberOfThreads = (int) Math.ceil(kernels / (1 - blockingCoefficient));
       if(Service.LOW_LEVEL.equals(type)){
           this.semaphore = new LowLevelSemaphore(maxNumberOfThreads);
       } else {
           this.semaphore = new Semaphore(maxNumberOfThreads);
       }


    }

    @Override
    public void run(){
        try {
            traverseDirectory(rootFolder);
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
                if (Files.isDirectory(path)){
                    traverseDirectory(path.toString());
                }
            }
        } catch (IOException io){
            throw new IOException("I/O error occurred while reading folders and files.");
        }

        processFilesInFolder(rootFolder);

        //TODO: Gracefully interrupt all threads

    }

    private void processFilesInFolder(String rootFolder) throws InterruptedException {
        semaphore.acquire();
        Thread worker;
        if(syncType.equals(Service.LOW_LEVEL)){
            worker = new LowLevelWorker(rootFolder, fileExtension, histogram, out, semaphore);
        } else {
            //TODO: Highlevel
            worker = new HighLevelWorker(rootFolder, fileExtension, histogram, semaphore);
        }
        try {
            worker.start();
            worker.join();
        } catch (InterruptedException | RuntimeException exception ) {
            throw new InterruptedException(exception.getMessage());
        }
    }

    @Override
    public String toString() {
        return "SequentialHistogramService";
    }

}
