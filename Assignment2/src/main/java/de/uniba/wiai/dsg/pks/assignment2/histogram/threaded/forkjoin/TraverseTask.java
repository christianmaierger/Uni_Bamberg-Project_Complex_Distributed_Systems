package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.PrintService;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class TraverseTask extends RecursiveTask<Histogram> {

    private final ForkJoinPool forkJoinPool;
    private final PrintService output;
    private final String rootFolder;
    private final String fileExtension;

    private final List<Future<Histogram>> tasks;

    public TraverseTask(ForkJoinPool forkJoinPool, String rootFolder, String fileExtension) {
        this.forkJoinPool = forkJoinPool;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.output = new PrintService();
        this.tasks = new LinkedList<>();
    }

    public TraverseTask(List<Future<Histogram>> tasks) {
        this.forkJoinPool = null;
        this.rootFolder = null;
        this.fileExtension = null;
        this.output = new PrintService();
        this.tasks = tasks;
    }

    @Override
    protected Histogram compute() {

        Histogram resultHistogram = new Histogram();

        try {
            traverseDirectory(rootFolder);

            if (tasks.size() > 1) {
                TraverseTask left = new TraverseTask(tasks.subList(0, tasks.size() / 2));
                TraverseTask right = new TraverseTask(tasks.subList(tasks.size() / 2 + 1, tasks.size() - 1));
                left.fork();
                right.fork();
                resultHistogram = Utils.addUpAllFields(left.compute(), right.compute());
            } else {
                resultHistogram = Utils.addUpAllFields(tasks.get(0).get(), new Histogram());
            }
            return resultHistogram;
        } catch (IOException | InterruptedException | ExecutionException e) {

        }

        return null;

    }

    private void traverseDirectory(String directory) throws IOException {
        Path folder = Paths.get(directory);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for(Path path : stream) {
                if(Thread.currentThread().isInterrupted()) {

                }
                //TraverseTask traverseTask = new TraverseTask(forkJoinPool, folder, fileExtension);
                //Directory -> new TraverseTask, der guckt alle an und sucht neue dirs
            }
        }
        //files angucken --> processFiles
        Future<Histogram> result = processFilesInFolder(directory);
        tasks.add(result);
    }

    private Future<Histogram> processFilesInFolder(String folder) {
        TraverseTask traverseTask = new TraverseTask(forkJoinPool, folder, fileExtension);
        Future<Histogram> result = forkJoinPool.submit(traverseTask);
        return result;

    }

    //processFiles
    //Files abarbeiten und loggen
    //Directory fertig loggen
}