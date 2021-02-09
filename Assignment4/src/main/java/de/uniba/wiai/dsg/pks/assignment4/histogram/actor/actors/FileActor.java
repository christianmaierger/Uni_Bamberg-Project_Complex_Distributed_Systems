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
        return Props.create(FileActor.class, FileActor::new);
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
            List<String> lines = getFileAsLines(filePath);
            long[] distribution = countLetters(lines);
            histogram.setDistribution(distribution);
            histogram.setLines(getLinesPerFile(filePath));
            histogram.setFiles(1);
            histogram.setProcessedFiles(1);
            ReturnResult fileResult = new ReturnResult(histogram, filePath);
            getSender().tell(fileResult, getSelf());
        } catch (IOException e) {
            ExceptionMessage exceptionMessage = new ExceptionMessage(e, filePath);
            getSender().tell(exceptionMessage, getSelf());
            throw e;
        }
    }

    private void handleUnknownMessage(Object unknownMessage) {
        throw new IllegalArgumentException(unknownMessage.getClass().getSimpleName());
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
}
