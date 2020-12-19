package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.PrintService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

public class StreamWorker {
    private final int WITHOUT_SUBDIRECTORIES = 1;
    private final String rootDirectory;
    private final Histogram histogram;
    private final PrintService printer;
    private final Predicate<Path> correctFileExtension;

    public StreamWorker(String rootDirectory, String fileExtension) {
        this.rootDirectory = rootDirectory;
        this.histogram = new Histogram();
        this.printer = new PrintService();
        correctFileExtension = (path) -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(fileExtension);
    }

    public Histogram calculateHistogram() {
        //Start printer
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(printer);

        //Traverse root folder and process each subfolder
        try {
            traverseRootDirectory(rootDirectory);
            printer.put(new Message(MessageType.FINISH));
            executor.shutdown();
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException | IOException exception) {
            //Handle executor termination
            exception.printStackTrace();
        }
        return this.histogram;
    }

    private void processDirectory(Path folder) {
        Histogram localHistogram = new Histogram();

        try {
            try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
                localHistogram.setFiles(streamOfPaths
                        .parallel()
                        .filter(Files::isRegularFile)
                        .count());
            }
            try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
                localHistogram.setProcessedFiles(streamOfPaths
                        .parallel()
                        .filter(correctFileExtension)
                        .count());
            }
            try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
                localHistogram.setLines(streamOfPaths
                        .parallel()
                        .filter(correctFileExtension)
                        .map(FileProcessingUtils::getLinesPerFile)
                        .mapToLong(Long::longValue)
                        .sum());
            }
            try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
                localHistogram.setDistribution(streamOfPaths
                        .parallel()
                        .filter(correctFileExtension)
                        .map(FileProcessingUtils::getFileAsLines)
                        .map(FileProcessingUtils::countLettersImperative)
                        .reduce(new long[Histogram.ALPHABET_SIZE], FileProcessingUtils::accumulateDistributions));
            }
            logDirectoryFinished(folder, localHistogram);
            updateGlobalHistogram(localHistogram);
        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private void logDirectoryFinished(Path folder, Histogram localHistogram) throws IOException, InterruptedException {
        try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
            streamOfPaths
                    .parallel()
                    .filter(correctFileExtension)
                    .forEach(path -> {
                        try {
                            printer.put(new Message(MessageType.FILE, path.toString()));
                        } catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }
                    });
        }
        printer.put(new Message(MessageType.FOLDER, folder.toString(), localHistogram));
    }

    private void traverseRootDirectory(String rootDirectory) throws IOException {
        Path folder = Paths.get(rootDirectory);
        try (Stream<Path> streamOfPaths = Files.walk(folder)) {
            streamOfPaths
                    .parallel()
                    .filter(Files::isDirectory)
                    .forEach(this::processDirectory);
            //FIXME: Ist das hier parallel genug?
        }
    }

    private void updateGlobalHistogram(Histogram localHistogram) {
        histogram.setDirectories(histogram.getDirectories() + 1);
        histogram.setFiles(histogram.getFiles() + localHistogram.getFiles());
        histogram.setProcessedFiles(histogram.getProcessedFiles() + localHistogram.getProcessedFiles());
        histogram.setLines(histogram.getLines() + localHistogram.getLines());
        for (int i = 0; i < Histogram.ALPHABET_SIZE; i++) {
            histogram.getDistribution()[i] += localHistogram.getDistribution()[i];
        }
    }
}
