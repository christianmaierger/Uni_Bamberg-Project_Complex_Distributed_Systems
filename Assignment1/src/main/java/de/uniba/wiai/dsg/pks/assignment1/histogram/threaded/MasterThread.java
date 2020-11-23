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

   

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        MasterThread masterThread = new MasterThread();
        masterThread.join();
    }

}
