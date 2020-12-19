package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileProcessingUtils {

    public static long getLinesPerFile(Path path){
        try (Stream<String> lines = Files.lines(path)) {
            return lines.count();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        //TODO: der return ist kacke
        return 0L;
    }

    public static List<String> getFileAsLines(Path path){
        try {
            return Files.readAllLines(path);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static long[] countLettersImperative(List<String> file){
        long[] distribution = new long[26];
        for (String line : file) {
            for (int x = 0; x < line.length(); x++) {
                char character = line.charAt(x);
                int asciiValue = (int) character;
                if (asciiValue >= 'A' && asciiValue <= 'Z') {
                    distribution[asciiValue - 'A']++;
                }
                if (asciiValue >= 'a' && asciiValue <= 'z') {
                    distribution[asciiValue - 'a']++;
                }
            }
        }
        return distribution;
    }

    public static long[] accumulateDistributions(long[] distributionA, long[] distributionB){
        long[] result = new long[Histogram.ALPHABET_SIZE];
        for (int i = 0; i < Histogram.ALPHABET_SIZE; i++) {
            result[i] = distributionA[i] + distributionB[i];
        }
        return result;
    }
}
