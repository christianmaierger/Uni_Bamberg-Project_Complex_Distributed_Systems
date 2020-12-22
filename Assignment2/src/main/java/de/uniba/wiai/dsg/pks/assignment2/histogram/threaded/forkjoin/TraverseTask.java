package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceRunnable;
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

    @GuardedBy(value ="itself")
    private final String rootFolder;
    @GuardedBy(value ="itself")
    private final String fileExtension;
    private final OutputServiceRunnable outputServiceRunnable;
    private final ExecutorService outputPool;
    private final ForkJoinPool mainPool;

    List<ForkJoinTask<Histogram>> tasksRepresentingEachFolder = new LinkedList<ForkJoinTask<Histogram>>();


    public TraverseTask(String rootFolder, String fileExtension, OutputServiceRunnable outputCallable, ExecutorService outputPool, ForkJoinPool mainPool) {
        this.rootFolder =rootFolder;
        this.fileExtension=fileExtension;
        this.outputServiceRunnable = outputCallable;
        this.outputPool = outputPool;
        this.mainPool = mainPool;
    }


    @Override
    protected Histogram compute() {

        Histogram resultHistogram = new Histogram();

        try {
            //zerteilen
            traverseDirectory(rootFolder);


            if(tasksRepresentingEachFolder.size()>0) {

                for (ForkJoinTask<Histogram> result : tasksRepresentingEachFolder) {

                    Histogram subResult;
                    subResult = result.join();
                    resultHistogram = Utils.addUpAllFields(subResult, resultHistogram);
                }

               // return resultHistogram;

            }
                //if file task berechnen:

                Histogram localHistogram = new Histogram();

                processFiles(localHistogram);
                localHistogram.setDirectories(1);
                logProcessedDirectory(localHistogram);

                // aggregieren
                return resultHistogram = Utils.addUpAllFields(resultHistogram, localHistogram);


        } catch (InterruptedException e) {
            try {
                shutdownPrinter(outputPool);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            try {
                throw e;
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }/* catch (ExecutionException e) {
            try {
                shutdownPrinter(outputPool);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            try {
                throw e;
            } catch (ExecutionException executionException) {
                executionException.printStackTrace();
            }
        } */catch (IOException e) {
            try {
                shutdownPrinter(outputPool);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            try {
                throw e;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    return resultHistogram;
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
                  //  traverseDirectory(path.toString());
                    ForkJoinTask<Histogram> result = processFilesInFolder(path.toString());
                    tasksRepresentingEachFolder.add(result);
                }
            }
        }
    }

    private ForkJoinTask<Histogram> processFilesInFolder(String folder) {

        TraverseTask folderTask = new TraverseTask(folder, fileExtension, outputServiceRunnable, outputPool, mainPool);
        ForkJoinTask<Histogram> result = folderTask.fork();

        return result;
    }


    private void shutdownPrinter(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        outputServiceRunnable.put(new Message(MessageType.FINISH));
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




    private void processFiles(de.uniba.wiai.dsg.pks.assignment.model.Histogram localHistogram) throws IOException, InterruptedException {

        Path folder = Paths.get(rootFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isRegularFile(path)){
                    localHistogram.setFiles(localHistogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect){

                        processFileContent(path, localHistogram);
                        logProcessedFile(path.toString());
                        localHistogram.setProcessedFiles(localHistogram.getProcessedFiles() + 1);
                    }
                }
            }

        }
    }

    private void processFileContent(Path path, de.uniba.wiai.dsg.pks.assignment.model.Histogram localHistogram){


        localHistogram.setLines(Utils.getLinesPerFile(path));

        List<String> lines = Utils.getFileAsLines(path);

        long[] distribution = Utils.countLetters(lines);

        localHistogram.setDistribution(Utils.sumUpDistributions(distribution, localHistogram.getDistribution()));

    }



    private void logProcessedFile(String path) throws InterruptedException {
        Message message = new Message(MessageType.FILE, path);
        outputServiceRunnable.put(message);
    }

    private void logProcessedDirectory(de.uniba.wiai.dsg.pks.assignment.model.Histogram localHistogram) throws InterruptedException {
        Message message = new Message(MessageType.FOLDER, rootFolder, localHistogram);
        outputServiceRunnable.put(message);
    }


}





