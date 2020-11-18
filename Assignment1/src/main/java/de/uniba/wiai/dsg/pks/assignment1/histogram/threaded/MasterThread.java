package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

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
    public void startProcessing(String rootFolder, String fileExtension){

    }

}
