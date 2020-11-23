package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

public class LowLevelWorker extends Thread {

    @Override
    public void run() {

        processDirectoryLowLevel(path.toString(), fileExtension);
        incrementNumberOfDirectories();
        out.logProcessedDirectory(path.toString());
    }
}
