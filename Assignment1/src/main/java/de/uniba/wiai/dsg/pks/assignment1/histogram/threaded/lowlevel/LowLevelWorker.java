package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputThread;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LowLevelWorker extends Thread {
    MasterThread masterThread;
    String directory;
    String extension;
    private final Object lock;
    OutputThread outputThread;


    public LowLevelWorker(MasterThread masterThread, String directory, String extension, Object lock, OutputThread outputThread) {
        this.masterThread= masterThread;
        this.directory = directory;
       this.extension = extension;
       this.lock=lock;
       this.outputThread=outputThread;
    }

    public void  processDirectoryWithWorker(String rootDirectory, String fileExtension) throws InterruptedException, IOException {
        Path folder = Paths.get(rootDirectory);

        // worker is different, but outPutThread is the same, so the boolean in out has to be set back, so
        // it can print again

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
            for(Path path: stream){
                if(Thread.currentThread().isInterrupted()){
                    throw new InterruptedException("Execution has been interrupted.");
                }
                if (Files.isDirectory(path)){
                    // to do aber wahrscheinlich nix machen
                } else if (Files.isRegularFile(path)){
                    masterThread.incrementNumberOfFiles();
                    if (path.getFileName().toString().endsWith(fileExtension)){
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        processFile(lines);


                        synchronized (lock) {
                            // masterThread.getOut().logProcessedFile(path.toString());

                        }
                    }
                }
            }
            synchronized (lock) {
                outputThread.setLastUpdatePrinted(false);
                masterThread.incrementNumberOfDirectories();
                outputThread.updateHistogram(masterThread.getHistogram());
                outputThread.setCurrentDirectory(rootDirectory);
                Object histCounter = new Object();
                outputThread.updateHistogramCounterList(histCounter);
                lock.notifyAll();
            }
            outputThread.setLastUpdatePrinted(true);


        } catch (IOException io){
            throw new IOException( "I/O error occurred while reading folders and files.");
        }

    }

    private void processFile(List<String> lines){
        // lines
        int linesInFile = lines.size();
        masterThread.addToNumberOfLines(linesInFile);

        // processed file
        masterThread.incrementNumberOfProcessedFiles();

        // letter distribution
        for (String line: lines) {
            countLettersInLine(line);
        }
    }

    private void countLettersInLine(String line){
        for(int x = 0; x < line.length(); x++){

            char character = line.charAt(x);
            int asciiValue = (int) character;

            if(asciiValue >= 'A' && asciiValue <= 'Z'){
                // Uppercase letters to lowercase
                asciiValue = (int) String.valueOf(character).toLowerCase().toCharArray()[0];
            }
            if(asciiValue >= 'a' && asciiValue <= 'z'){
                // will only increment for lowercase letters
                masterThread.incrementDistributionAtX(asciiValue - 'a');
            }
        }
    }




    @Override
    public void run() {
                    try {

                processDirectoryWithWorker(directory, extension);

            } catch (Exception e) {
                //todo
            }

        }
    }

