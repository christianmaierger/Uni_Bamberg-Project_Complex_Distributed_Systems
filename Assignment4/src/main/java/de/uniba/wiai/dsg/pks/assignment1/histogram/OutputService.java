package de.uniba.wiai.dsg.pks.assignment1.histogram;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import net.jcip.annotations.NotThreadSafe;

import java.util.Arrays;
import java.util.Formatter;


/**
 * Offers methods to log the finalisation of a processed file or a processed directory to console. The log messages
 * are serially numbered, starting from 1.
 */
@NotThreadSafe
public class OutputService {
    private int lineNumber = 1;

    /**
     * Prints a message to console that tells the user that "directoryPath" has been finished and also outputs the
     * intermediary result of the histogram.
     * @param directoryPath path of the directory that has been processed
     */
    public void logProcessedDirectory(String directoryPath, Histogram histogram){
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("N: %d - ", lineNumber);
        formatter.format("Directory %s finished ", directoryPath);
        formatter.format("[distr = %s, ", Arrays.toString(histogram.getDistribution()));
        formatter.format("lines=%d, ", histogram.getLines());
        formatter.format("files=%d, ", histogram.getFiles());
        formatter.format("processedFiles=%d, ", histogram.getProcessedFiles());
        formatter.format("directories=%d]", histogram.getDirectories());
        System.out.println(stringBuilder);
        lineNumber++;
    }

    /**
     * Prints a message to console that tells the user that "filePath" has been finished.
     * @param filePath path of the file that has been finished
     */
    public void logProcessedFile(String filePath){
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("N: %d - ", lineNumber);
        formatter.format("File %s finished !", filePath);
        System.out.println(stringBuilder);
        lineNumber++;
    }
}
