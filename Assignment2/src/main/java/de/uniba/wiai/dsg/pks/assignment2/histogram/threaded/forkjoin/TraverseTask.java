package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceCallable;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

public class TraverseTask extends RecursiveTask {

    @GuardedBy(value ="itself")
    private final String rootFolder;
    @GuardedBy(value ="itself")
    private final String fileExtension;
    private final OutputServiceCallable outputServiceCallable;


    public TraverseTask(String rootFolder, String fileExtension, OutputServiceCallable outputCallable) {
        this.rootFolder =rootFolder;
        this.fileExtension=fileExtension;
        this.outputServiceCallable = outputCallable;
    }


    @Override
    protected Histogram compute() {

        Histogram localHistogram = new Histogram();

        processFiles(localHistogram);
        localHistogram.setDirectories(1);
        logProcessedDirectory(localHistogram);
        return localHistogram;
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
        outputServiceCallable.put(message);
    }

    private void logProcessedDirectory(de.uniba.wiai.dsg.pks.assignment.model.Histogram localHistogram) throws InterruptedException {
        Message message = new Message(MessageType.FOLDER, rootFolder, localHistogram);
        outputServiceCallable.put(message);
    }


}




}
