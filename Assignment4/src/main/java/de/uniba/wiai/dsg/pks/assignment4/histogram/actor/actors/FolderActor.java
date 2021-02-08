package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
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
                .match(ExeptionMessage.class, this::handleException)
                .matchAny(this::handleUnknownMessage)
                .build();

    }

    private void handleException(ExeptionMessage exeptionMessage) {
        Exception exceptionFromFile = exeptionMessage.getException();
        if (exceptionFromFile instanceof IOException) {
            exceptionFromFile.getCause();
            Path missingResultPath = exeptionMessage.getPath();
            if (!retriedPathList.contains(missingResultPath)){
                retriedPathList.add(missingResultPath);
                FileMessage retryMessage = new FileMessage(missingResultPath, outputActor);
                loadBalancer.tell(retryMessage, getSelf());
            } else {
                projectActor.tell(PoisonPill.getInstance(), getSelf());
            }
        }
    }


    private void processFileResults(ReturnResult fileResult) {
        histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
        Histogram subResult = fileResult.getHistogram();
        histogram = addUpAllFields(subResult, histogram);
        filesProcessed++;
        checkForCompletion(fileResult.getFilePath().toString());
    }

    private void checkForCompletion(String path){
        if(filesProcessed == filesToProcess){
            histogram.setDirectories(1);
            outputActor.tell(new LogMessage(this.histogram, path, LogMessageType.FOLDER), getSelf());
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
    private static Histogram addUpAllFields(Histogram subResultHistogram, Histogram oldHistogram) {

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

    public void calculateFolderHistogram(ParseDirectory message) {
        try {
            this.projectActor = getSender();
            processFiles(message.getPath(), message.getFileExtension());
        } catch (IOException e) {
            projectActor.tell(PoisonPill.getInstance(), getSelf());
        }
    }


    private void handleUnknownMessage(Object unknownMessage) {
        UnknownMessage message = new UnknownMessage(unknownMessage.getClass().toString());
        outputActor.tell(message, getSelf());
    }

    /**
     * @throws IOException
     */
    private void processFiles(String folder, String fileExtension) throws IOException {
        Path folderPath = Paths.get(folder);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {

                    histogram.setFiles(histogram.getFiles() + 1);
                    filesProcessed++;
                    boolean fileExtensionCorrect = path.getFileName().toString().endsWith(fileExtension);
                    if (fileExtensionCorrect) {
                        this.filesToProcess++;
                        FileMessage message = new FileMessage(path, outputActor);
                        loadBalancer.tell(message, getSelf());
                    }
                }
            }
        }
        checkForCompletion(folder);
    }


}
