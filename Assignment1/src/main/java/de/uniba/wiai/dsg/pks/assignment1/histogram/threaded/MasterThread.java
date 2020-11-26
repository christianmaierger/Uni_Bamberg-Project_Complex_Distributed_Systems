package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.SyncType;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelSemaphore;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelWorker;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Der Master-Thread, der die einzelnen Verzeichnisthreads startet.
 */
public class MasterThread extends Thread{
    private final Histogram histogram;
    private final List<Thread> workerList;
    private final List<String> directoriesToProcess;
    private final String fileExtension;
    private final SyncType syncType;
    private final LowLevelSemaphore semaphore;
    final OutputService out;

    private final Object lock = new Object();

    // Es muss sichergestellt sein, dass es nie mehr current als max Threads gibt.

    public MasterThread(double blockingCoefficient, String fileExtension, SyncType type){
       this.fileExtension = fileExtension;
       this.histogram = new Histogram();
       this.syncType = type;
       this.workerList = new ArrayList<>();
       this.directoriesToProcess = new ArrayList<>();
       int kernels = Runtime.getRuntime().availableProcessors();
       int maxNumberOfThreads = (int) Math.ceil(kernels / (1 - blockingCoefficient));
       this.semaphore = new LowLevelSemaphore(maxNumberOfThreads);
       this.out = new OutputService();
    }


    /**
     * Starts the processing. Starts one thread per directory, but the current number of
     * threads should not be more than the maximal number of threads.
     * @param rootFolder
     */
    public Histogram traverseRootDirectory(String rootFolder) throws IOException {
        //look for directories in folder
        Path folder = Paths.get(rootFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isDirectory(path)){
                    traverseRootDirectory(path.toString());
                }

            }
        } catch (IOException io){
            throw new IOException("I/O error occurred while reading folders and files.");
        }

        //process files in folder
        semaphore.acquire();
        Thread rootWorker = new LowLevelWorker(rootFolder, fileExtension, histogram, this, out, semaphore);
        rootWorker.start();

        //TODO: Master-Thread muss traversieren
        //TODO: Gracefully interrupt all threads
        //TODO: LOGGING am Endo vom Prozessieren
        return histogram;
    }

    @Override
    public String toString() {
        return "SequentialHistogramService";
    }

    public void addDirectory(String path){
        synchronized (lock){
            directoriesToProcess.add(path);
        }

    }

/**    private void createWorkers(String rootFolder, String fileExtension) {
        if (syncType.equals(SyncType.HIGHLEVEL)) {
            //TODO: start High Level here
            for (int i = 0; i < maxNumberOfThreads; i++) {
                Thread worker = new LowLevelWorker(rootFolder, fileExtension, histogram, this);
                workerList.add(worker);
            }
        } else {
            for (int i = 0; i < maxNumberOfThreads; i++) {
                Thread worker = new LowLevelWorker(rootFolder, fileExtension, histogram, this);
                workerList.add(worker);
            }
        }
    }
*/

}
