package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceCallable;
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
    // liste wird ja nur von diesem thread verwendet? ok? besser concurrent Struktur?
    // oder gleich blockingqueue verwenden?

    private final List<Future<Histogram>> listOfFuturesRepresentingEachFolder = new LinkedList<>();
    @GuardedBy(value ="itself")
    private final OutputServiceCallable outputCallable;






    public MasterCallable(ExecutorService masterExcecutor, String rootFolder, String fileExtension) {
        this.executorService= masterExcecutor;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.outputCallable = new OutputServiceCallable();
        this.outputPool = Executors.newSingleThreadExecutor();

    }

    public Histogram call() throws InterruptedException, ExecutionException, IOException {
        //TODO: Suchbereich weiter zerlegen ODER Berechnung durchfuehren
        Histogram resultHistogram = new Histogram();

      outputPool.submit(outputCallable);


      try {
        traverseDirectory(rootFolder);


        for (Future<Histogram> result: listOfFuturesRepresentingEachFolder) {

                Histogram subResult;
                subResult = result.get();
               resultHistogram = Utils.addUpAllFields(subResult, resultHistogram);
            }
          outputCallable.put(new Message(MessageType.FINISH));
          return resultHistogram;
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

    private Future<Histogram> processFilesInFolder(String folder) {

        TraverseFolderCallable folderTask = new TraverseFolderCallable(folder, fileExtension, outputCallable);
        Future<Histogram> result = executorService.submit(folderTask);

        return result;
    }


    private void shutdownPrinter(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        outputCallable.put(new Message(MessageType.FINISH));
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


