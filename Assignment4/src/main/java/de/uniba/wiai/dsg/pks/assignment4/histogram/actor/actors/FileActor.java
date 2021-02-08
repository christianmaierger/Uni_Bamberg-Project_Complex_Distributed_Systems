package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileActor extends AbstractActor {


    static Props props() {
        return Props.create(FileActor.class, () -> new FileActor());
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FileMessage.class, this::processFile)
                .matchAny(this::handleUnknownMessage)
                .build();
    }

    private void processFile(FileMessage message) throws IOException {
        Path filePath = message.getPath();
        Histogram histogram = new Histogram();
        try {
            histogram.setLines(histogram.getLines() + getLinesPerFile(filePath));
            List<String> lines = getFileAsLines(filePath);
            long[] distribution = countLetters(lines);
            histogram.setFiles(histogram.getFiles()+1);
            histogram.setDistribution(sumUpDistributions(distribution, histogram.getDistribution()));
            ReturnResult fileResult = new ReturnResult(histogram, filePath);
            getSender().tell(fileResult, getSelf());
            message.getOutputActor().tell(new LogMessage(histogram, filePath.toString(), LogMessageType.FILE), getSelf());
        } catch (IOException e) {
            ExeptionMessage exeptionMessage = new ExeptionMessage(e, filePath);
            getSender().tell(exeptionMessage, getSelf());
            throw e;
        }
    }

    private void handleUnknownMessage(Object unknownMessage) {
        UnknownMessage message = new UnknownMessage(unknownMessage.getClass().toString());
        //outputActor.tell(message, getSelf());
        //TODO: Hier können wir gar nicht loggen, dass eine unknown Message kam
    }

    /**
     * Returns the number of lines in the input Path "path"
     *
     * @param path path to file whose numbers shall be counted
     * @return number of lines in the input file
     */
    private long getLinesPerFile(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
        }
    }

    /**
     * Returns a List<String> representation of the file at the input Path "path".
     *
     * @param path Path to file to convert
     * @return List<String> of input file
     */
    private List<String> getFileAsLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }

    /**
     * Counts lower- and uppercase latin alphabet letters in the given input file "file". Returns a long array
     * with 26 fields. The first field holds the number of 'A'/'a' characters in the file, the second field the
     * number of 'B'/'b' characters and so on.
     *
     * @param file List<String> file to count the letters of
     * @return long[] representing the respective occurrence of latin characters
     */
    private long[] countLetters(List<String> file) {
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
    private long[] sumUpDistributions(long[] distributionA, long[] distributionB) {
        long[] result = new long[Histogram.ALPHABET_SIZE];
        for (int i = 0; i < Histogram.ALPHABET_SIZE; i++) {
            result[i] = distributionA[i] + distributionB[i];
        }
        return result;
    }
}
