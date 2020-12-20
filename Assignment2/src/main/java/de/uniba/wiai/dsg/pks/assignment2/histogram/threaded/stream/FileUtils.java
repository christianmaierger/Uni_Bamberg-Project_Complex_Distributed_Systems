package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.util.stream.Stream;

public class FileUtils {

    public static long getLinesPerFile(Path path){
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
            //TODO: Problem mit Charset?
        } catch (IOException | UncheckedIOException exception) {
            throw new RuntimeException("An I/O error occurred.");
        }
    }

    public static List<String> getFileAsLines(Path path){
        try {
            return Files.readAllLines(path);
            //TODO: Problem mit Charset?
        } catch (IOException | UncheckedIOException exception) {
            throw new RuntimeException("An I/O error occurred.");
        }
    }

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

    public static long[] sumUpDistributions(long[] distributionA, long[] distributionB){
        long[] result = new long[Histogram.ALPHABET_SIZE];
        for (int i = 0; i < Histogram.ALPHABET_SIZE; i++) {
            result[i] = distributionA[i] + distributionB[i];
        }
        return result;
    }
}