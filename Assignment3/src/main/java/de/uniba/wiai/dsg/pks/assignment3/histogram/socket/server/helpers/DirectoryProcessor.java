package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.helpers;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.DirectoryServer;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@ThreadSafe
public class DirectoryProcessor implements Callable<Histogram> {

    private final ParseDirectory parseDirectory;
    private final DirectoryServer parentServer;

    public DirectoryProcessor(ParseDirectory parseDirectory, DirectoryServer parentServer) {
        this.parseDirectory = parseDirectory;
        this.parentServer = parentServer;
    }


    @Override
    public Histogram call() throws IOException {
        Optional<Histogram> cachedResult = parentServer.getCachedResult(parseDirectory);
        if(cachedResult.isPresent()){
            return cachedResult.get();
        }
        try {
            Histogram histogram = new Histogram();
            processFiles(histogram);
            histogram.setDirectories(1);
            return histogram;
        }
        catch (InterruptedException exception){
            return null;
            //TODO: ist das so gut? Weil an sich ist das Ergebnis ja nach einem Interrupt egal...
        }
    }

    private void checkForInterrupt() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Execution has been interrupted.");
        }
    }

    private void processFiles(Histogram localHistogram) throws IOException, InterruptedException {
        Path folder = Paths.get(parseDirectory.getPath());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    checkForInterrupt();
                    localHistogram.setFiles(localHistogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(parseDirectory.getFileExtension());
                    if (fileExtensionCorrect) {
                        processFileContent(path, localHistogram);
                        localHistogram.setProcessedFiles(localHistogram.getProcessedFiles() + 1);
                    }
                }
            }
        }
    }

    private void processFileContent(Path path, Histogram localHistogram) throws IOException {
        localHistogram.setLines(localHistogram.getLines() + DirectoryUtils.getLinesPerFile(path));
        List<String> lines = DirectoryUtils.getFileAsLines(path);
        long[] distribution = DirectoryUtils.countLetters(lines);
        localHistogram.setDistribution(DirectoryUtils.sumUpDistributions(distribution, localHistogram.getDistribution()));
    }
}





