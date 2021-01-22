package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.helpers;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Contains static methods to process a folder and its files with regard to letter frequency analysis
 * and file statistics.
 */
@ThreadSafe
public class DirectoryUtils {

    /**
     * Returns the number of lines in the input Path "path"
     * @param path path to file whose numbers shall be counted
     * @return number of lines in the input file
     */
    public static long getLinesPerFile(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
        }
    }

    /**
     * Returns a List<String> representation of the file at the input Path "path".
     * @param path Path to file to convert
     * @return List<String> of input file
     */
    public static List<String> getFileAsLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    /**
     * Counts lower- and uppercase latin alphabet letters in the given input file "file". Returns a long array
     * with 26 fields. The first field holds the number of 'A'/'a' characters in the file, the second field the
     * number of 'B'/'b' characters and so on.
     * @param file List<String> file to count the letters of
     * @return long[] representing the respective occurrence of latin characters
     */
    public static long[] countLetters(List<String> file){
        long[] distribution = new long[26];
        for (String line : file) {
            for (int x = 0; x < line.length(); x++) {
                char character = line.charAt(x);
                if (character >= 'A' && character <= 'Z') {
                    distribution[character - 'A']++;
                }
                if (character >= 'a' && character <= 'z') {
                    distribution[character - 'a']++;
                }
            }
        }
        return distribution;
    }

    /**
     * Adds two long[] arrays of size 26 field by field and returns the resulting long[] array.
     * This can be used to combine two character distributions.
     *
     * @param distributionA first long[] distribution of size 26
     * @param distributionB second long[] distribution of size 26
     * @return long[] array that holds the field-wise addition of the two input arrays
     */
    public static long[] sumUpDistributions(long[] distributionA, long[] distributionB){
        long[] result = new long[Histogram.ALPHABET_SIZE];
        for (int i = 0; i < Histogram.ALPHABET_SIZE; i++) {
            result[i] = distributionA[i] + distributionB[i];
        }
        return result;
    }


    /**
     * Adds up all fields of two histograms and returns a new histogram with their values from all fields added
     * together.
     *
     * @param subResultHistogram a new result as histogram of which the fields should be added on the fields of a given histogram
     * @param oldHistogram the histogram to which the method should add to
     * @return a Histogram holding the addition of the two input Histograms
     */
    public static Histogram addUpAllFields(Histogram subResultHistogram, Histogram oldHistogram) {

        long[] oldHistogramDistribution= oldHistogram.getDistribution();
        long[] newHistogramDistribution= subResultHistogram.getDistribution();

        for(int i=0; i<26 ; i++) {
            oldHistogramDistribution[i]= oldHistogramDistribution[i] + newHistogramDistribution[i];
        }

        Histogram result = new Histogram();
        result.setDistribution(oldHistogramDistribution);
        result.setFiles(oldHistogram.getFiles() + subResultHistogram.getFiles());
        result.setProcessedFiles(oldHistogram.getProcessedFiles() + subResultHistogram.getProcessedFiles());
        result.setDirectories(oldHistogram.getDirectories() + subResultHistogram.getDirectories());
        result.setLines(oldHistogram.getLines() + subResultHistogram.getLines());

        return result;
    }
}
