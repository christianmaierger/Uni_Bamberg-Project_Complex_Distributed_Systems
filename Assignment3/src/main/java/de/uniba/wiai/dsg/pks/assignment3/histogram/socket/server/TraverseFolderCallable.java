package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
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
    @GuardedBy(value = "itself")
    private final ParseDirectory parseDirectory;
    @GuardedBy(value = "itself")
    private final TCPClientHandler handler;


    /**
     * Creates a TraverseFolderCallable which analyses the files in one specific folder without its subfolders.
     * It only looks at files of a specified file extension. Files that have been processed are logged to console.
     *
     *
     * @param parseDirectory
     * @param handler
     */
    public TraverseFolderCallable(ParseDirectory parseDirectory, TCPClientHandler handler) {
        this.rootFolder = parseDirectory.getPath();
        this.fileExtension = parseDirectory.getFileExtension();
        this.handler = handler;
        this.parseDirectory=parseDirectory;
    }


    // im folgenden kommen die geworfenen ex ja in futures die nie ausgelesen werden, ob das so ideal ist?

    /**
     * Performs a frequency analysis as statistical metrics on the files of type this.fileExtension in this.rootFolder and
     * returns its analysis as a Histogram. Processed files are logged to console.
     *
     * @return Histogram holding the result of the analysis
     * @throws InterruptedException if the current Thread is interrupted
     * @throws IOException if an I/O error occurs
     */
    public Histogram call() throws IOException, InterruptedException {
        Histogram localHistogram = new Histogram();
        processFiles(localHistogram);
        localHistogram.setDirectories(1);

        handler.getSemaphore().acquire();
        Histogram acummulatedHistogram = handler.getSubResultHistogram();
        handler.setSubResultHistogram(Utils.addUpAllFields(localHistogram, acummulatedHistogram));
        handler.getSemaphore().release();

        handler.getServer().putInCache(parseDirectory, localHistogram);
        return localHistogram;
    }

    // das braucht auch eigentlich garnimmer interuptable sein

    // Abfragen ob thread interupted ist


    /**
     * Scans through the root folder and looks for files. Each file found while iterating is then processed
     *
     * @param localHistogram the histogram for one folder that the Callable was created for and that is processed here
     * @throws IOException          if I/O error occurred during processing of the folder and its files
     */
    private void processFiles(Histogram localHistogram) throws IOException {
        Path folder = Paths.get(rootFolder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    localHistogram.setFiles(localHistogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect) {
                        processFileContent(path, localHistogram);
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

}
