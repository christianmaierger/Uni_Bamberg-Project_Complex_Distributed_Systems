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
        Optional<Histogram> cachedHistogram = parentServer.getCachedResult(parseDirectory);
        if(cachedHistogram.isPresent()){
            return cachedHistogram.get();
        }
        try {
            Histogram histogram = new Histogram();
            processFiles(histogram);
            histogram.setDirectories(1);
            parentServer.putInCache(parseDirectory, histogram);
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

    private void processFiles(Histogram histogram) throws IOException, InterruptedException {
        Path folder = Paths.get(parseDirectory.getPath());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    checkForInterrupt();
                    histogram.setFiles(histogram.getFiles() + 1);
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(parseDirectory.getFileExtension());
                    if (fileExtensionCorrect) {
                        processFileContent(path, histogram);
                        histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
                    }
                }
            }
        }
    }

    private void processFileContent(Path path, Histogram histogram) throws IOException {
        histogram.setLines(histogram.getLines() + DirectoryUtils.getLinesPerFile(path));
        List<String> lines = DirectoryUtils.getFileAsLines(path);
        long[] distribution = DirectoryUtils.countLetters(lines);
        histogram.setDistribution(DirectoryUtils.sumUpDistributions(distribution, histogram.getDistribution()));
    }
}





