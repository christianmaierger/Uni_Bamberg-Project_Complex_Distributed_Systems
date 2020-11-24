package de.uniba.wiai.dsg.pks.assignment1.histogram.shared;

import javax.print.PrintService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadsafeHistogram  {

    private AtomicLong dirCounter = new AtomicLong(0);
    private AtomicLong fileCounter = new AtomicLong(0);
    private AtomicLong processedFileCounter = new AtomicLong(0);
    private AtomicInteger processedLineCounter = new AtomicInteger(0);
    private AtomicInteger printLineCounter = new AtomicInteger(0);
   // private PrintService printService = new PrintService(this);
    private boolean lineEmpty=false;


    public AtomicLong getDirCounter() {
        return dirCounter;
    }

    public void setDirCounter(AtomicLong dirCounter) {
        this.dirCounter = dirCounter;
    }

    public AtomicLong getFileCounter() {
        return fileCounter;
    }

    public void setFileCounter(AtomicLong fileCounter) {
        this.fileCounter = fileCounter;
    }

    public AtomicLong getProcessedFileCounter() {
        return processedFileCounter;
    }

    public void setProcessedFileCounter(AtomicLong processedFileCounter) {
        this.processedFileCounter = processedFileCounter;
    }

    public AtomicInteger getProcessedLineCounter() {
        return processedLineCounter;
    }

    public void setProcessedLineCounter(AtomicInteger processedLineCounter) {
        this.processedLineCounter = processedLineCounter;
    }

    public AtomicInteger getPrintLineCounter() {
        return printLineCounter;
    }

    public void setPrintLineCounter(int printLineCounter) {

        this.printLineCounter = new AtomicInteger(printLineCounter);;
    }

    public boolean isLineEmpty() {
        return lineEmpty;
    }

    public void setLineEmpty(boolean lineEmpty) {
        this.lineEmpty = lineEmpty;
    }




}
