package de.uniba.wiai.dsg.pks.assignment1.histogram.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.util.LinkedList;

public class OutputThread extends Thread {
    private Histogram histogram;
    private final Object lock;
    private boolean poisonPill = false;
    private boolean lastUpdatePrinted = true;
    private MasterThread masterThread;
    private OutputService out;
    private String currentDirectory = "";
    private LinkedList<Object> histogramCounterList = new LinkedList<>();

    public OutputThread(Object lock, MasterThread masterThread, Histogram histogram) {
        this.lock = lock;
        this.histogram = histogram;
        this.masterThread=masterThread;
    }


    public LinkedList<Object> getHistogramCounterList() {
        return histogramCounterList;
    }

    public void setHistogramCounterList(LinkedList<Object> histogramCounterList) {
        this.histogramCounterList = histogramCounterList;
    }

    public void updateHistogramCounterList (Object obj) {
       // setHistogramCounterList(getHistogramCounterList().add(in));
        histogramCounterList.add(obj);
    }


    public boolean getLastUpdatePrinted() {
        return lastUpdatePrinted;
    }

    public void setLastUpdatePrinted(boolean lastUpdatePrinted) {
        this.lastUpdatePrinted = lastUpdatePrinted;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public boolean getPoisonPill() {
        return poisonPill;
    }

    public void setPoisonPill(boolean poisonPill) {
        this.poisonPill = poisonPill;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void setHistogram(Histogram histogram) {
        this.histogram = histogram;
    }

    public synchronized void updateHistogram(Histogram histogram) {
        this.setHistogram(histogram);
    }

    @Override
    public void run() {
       out = new OutputService(histogram);

        for ( Object histogramCounter: histogramCounterList) {



            synchronized (lock) {


              while (getLastUpdatePrinted()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

                out.logProcessedDirectory(getCurrentDirectory());
              setLastUpdatePrinted(true);
            }
        }
    }

}
