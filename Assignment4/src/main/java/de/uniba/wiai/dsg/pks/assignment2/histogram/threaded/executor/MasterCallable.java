package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.PrintService;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;


@ThreadSafe
public class MasterCallable implements Callable<Histogram> {

    private final ExecutorService executorService;
    private final ExecutorService outputPool;
    @GuardedBy(value = "itself")
    private final String rootFolder;
    @GuardedBy(value = "itself")
    private final String fileExtension;
    private final List<Future<Histogram>> listOfFuturesRepresentingEachFolder = new LinkedList<>();
    private final PrintService printService;

    /**
     * Creates a MasterCallable which can be used to perform a frequency analysis on all the files in one
     * folder with a specific file extension.
     * @param masterExecutor Thread Pool in which the MasterCallable is run
     * @param rootFolder folder to search
     * @param fileExtension file extension to look for
     */
    public MasterCallable(ExecutorService masterExecutor, String rootFolder, String fileExtension) {
        this.executorService = masterExecutor;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.printService = new PrintService();
        this.outputPool = Executors.newSingleThreadExecutor();
    }

    /**
     * Analyses the rootDirectory of this MasterCallable and returns the result of the analysis as a Histogram.
     * Furthermore, messages to indicate that a file or folder has been processed are printed to console.
     *
     * @return Histogram representing the result of the analysis of this.rootDirectory
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the this Thread is interrupted
     * @throws ExecutionException if any other exception occurs within the calculations of subclasses
     */
    public Histogram call() throws InterruptedException, ExecutionException, IOException {
        Histogram resultHistogram = new Histogram();
        outputPool.submit(printService);

        try {
            traverseDirectory(rootFolder);
            for (Future<Histogram> result : listOfFuturesRepresentingEachFolder) {
                if(Thread.currentThread().isInterrupted()){
                    throw new InterruptedException("Execution has been interrupted.");
                }
                Histogram subResult;
                subResult = result.get();
                resultHistogram = Utils.addUpAllFields(subResult, resultHistogram);
            }
            return resultHistogram;
        } finally{
            shutdownPrinter(outputPool);
        }
    }

    /**
     * Scans through the root folder and looks for directories. After the root folder has been fully scanned,
     * the files in it are processed.
     *
     * @param currentFolder folder to scan through
     * @throws IOException          if I/O error occurred during processing of the folder
     * @throws InterruptedException if Thread is interrupted
     */
    private void traverseDirectory(String currentFolder) throws IOException, InterruptedException {
        Path folder = Paths.get(currentFolder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (Thread.currentThread().isInterrupted()) {
                    shutdownPrinter(outputPool);
                    throw new InterruptedIOException();
                }
                if (Files.isDirectory(path)) {
                    traverseDirectory(path.toString());
                }
            }
        }
        Future<Histogram> result = processFilesInFolder(currentFolder);
        listOfFuturesRepresentingEachFolder.add(result);
    }


    /**
     * Starts a Worker to process the files in a given root folder.
     *
     * @param folder folder to process
     * @return a Future of the resulting Histogram
     */
    private Future<Histogram> processFilesInFolder(String folder) {
        TraverseFolderCallable folderTask = new TraverseFolderCallable(folder, fileExtension, printService);
        Future<Histogram> result = executorService.submit(folderTask);
        return result;
    }


    /**
     * This method is called to ensure an orderly shutdown of the PrintService by sending it a termination message
     * and shutting down the pool in which it is executed according to Java API.
     *
     * @param executor the Thread Pool to shutdown
     * @throws InterruptedException if Thread is interrupted
     */
    private void shutdownPrinter(ExecutorService executor) throws InterruptedException {
        printService.put(new Message(MessageType.FINISH));
        executor.shutdown();
        try {
            if (!executor.awaitTermination(120, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(120, TimeUnit.MILLISECONDS)) {
                    System.err.println("Output pool did not terminate.");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}