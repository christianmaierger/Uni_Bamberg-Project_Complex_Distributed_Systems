package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Worker extends Thread {
    private final String rootFolder;
    private final MasterThread masterThread;
    private final Histogram localHistogram;

    public Worker(String rootFolder, MasterThread masterThread){
        this.rootFolder = rootFolder;
        this.masterThread = masterThread;
        this.localHistogram = new Histogram();
    }

    @Override
    public void run() {
        try{
            processFiles();
            updateSharedHistogram();
        } catch (InterruptedException | IOException ignored) {
            //FIXME: Hier bringt es ja gar nichts, die exception irgendwie weiterzugeben, weil es keine thread gibt,
            //der sie catchen könnte, oder? Ist ok für InterruptedException, aber nicht für IO, weil dann kann man ja
            //gar nicht zurückmelden, dass es einen IO-Fehler gab. Dafür evtl über ein Feld mit dem Service kommunizieren?
        } finally{
            masterThread.getThreadSemaphore().release();
        }
    }

    private void processFiles() throws IOException, InterruptedException {
        Path folder = Paths.get(rootFolder);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isRegularFile(path)){
                    localHistogram.setFiles(localHistogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(masterThread.getFileExtension());
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


    private void updateSharedHistogram() throws InterruptedException {
        masterThread.getBooleanSemaphore().acquire();
        try{
            addToNumberOfFiles(localHistogram.getFiles());
            addToNumberOfProcessFiles(localHistogram.getProcessedFiles());
            addToNumberOfLines(localHistogram.getLines());
            for(int x = 0; x < 26; x++){
                masterThread.getHistogram().getDistribution()[x] += localHistogram.getDistribution()[x];
            }
            incrementNumberOfDirectories();
            logProcessedDirectory();
        } finally {
            masterThread.getBooleanSemaphore().release();
        }
    }

    private void processFileContent(List<String> lines){
        // count lines
        long linesInFile = lines.size();
        localHistogram.setLines(localHistogram.getLines() + linesInFile);

        // count letter distribution
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


    private void addToNumberOfFiles(long x){
        masterThread.getHistogram().setFiles(masterThread.getHistogram().getFiles() + x);
    }

    private void addToNumberOfProcessFiles(long x){
        masterThread.getHistogram().setProcessedFiles(masterThread.getHistogram().getProcessedFiles() + x);
    }

    private void addToNumberOfLines(long x){
        masterThread.getHistogram().setLines(masterThread.getHistogram().getLines() + x);
    }

    private void incrementNumberOfDirectories(){
        masterThread.getHistogram().setDirectories(masterThread.getHistogram().getDirectories() + 1);
    }

    private void logProcessedFile(String path) throws InterruptedException {
        Message message = new Message(MessageType.FILE, path);
        masterThread.getOutputThread().put(message);
    }

    private void logProcessedDirectory() throws InterruptedException {
        Message message = new Message(MessageType.FOLDER, rootFolder, masterThread.getHistogram());
        masterThread.getOutputThread().put(message);
    }
}
