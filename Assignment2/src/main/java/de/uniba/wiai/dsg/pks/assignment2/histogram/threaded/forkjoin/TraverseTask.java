package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.PrintService;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TraverseTask extends RecursiveTask<Histogram> {

    @GuardedBy(value = "itself")
    private final String rootFolder;
    @GuardedBy(value = "itself")
    private final String fileExtension;
    private final PrintService printService;
    private final ExecutorService outputPool;
    private final ForkJoinPool mainPool;
    private final List<ForkJoinTask<Histogram>> tasksRepresentingEachFolder = new LinkedList<>();

    public TraverseTask(String rootFolder, String fileExtension, PrintService outputCallable, ExecutorService outputPool, ForkJoinPool mainPool) {
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.printService = outputCallable;
        this.outputPool = outputPool;
        this.mainPool = mainPool;
    }


    @Override
    protected Histogram compute() {
        try {
            traverseDirectory(rootFolder);
            Histogram localHistogram = new Histogram();
            processFiles(localHistogram);
            localHistogram.setDirectories(1);
            logProcessedDirectory(localHistogram);

            Histogram resultHistogram = new Histogram();
            for (ForkJoinTask<Histogram> result : tasksRepresentingEachFolder) {
                checkForInterrupt();
                Histogram subResult;
                subResult = result.join();
                resultHistogram = Utils.addUpAllFields(subResult, resultHistogram);
            }
            resultHistogram = Utils.addUpAllFields(resultHistogram, localHistogram);
            return resultHistogram;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private void traverseDirectory(String currentFolder) throws IOException {
        Path folder = Paths.get(currentFolder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                checkForInterrupt();
                if (Files.isDirectory(path)) {
                    TraverseTask folderTask = new TraverseTask(path.toString(), fileExtension, printService, outputPool, mainPool);
                    ForkJoinTask<Histogram> result = folderTask.fork();
                    tasksRepresentingEachFolder.add(result);
                }
            }
        }
    }

    private void checkForInterrupt() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException("Execution has been interrupted.");
        }
    }

    private void processFiles(Histogram localHistogram) throws IOException, InterruptedException {
        Path folder = Paths.get(rootFolder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    checkForInterrupt();
                    localHistogram.setFiles(localHistogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect) {
                        processFileContent(path, localHistogram);
                        logProcessedFile(path.toString());
                        localHistogram.setProcessedFiles(localHistogram.getProcessedFiles() + 1);
                    }
                }
            }

        }
    }

    private void processFileContent(Path path, Histogram localHistogram) {
        localHistogram.setLines(Utils.getLinesPerFile(path));
        List<String> lines = Utils.getFileAsLines(path);
        long[] distribution = Utils.countLetters(lines);
        localHistogram.setDistribution(Utils.sumUpDistributions(distribution, localHistogram.getDistribution()));
    }

    private void logProcessedFile(String path) throws InterruptedException {
        Message message = new Message(MessageType.FILE, path);
        printService.put(message);
    }

    private void logProcessedDirectory(Histogram localHistogram) throws InterruptedException {
        Message message = new Message(MessageType.FOLDER, rootFolder, localHistogram);
        printService.put(message);
    }
}





