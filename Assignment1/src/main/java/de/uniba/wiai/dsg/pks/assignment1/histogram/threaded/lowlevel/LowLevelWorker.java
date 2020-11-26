package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LowLevelWorker extends Thread {
    private final String directory;
    private final String fileExtension;
    private final Histogram histogram;
    private final MasterThread masterThread;
    private final Object lock = new Object();
    private final OutputService out;
    private final LowLevelSemaphore semaphore;

    public LowLevelWorker(String directory, String fileExtension, Histogram histogram, MasterThread masterThread, OutputService out, LowLevelSemaphore semaphore){
        this.directory = directory;
        this.fileExtension = fileExtension;
        this.histogram = histogram;
        this.masterThread = masterThread;
        this.out = out;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        Path folder = Paths.get(directory);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isRegularFile(path)){

                    //count
                    Histogram histogramForCurrentFile = new Histogram();
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect){
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        histogramForCurrentFile = processFileContent(lines);
                    }

                    //update
                    updateResultsInHistogram(histogramForCurrentFile, fileExtensionCorrect, path);
                }
            }
        } catch (IOException io){
            throw new RuntimeException("I/O error occurred while reading folders and files.");
        }
        synchronized (lock){
            incrementNumberOfDirectories();
            //TODO: DeepCopy löschen
            out.logProcessedDirectory(directory, histogram);
        }
        semaphore.release();
    }

    private void updateResultsInHistogram(Histogram updateHistogram, boolean correctFileExtension, Path path){
        synchronized (lock){
            incrementNumberOfFiles();
            if(correctFileExtension){
                incrementNumberOfProcessedFiles();
                addToNumberOfLines(updateHistogram.getLines());
                for(int x = 0; x < 26; x++){
                    histogram.getDistribution()[x] += updateHistogram.getDistribution()[x];
                }
                //TODO: CHANGE THIS TO BE A CONSUMER/PRODUCER PATTERN
                out.logProcessedFile(path.toString());
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
        histogram.setFiles(histogram.getFiles() + 1);
    }

    private void incrementNumberOfProcessedFiles(){
        histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
    }

    private void addToNumberOfLines(long x){
        histogram.setLines(histogram.getLines() + x);
    }

    private void incrementNumberOfDirectories(){
        histogram.setDirectories(histogram.getDirectories() + 1);
    }

}
