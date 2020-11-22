package de.uniba.wiai.dsg.pks.assignment1.histogram;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.util.Arrays;
import java.util.Formatter;

/**
 * Diese Klasse kümmert sich nur um den Output auf der Konsole. Die kann später
 * hoffentlich einfach in einen Thread gepackt werden.
 */
public class OutputService {
    private int lineNumber = 1;
    Histogram histogram;

    public OutputService(Histogram histogram){
        this.histogram = histogram;
    }

    /**
     * Prints a message to console that tells the user that the input directory has been finished and outputs the
     * intermediary result of the histogram. Also increments the attribute lineNumber of this OutputService.
     * @param directoryPath path of the directory that has been processed
     */
    void logProcessedDirectory(String directoryPath){
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
     * Prints a message to console that tells the user that the input file has been finished. Also increments the
     * attribute lineNumber of this OutputService.
     * @param filePath path of the file that has been finished
     */
    void logProcessedFile(String filePath){
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        formatter.format("N: %d - ", lineNumber);
        formatter.format("File %s finished !", filePath);
        System.out.println(stringBuilder);
        lineNumber++;
    }
}
