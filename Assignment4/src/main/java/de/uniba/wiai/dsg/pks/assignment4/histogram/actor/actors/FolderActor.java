package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.FinalFailureException;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class FolderActor extends AbstractActor {
    private int filesToProcess;
    private int filesProcessed;
    private final ActorRef loadBalancer;
    private Histogram histogram;
    private ActorRef projectActor;
    private final ActorRef outputActor;
    private final List<Path> retriedPathList = new LinkedList<>();
    private String folderPath;


    public FolderActor(ActorRef loadBalancer, ActorRef outputActor) {
        this.loadBalancer = loadBalancer;
        this.histogram = new Histogram();
        this.outputActor = outputActor;
    }

    static Props props(ActorRef loadBalancer, ActorRef outputActor) {
        return Props.create(FolderActor.class, () -> new FolderActor(loadBalancer, outputActor));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ParseDirectory.class, this::calculateFolderHistogram)
                .match(ReturnResult.class, this::processFileResults)
                .match(ExceptionMessage.class, this::handleException)
                .matchAny(this::handleUnknownMessage)
                .build();
    }


    private void calculateFolderHistogram(ParseDirectory message) throws FinalFailureException {
        try {
            this.projectActor = getSender();
            folderPath = message.getPath();
            processFiles(message.getPath(), message.getFileExtension());
        } catch (IOException e) {
            throw new FinalFailureException(e);
        }
    }

    private void handleException(ExceptionMessage exceptionMessage) throws FinalFailureException {
        Exception exceptionFromFile = exceptionMessage.getException();
        if (exceptionFromFile instanceof IOException) {
            Path missingResultPath = exceptionMessage.getPath();
            if (!retriedPathList.contains(missingResultPath)) {
                retriedPathList.add(missingResultPath);
                FileMessage retryMessage = new FileMessage(missingResultPath);
                loadBalancer.tell(retryMessage, getSelf());
            } else {
                throw new FinalFailureException("FolderActor was not able to finish due to repeated IOException." +
                        "Result cannot be correct anymore.",
                        exceptionFromFile.getCause());
            }
        }
    }

    private void processFileResults(ReturnResult fileResult) {
        Histogram subResult = fileResult.getHistogram();
        outputActor.tell(new LogMessage(subResult, fileResult.getFilePath().toString(), LogMessageType.FILE), getSender());
        histogram = addUpAllFields(subResult, histogram);
        filesProcessed++;
        checkForCompletion();
    }

    private void checkForCompletion() {
        if (filesProcessed == filesToProcess) {
            histogram.setDirectories(1);
            outputActor.tell(new LogMessage(this.histogram, folderPath, LogMessageType.FOLDER), getSelf());
            projectActor.tell(new ReturnResult(this.histogram), getSelf());
        }
    }

    /**
     * Adds up all fields of two histograms and returns a new histogram with their values from all fields added
     * together.
     *
     * @param subResultHistogram a new result as histogram of which the fields should be added on the fields of a given histogram
     * @param oldHistogram       the histogram to which the method should add to
     * @return a Histogrom holding the addition of the two input Histograms
     */
    private Histogram addUpAllFields(Histogram subResultHistogram, Histogram oldHistogram) {

        long[] oldHistogramDistribution = oldHistogram.getDistribution();
        long[] newHistogramDistribution = subResultHistogram.getDistribution();

        for (int i = 0; i < 26; i++) {
            oldHistogramDistribution[i] = oldHistogramDistribution[i] + newHistogramDistribution[i];
        }

        Histogram result = new Histogram();
        result.setDistribution(oldHistogramDistribution);
        result.setFiles(oldHistogram.getFiles() + subResultHistogram.getFiles());
        result.setProcessedFiles(oldHistogram.getProcessedFiles() + subResultHistogram.getProcessedFiles());
        result.setDirectories(oldHistogram.getDirectories() + subResultHistogram.getDirectories());
        result.setLines(oldHistogram.getLines() + subResultHistogram.getLines());
        return result;
    }

    private void handleUnknownMessage(Object unknownMessage) {
        throw new IllegalArgumentException(unknownMessage.getClass().getSimpleName());
    }

    private void processFiles(String folder, String fileExtension) throws IOException {
        Path folderPath = Paths.get(folder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect) {
                        filesToProcess++;
                        FileMessage message = new FileMessage(path);
                        loadBalancer.tell(message, getSelf());
                    } else {
                        histogram.setFiles(histogram.getFiles() + 1);
                    }
                }
            }
        }
        checkForCompletion();
    }
}
