package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.PrintService;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A Callable to process a given root folder and analyses letter distribution as well as some statistical metrics
 * about the processed files and subdirectories.
 */
@ThreadSafe
public class StreamWorker implements Callable<Histogram> {
    private final int WITHOUT_SUBDIRECTORIES = 1;
    private final String rootDirectory;
    private final PrintService printer;
    private final ExecutorService printExecutor;
    private final Predicate<Path> correctFileExtension;
    private volatile boolean interrupted = false;

    /**
     * Create a StreamWorker with the root folder that shall be analyses and the file extension
     * of the files which shall be considered during the analysis.
     * @param rootDirectory String of the path to the root folder whose content shall be analysed
     * @param fileExtension String specifying the kind of file that should be analysed
     */
    public StreamWorker(String rootDirectory, String fileExtension) {
        this.rootDirectory = rootDirectory;
        this.printer = new PrintService();
        this.printExecutor = Executors.newSingleThreadExecutor();
        this.correctFileExtension = (path) -> Files.isRegularFile(path)
                && path.getFileName().toString().endsWith(fileExtension);
    }

    /**
     * Analyses the rootDirectory of this.StreamWorker and returns the result of the analysis as a Histogram.
     * Furthermore, messages to indicate that a files or folder has been processed are printed to console.
     *
     * @return Histogram representing the result of the analysis of this.rootDirectory
     * @throws IOException if an I/O error occurs
     * @throws RuntimeException if an I/O error occurs or the current thread is asked to stop via an interrupt or
     *                          a call to the method stopProcessing()
     */
    @Override
    public Histogram call() throws IOException {
        printExecutor.submit(printer);
        try {
            Histogram histogram = traverseRootDirectory();
            return histogram;
        } catch (IOException exception) {
            throw new IOException("An I/O Exception occurred.");
        } catch (RuntimeException exception) {
            throw new RuntimeException(exception.getMessage(), exception.getCause());
        } finally {
            shutDownPrinter();
        }
    }

    /**
     * Stops the processing and causes the call() method to throw a RuntimeException.
     */
    public void stopProcessing() {
        interrupted = true;
    }

    private Histogram accumulateHistograms(Histogram a, Histogram b) {
        checkForInterrupt();
        Histogram result = new Histogram();
        result.setDirectories(a.getDirectories() + b.getDirectories());
        result.setFiles(a.getFiles() + b.getFiles());
        result.setProcessedFiles(a.getProcessedFiles() + b.getProcessedFiles());
        result.setLines(a.getLines() + b.getLines());
        result.setDistribution(FileUtils.sumUpDistributions(a.getDistribution(), b.getDistribution()));
        return result;
    }

    private void checkForInterrupt() {
        if (interrupted) {
            shutDownPrinter();
            throw new RuntimeException("Execution has been interrupted.");
        }
    }

    private long countFiles(Path folder) throws IOException {
        checkForInterrupt();
        try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
            return (streamOfPaths
                    .parallel()
                    .filter(Files::isRegularFile)
                    .count());
        }
    }

    private long countFilesWithCorrectExtension(Path folder) throws IOException {
        checkForInterrupt();
        try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
            return streamOfPaths
                    .parallel()
                    .filter(correctFileExtension)
                    .count();
        }
    }

    private long[] countDistribution(Path folder) throws IOException {
        checkForInterrupt();
        try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
            return (streamOfPaths
                    .parallel()
                    .filter(correctFileExtension)
                    .map(FileUtils::getFileAsLines)
                    .map(FileUtils::countLetters)
                    .reduce(new long[Histogram.ALPHABET_SIZE], FileUtils::sumUpDistributions));
        }
    }

    private long countLines(Path folder) throws IOException {
        checkForInterrupt();
        try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
            return (streamOfPaths
                    .parallel()
                    .filter(correctFileExtension)
                    .map(FileUtils::getLinesPerFile)
                    .mapToLong(Long::longValue)
                    .sum());
        }
    }

    private void logDirectoryFinished(Path folder, Histogram histogram) throws IOException {
        checkForInterrupt();
        try (Stream<Path> streamOfPaths = Files.walk(folder, WITHOUT_SUBDIRECTORIES)) {
            streamOfPaths
                    .filter(correctFileExtension)
                    .forEach(this::logFile);
        }
        try {
            printer.put(new Message(MessageType.FOLDER, folder.toString(), histogram));
        } catch (InterruptedException exception) {
            checkForInterrupt();
        }
    }

    private void logFile(Path path) {
        try {
            printer.put(new Message(MessageType.FILE, path.toString()));
        } catch (InterruptedException exception) {
            checkForInterrupt();
        }
    }

    private Histogram processDirectory(Path folder) {
        checkForInterrupt();
        Histogram histogram = new Histogram();
        try {
            histogram.setFiles(countFiles(folder));
            histogram.setProcessedFiles(countFilesWithCorrectExtension(folder));
            histogram.setLines(countLines(folder));
            histogram.setDistribution(countDistribution(folder));
            histogram.setDirectories(1);
            logDirectoryFinished(folder, histogram);
        } catch (IOException exception) {
            throw new RuntimeException("An I/O Exception occurred.");
        }
        return histogram;
    }

    private void shutDownPrinter() {
        try {
            printer.put(new Message(MessageType.FINISH));
            printExecutor.shutdown();
            printExecutor.awaitTermination(60, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            printExecutor.shutdownNow();
        }
    }

    private Histogram traverseRootDirectory() throws IOException {
        Histogram histogram;
        Path folder = Paths.get(rootDirectory);
        try (Stream<Path> streamOfPaths = Files.walk(folder)) {
            histogram = streamOfPaths
                    .parallel()
                    .filter(Files::isDirectory)
                    .map(this::processDirectory)
                    .reduce(new Histogram(), this::accumulateHistograms);
        }
        return histogram;
    }
}
