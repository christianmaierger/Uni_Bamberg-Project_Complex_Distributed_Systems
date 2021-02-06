package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.PrintService;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@ThreadSafe
public class TraverseFolderCallable implements Callable<Histogram> {
    @GuardedBy(value = "itself")
    private final String rootFolder;
    @GuardedBy(value = "itself")
    private final String fileExtension;
    private final PrintService printService;

    /**
     * Creates a TraverseFolderCallable which analyses the files in one specific folder without its subfolders.
     * It only looks at files of a specified file extension. Files that have been processed are logged to console.
     *
     * @param rootFolder folder to search
     * @param fileExtension type of file to look at
     * @param printService which shall take care of the logs to console
     */
    public TraverseFolderCallable(String rootFolder, String fileExtension, PrintService printService) {
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.printService = printService;
    }

    /**
     * Performs a frequency analysis as statistical metrics on the files of type this.fileExtension in this.rootFolder and
     * returns its analysis as a Histogram. Processed files are logged to console.
     *
     * @return Histogram holding the result of the analysis
     * @throws InterruptedException if the current Thread is interrupted
     * @throws IOException if an I/O error occurs
     */
    public Histogram call() throws InterruptedException, IOException {
        Histogram localHistogram = new Histogram();
        processFiles(localHistogram);
        localHistogram.setDirectories(1);
        logProcessedDirectory(localHistogram);
        return localHistogram;
    }

    /**
     * Scans through the root folder and looks for files. Each file found while iterating is then processed and send as
     * Message to the PrintService to be printed.
     *
     * @param localHistogram the histogram for one folder that the Callable was created for and that is processed here
     * @throws IOException          if I/O error occurred during processing of the folder and its files
     * @throws InterruptedException if Thread is interrupted
     */
    private void processFiles(Histogram localHistogram) throws IOException, InterruptedException {
        Path folder = Paths.get(rootFolder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    if(Thread.currentThread().isInterrupted()){
                        throw new InterruptedException("Execution has been interrupted.");
                    }
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

    /**
     * Scans through the lines of a file counting them and calculates the distribution of latin alphabet letters.
     *
     * @param path           the path of a file to be processes
     * @param localHistogram the local Histogram for one folder for which the results are added up
     *                       for all files in that folder
     */
    private void processFileContent(Path path, Histogram localHistogram) {
        localHistogram.setLines(localHistogram.getLines() + Utils.getLinesPerFile(path));
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
