package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceRunnable;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import net.jcip.annotations.GuardedBy;
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

    @GuardedBy(value ="itself")
    private final ExecutorService executorService;
    @GuardedBy(value ="itself")
    private final ExecutorService outputPool;
    @GuardedBy(value ="itself")
    private final String rootFolder;
    @GuardedBy(value ="itself")
    private final String fileExtension;


    private final List<Future<Histogram>> listOfFuturesRepresentingEachFolder = new LinkedList<>();
    @GuardedBy(value ="itself")
    private final OutputServiceRunnable outputRunnable;



    public MasterCallable(ExecutorService masterExcecutor, String rootFolder, String fileExtension) {
        this.executorService= masterExcecutor;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.outputRunnable = new OutputServiceRunnable();
        this.outputPool = Executors.newSingleThreadExecutor();

    }

    public Histogram call() throws InterruptedException, ExecutionException, IOException {

        Histogram resultHistogram = new Histogram();
        outputPool.submit(outputRunnable);

      try {
        traverseDirectory(rootFolder);
        for (Future<Histogram> result: listOfFuturesRepresentingEachFolder) {
                Histogram subResult;
                subResult = result.get();
               resultHistogram = Utils.addUpAllFields(subResult, resultHistogram);
            }
          outputRunnable.put(new Message(MessageType.FINISH));
          return resultHistogram;

          // schöner collapsen oder?
      } catch (InterruptedException e) {
          shutdownPrinter(outputPool);
          throw e;
      } catch (ExecutionException e) {
          shutdownPrinter(outputPool);
          throw e;
      } catch (IOException e) {
          shutdownPrinter(outputPool);
          throw e;
      }
    }

    /**
     * Scans through the root folder and looks for directories. After the root folder has been fully scanned,
     * the files in it are processed.
     * @param currentFolder folder to scan through
     * @throws IOException if I/O error occurred during processing of the folder
     * @throws InterruptedException if Thread is interrupted
     */
    private void traverseDirectory(String currentFolder) throws IOException, InterruptedException {
        Path folder = Paths.get(currentFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if(Thread.currentThread().isInterrupted()){
                   shutdownPrinter(outputPool);
                    throw new InterruptedIOException();
                }
                if (Files.isDirectory(path)){
                    traverseDirectory(path.toString());
                }
            }
        }
        Future<Histogram> result = processFilesInFolder(currentFolder);
        listOfFuturesRepresentingEachFolder.add(result);
    }


    /**
     * Starts a Worker to process the files in a given root folder.
     * @param folder folder to process
     * @throws InterruptedException if Thread is interrupted
     */
    private Future<Histogram> processFilesInFolder(String folder) {
        TraverseFolderCallable folderTask = new TraverseFolderCallable(folder, fileExtension, outputRunnable);
        Future<Histogram> result = executorService.submit(folderTask);
        return result;
    }


    /**
     * If during execution any exeption ooccures, may it be by interupption or otherwise, this method is called to ensure
     * an orderly shutdown of the OutputServiceRUnnable by sending it a termination message and shutting down the pool
     * in which it is executed according to Java API.
     * @param executor the Threadpool to shutdown
     * @throws InterruptedException if Thread is interrupted
     */
    private void shutdownPrinter(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        outputRunnable.put(new Message(MessageType.FINISH));
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.MILLISECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    }


