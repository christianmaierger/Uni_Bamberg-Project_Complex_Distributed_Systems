package de.uniba.wiai.dsg.pks.assignment.shared;

import de.uniba.wiai.dsg.pks.assignment1.histogram.sequential.SequentialHistogramService;

import java.util.Arrays;

public class PrintService {

    SequentialHistogramService histService;

    public PrintService(SequentialHistogramService histService) {
        this.histService = histService;
    }

    public void printDirectoryProcessed(String directory) {
        histService.setPrintLineCounter(histService.getPrintLineCounter().incrementAndGet());
        int index = directory.lastIndexOf('\\');
        String dir = directory.substring(0,index);
        index = directory.lastIndexOf("data");
        dir = directory.substring(index);
        dir= "...\\"+dir;
        System.out.println("N:"+ histService.getPrintLineCounter() + "- Directory " + dir + " finished \n" + "[distr=" +
                Arrays.toString(histService.getHistogram().getDistribution())+", \n"+  "lines=" +
                histService.getHistogram().getLines() + ", files=" +
                histService.getHistogram().getFiles() +  ", processedFiles=" + histService.getHistogram().getProcessedFiles() +
                ", directories=" + histService.getHistogram().getDirectories() + "]");
    }

    public void printFileProcessed(String path) {
        int index = path.lastIndexOf("data");
        String dir = path.substring(index);
        dir= "...\\"+dir;
        histService.setPrintLineCounter(histService.getPrintLineCounter().incrementAndGet());
        System.out.println("N:"+ histService.getPrintLineCounter() + "- File " + dir + " finished !");
    }

}
