package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Semaphore;

public class HighLevelWorker extends Thread {
    private final String directory;
    private final String fileExtension;
    private final Histogram histogram;
    private final Object lock = new Object();
    private final Semaphore semaphore;

    public HighLevelWorker(String directory, String fileExtension, Histogram histogram, Semaphore semaphore){
        this.directory = directory;
        this.fileExtension = fileExtension;
        this.histogram = histogram;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        Path folder = Paths.get(directory);
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if (Files.isRegularFile(path)){
                    if(Thread.currentThread().isInterrupted()){
                        throw new InterruptedException("Execution has been interrupted.");
                    }

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
        } catch (IOException exception){
            throw new RuntimeException("I/O error occurred while reading folders and files.");
        } catch (InterruptedException exception){
            throw new RuntimeException(exception.getMessage());
        }
        synchronized (lock){
            incrementNumberOfDirectories();
            //TODO: LOG DIRECTORY
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
                //TODO: LOG FILE
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
