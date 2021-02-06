package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.FileMessage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileActor extends AbstractActor {



    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FileMessage.class, this::processFile)
                .build();
    }

    private <P> void processFile(FileMessage message) {
       Path filePath= message.getPath();
       // fraglich ob neues hist ok, oder vielleicht das aus FolderActor?
        Histogram histogram = new Histogram();

        //todo ex handling
        try {
            processFileContent(filePath, histogram);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFileContent(Path path, Histogram histogram) throws IOException {
        histogram.setLines(histogram.getLines() + getLinesPerFile(path));
        List<String> lines = getFileAsLines(path);
        long[] distribution = countLetters(lines);
        histogram.setDistribution(sumUpDistributions(distribution, histogram.getDistribution()));
    }

    /**
     * Returns the number of lines in the input Path "path"
     * @param path path to file whose numbers shall be counted
     * @return number of lines in the input file
     */
    public static long getLinesPerFile(Path path){
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
        } catch (IOException | UncheckedIOException exception) {
            throw new RuntimeException("An I/O error occurred.");
        }
    }

    /**
     * Returns a List<String> representation of the file at the input Path "path".
     * @param path Path to file to convert
     * @return List<String> of input file
     */
    public static List<String> getFileAsLines(Path path){
        try {
            return Files.readAllLines(path);
        } catch (IOException | UncheckedIOException exception) {
            throw new RuntimeException("An I/O error occurred.");
        }
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





}
