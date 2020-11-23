package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.SyncType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Der Master-Thread, der die einzelnen Verzeichnisthreads startet.
 */
public class MasterThread extends Thread{
    private Histogram histogram = new Histogram();
    private int currentNumberOfThreads;
    private int maxNumberOfThreads;

    // Es muss sichergestellt sein, dass es nie mehr current als max Threads gibt.


    public MasterThread(int blockingCoefficient){
       this.currentNumberOfThreads = 0;
       int kernels = Runtime.getRuntime().availableProcessors();
       this.maxNumberOfThreads = kernels / (1 - blockingCoefficient);
    }

    /**
     * Starts the processing. Starts one thread per directory, but the current number of
     * threads should not be more than the maximal number of threads.
     * @param rootFolder
     * @param fileExtension
     */
    public void startProcessing(String rootFolder, String fileExtension, SyncType type){
        if(type.equals(SyncType.HIGHLEVEL)){

        } else {

        }

    }

    @Override
    public String toString() {
        return "SequentialHistogramService";
    }

    /**
     * Scans a directory with the given Code Snippet 2 from the Assignment sheet and
     * starts the processing of either directories by calling this method again or the
     * processing of a file by calling method fileprocessing.
     * Increments the number of processed directories by one and also calls the log-method for finished
     * directories. Also increments the number of files in the histogram (just files, not processed files).
     * The number of processed files is considered in the processFile method.
     *
     * @param rootDirectory
     * @param fileExtension
     */
    private void processDirectoryLowLevel(String rootDirectory, String fileExtension) throws InterruptedException, IOException {
        Path folder = Paths.get(rootDirectory);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if(Thread.currentThread().isInterrupted()){
                    throw new InterruptedException("Execution has been interrupted.");
                }
                if (Files.isDirectory(path)){
                   
                } else if (Files.isRegularFile(path)){
                    incrementNumberOfFiles();
                    if (path.getFileName().toString().endsWith(fileExtension)){
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        processFile(lines);
                        out.logProcessedFile(path.toString());
                    }
                }
            }
        } catch (IOException io){
            throw new IOException( "I/O error occurred while reading folders and files.");
        }

    }

}
