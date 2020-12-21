package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputService;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class MasterTask<> extends RecursiveTask {

    private final ForkJoinPool forkJoinPool;
    private final OutputService output;
    private final String rootFolder;
    private final String fileExtension;

    private final List<Future<Histogram>> tasks;

    public MasterTask(ForkJoinPool forkJoinPool, String rootFolder, String fileExtension) {
        this.forkJoinPool = forkJoinPool;
        this.rootFolder = rootFolder;
        this.fileExtension = fileExtension;
        this.output = new OutputService();
        this.tasks = new LinkedList<>();
    }

    public MasterTask(List<Future<Histogram>> tasks) {
        this.forkJoinPool = null;
        this.rootFolder = null;
        this.fileExtension = null;
        this.output = new OutputService();
        this.tasks = tasks;
    }

    @Override
    protected Histogram compute() {

        Histogram resultHistogram = new Histogram();

        try {
            traverseDirectory(rootFolder);

            if (tasks.size() > 1) {
                MasterTask left = new MasterTask(tasks.subList(0, tasks.size() / 2));
                MasterTask right = new MasterTask(tasks.subList(tasks.size() / 2 + 1, tasks.size() - 1));
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
            }
        }
        Future<Histogram> result = processFilesInFolder(directory);
        tasks.add(result);
    }

    private Future<Histogram> processFilesInFolder(String folder) {
        MasterTask masterTask = new MasterTask(forkJoinPool, folder, fileExtension);
        Future<Histogram> result = forkJoinPool.submit(masterTask);
        return result;

    }
}
