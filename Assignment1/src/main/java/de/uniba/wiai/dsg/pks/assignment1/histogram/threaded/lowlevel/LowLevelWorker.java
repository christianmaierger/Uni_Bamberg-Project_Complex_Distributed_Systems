package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.Message;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class LowLevelWorker extends Thread {
    private final String rootFolder;
    private final MasterThread masterThread;
    private final Object lock = new Object();

    public LowLevelWorker(String rootFolder, MasterThread masterThread){
        this.rootFolder = rootFolder;
        this.masterThread = masterThread;
    }

    @Override
    public void run() {
        try{
            processFilesInDirectory();
            logFinishedProcessing();
        } catch(InterruptedException | IOException exception){
            throw new RuntimeException(exception.getMessage());
        }
        masterThread.getThreadSemaphore().release();
    }

    public String toString(){
        return "LowLevelWorker";
    }

    private void logFinishedProcessing() throws InterruptedException {

            incrementNumberOfDirectories();
            logProcessedDirectory();

    }

    private void processFilesInDirectory() throws InterruptedException, IOException {
        Path folder = Paths.get(masterThread.getRootFolder());
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isRegularFile(path)){

                    //count
                    Histogram countHistogram = new Histogram();
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(masterThread.getFileExtension());
                    if (fileExtensionCorrect){
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        countHistogram = processFileContent(lines);
                    }

                    //update
                    updateResultsInHistogram(countHistogram, fileExtensionCorrect, path);
                }
            }
        }
    }

    private void updateResultsInHistogram(Histogram updateHistogram, boolean correctFileExtension, Path path) throws InterruptedException {
        synchronized (lock){
            incrementNumberOfFiles();
            if(correctFileExtension){
                incrementNumberOfProcessedFiles();
                addToNumberOfLines(updateHistogram.getLines());
                for(int x = 0; x < 26; x++){
                    masterThread.getHistogram().getDistribution()[x] += updateHistogram.getDistribution()[x];
                }
                logProcessedFile(path.toString());
            }
        }
    }

    private Histogram processFileContent(List<String> lines){
        //create new Histogram to store results
        Histogram resultHistogram = new Histogram();

        // count lines
        long linesInFile = lines.size();
        resultHistogram.setLines(linesInFile);

        // count letter distribution
        long[] distribution = new long[26];
        for (String line: lines) {
            countLettersInLine(line, distribution);
        }
        resultHistogram.setDistribution(distribution);

        return resultHistogram;
    }

    private void countLettersInLine(String line, long[] distribution){
        for(int x = 0; x < line.length(); x++){

            char character = line.charAt(x);
            int asciiValue = (int) character;

            if(asciiValue >= 'A' && asciiValue <= 'Z'){
                distribution[asciiValue - 'A']++;
            }
            if(asciiValue >= 'a' && asciiValue <= 'z'){
                distribution[asciiValue - 'a']++;
            }
        }
    }

    private void incrementNumberOfFiles(){
        masterThread.getHistogram().setFiles(masterThread.getHistogram().getFiles() + 1);
    }

    private void incrementNumberOfProcessedFiles(){
        masterThread.getHistogram().setProcessedFiles(masterThread.getHistogram().getProcessedFiles() + 1);
    }

    private void addToNumberOfLines(long x){
        masterThread.getHistogram().setLines(masterThread.getHistogram().getLines() + x);
    }

    private void incrementNumberOfDirectories(){
        masterThread.getHistogram().setDirectories(masterThread.getHistogram().getDirectories() + 1);
    }

    private void logProcessedFile(String path) throws InterruptedException {
        Message message = new Message(MessageType.FIlE, path);
        masterThread.getOut().put(message);
    }

    private void logProcessedDirectory() throws InterruptedException {
        Message message = new Message(MessageType.FOLDER, rootFolder, masterThread.getHistogram());
        masterThread.getOut().put(message);
    }

}
