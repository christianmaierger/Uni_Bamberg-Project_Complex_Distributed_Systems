package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.SyncType;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel.HighlevelHistogramService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowLevelWorker;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowlevelHistogramService;

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
    // I think we better use the histogram field of LowLevelService, but tut says better do it all here
    private Histogram histogram = new Histogram();
    private int currentNumberOfThreads;
    private int maxNumberOfThreads;
    private SyncType type;
    String rootFolder;
    String fileExtension;
   LowlevelHistogramService histogramLowLevelService;
   HighlevelHistogramService histogramHighLevelService;
   private final OutputService out;
   private final Object lock = new Object();

    // Es muss sichergestellt sein, dass es nie mehr current als max Threads gibt.


    public MasterThread(double blockingCoefficient, SyncType type, String rootFolder, String fileExtension){
       this.currentNumberOfThreads = 0;
       int kernels = Runtime.getRuntime().availableProcessors();
     //  this.maxNumberOfThreads = kernels / (1 - blockingCoefficient);
       this.type=type;
       this.rootFolder = rootFolder;
       this.fileExtension = fileExtension;
       // make to cases to make histogramService high or low level according to enum
       if (type.equals(SyncType.LOWLEVEL)){
          histogramLowLevelService = new LowlevelHistogramService();

       } else {
          histogramHighLevelService = new HighlevelHistogramService();
       }
        out = new OutputService(histogram);
    }

    public synchronized OutputService getOut() {
        return out;
    }

    public synchronized Histogram getHistogram() {
        return histogram;
    }

    /**
     * Starts the processing. Starts one thread per directory, but the current number of
     * threads should not be more than the maximal number of threads.
     * @param rootFolder
     * @param fileExtension
     */
    public void startProcessing(String rootFolder, String fileExtension) throws HistogramServiceException{

       // histogramService.incrementNumberOfDirectories();

        if(type.equals(SyncType.LOWLEVEL)){
            incrementNumberOfDirectories();

            try {
                processDirectoryLowLevel(rootFolder, fileExtension);
            } catch (InterruptedException | IOException e) {
              throw new HistogramServiceException(e.getMessage());
            }

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
    public void processDirectoryLowLevel(String rootDirectory, String fileExtension) throws InterruptedException, IOException {
        Path folder = Paths.get(rootDirectory);
        LowLevelWorker mainWorker = new LowLevelWorker(this, rootDirectory, fileExtension);
        mainWorker.start();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                System.out.println("Pfad ist:  "+ path);
                if(Thread.currentThread().isInterrupted()){
                    throw new InterruptedException("Execution has been interrupted.");
                }
                if (Files.isDirectory(path) && !path.toString().equals(rootDirectory)){
                    LowLevelWorker worker = new LowLevelWorker(this, path.toString(), fileExtension);
                    worker.start();
                    incrementNumberOfDirectories();
                    //worker.join();
                    processDirectoryLowLevel(path.toString(), fileExtension);
                    out.logProcessedDirectory(path.toString());

                } else if (Files.isRegularFile(path)){
                    // denke dafür hier nichts machen
                }
            }
            mainWorker.join();
        } catch (IOException io) {
            throw new IOException( "I/O error occurred while reading folders and files.");
        }

    }










    public void incrementNumberOfFiles() {
        synchronized (lock) {
            histogram.setFiles(histogram.getFiles() + 1);
        }
    }

    public void incrementNumberOfProcessedFiles() {
        synchronized (lock) {
            histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
        }
    }

    public void addToNumberOfLines(int x) {
        synchronized (lock) {
            histogram.setLines(histogram.getLines() + x);
        }
    }

    public void incrementNumberOfDirectories() {
        synchronized (lock) {
            histogram.setDirectories(histogram.getDirectories() + 1);
        }
    }

    public void incrementDistributionAtX(int x){
        synchronized (lock) {
            histogram.getDistribution()[x]++;
        }
    }




    @Override
    public void run() {
        try {
            startProcessing(rootFolder, fileExtension);
        } catch (HistogramServiceException e) {
            // TO DO
        }

    }



}
