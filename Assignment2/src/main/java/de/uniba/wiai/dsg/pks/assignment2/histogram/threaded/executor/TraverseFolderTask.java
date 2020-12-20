package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceCallable;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

@ThreadSafe
public class TraverseFolderTask implements Callable<Histogram> {
    @GuardedBy(value ="itself")
    private final String rootFolder;
    @GuardedBy(value ="itself")
    private final String fileExtension;
    @GuardedBy(value ="itself")
    private final Histogram localHistogram = new Histogram();
    @GuardedBy(value ="itself")
    private final OutputServiceCallable outputServiceCallable;

    public TraverseFolderTask(ExecutorService executorService, String rootFolder, String fileExtension, OutputServiceCallable outputCallable) {
    this.rootFolder =rootFolder;
    this.fileExtension=fileExtension;
    this.outputServiceCallable = outputCallable;
    }



    public Histogram call() throws InterruptedException, IOException {

        processFiles();
        localHistogram.setDirectories(1);
        logProcessedDirectory();
        return localHistogram;
    }



    private void processFiles() throws IOException, InterruptedException {

        Path folder = Paths.get(rootFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isRegularFile(path)){
                    localHistogram.setFiles(localHistogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect){
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        processFileContent(lines);
                        logProcessedFile(path.toString());
                        localHistogram.setProcessedFiles(localHistogram.getProcessedFiles() + 1);
                    }
                }
            }

        }
    }

    private void processFileContent(List<String> lines){

        long linesInFile = lines.size();
        localHistogram.setLines(localHistogram.getLines() + linesInFile);

        for (String line: lines) {
            countLettersInLine(line);
        }
    }

    private void countLettersInLine(String line){
        for(int x = 0; x < line.length(); x++){
            char character = line.charAt(x);
            int asciiValue = (int) character;

            if(asciiValue >= 'A' && asciiValue <= 'Z'){
                localHistogram.getDistribution()[asciiValue - 'A']++;
            }
            if(asciiValue >= 'a' && asciiValue <= 'z'){
                localHistogram.getDistribution()[asciiValue - 'a']++;
            }
        }
    }



    private void logProcessedFile(String path) throws InterruptedException {
        Message message = new Message(MessageType.FILE, path);
       outputServiceCallable.put(message);
    }

    private void logProcessedDirectory() throws InterruptedException {
        Message message = new Message(MessageType.FOLDER, rootFolder, localHistogram);
        outputServiceCallable.put(message);
    }


}
