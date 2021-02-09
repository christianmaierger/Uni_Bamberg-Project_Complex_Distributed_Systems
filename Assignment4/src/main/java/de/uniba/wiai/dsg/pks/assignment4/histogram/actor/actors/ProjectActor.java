package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.FinalFailureException;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProjectActor extends AbstractActor {
    private final int NUMBER_FILE_ACTORS = 8;
    private int pendingFolderActors = 0;
    private String rootDirectory;
    private String fileExtension;
    private ActorRef loadBalancer;
    private ActorRef outputActor;
    private ActorRef service;
    private final Histogram histogram = new Histogram();
    private final SupervisorStrategy strategy = new OneForOneStrategy(10, Duration.ofMinutes(1),
            DeciderBuilder
                    .match(IOException.class, e -> SupervisorStrategy.restart())
                    .matchAny(o -> SupervisorStrategy.escalate())
                    .build());

    public static Props props() {
        return Props.create(ProjectActor.class, () -> new ProjectActor());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ParseDirectory.class, this::handleRequest)
                .match(ReturnResult.class, this::addHistogram)
                .match(PoisonPill.class, this::suicide)
                .matchAny(this::handleUnknownMessage)
                .build();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        List<Routee> routees = new ArrayList<>();
        for (int i = 1; i <= NUMBER_FILE_ACTORS; i++) {
            ActorRef fileActor = getContext().actorOf(FileActor.props(), "FileActor" + i);
            routees.add(new ActorRefRoutee(fileActor));
        }
        this.loadBalancer = getContext().actorOf(LoadBalancer.props(routees), "LoadBalancer");
        this.outputActor = getContext().actorOf(OutputActor.props(), "OutputActor");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private void handleRequest(ParseDirectory request) {
        try {
            this.service = getSender();
            this.rootDirectory = request.getPath();
            this.fileExtension = request.getFileExtension();
            Path rootDirectoryPath = Path.of(rootDirectory);
            this.pendingFolderActors++;
            startFolderActor(rootDirectory, this.pendingFolderActors);
            traverse(rootDirectoryPath);
        } catch (IOException ioException){
            getSender().tell(new akka.actor.Status.Failure(ioException), getSelf());
        }
    }

    private void traverse(Path rootDirectoryPath) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectoryPath)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    this.pendingFolderActors++;
                    startFolderActor(path.toString(), this.pendingFolderActors);
                    traverse(path);
                }
            }
        }
    }

    private void addHistogram(ReturnResult returnResult) {
        Histogram newHistogram = returnResult.getHistogram();
        this.histogram.setFiles(this.histogram.getFiles() + newHistogram.getFiles());
        this.histogram.setProcessedFiles(this.histogram.getProcessedFiles() + newHistogram.getProcessedFiles());
        this.histogram.setDirectories(this.histogram.getDirectories() + newHistogram.getDirectories());
        this.histogram.setLines(this.histogram.getLines() + newHistogram.getLines());
        for (int i = 0; i < 26; i++) {
            this.histogram.getDistribution()[i] = this.histogram.getDistribution()[i] + newHistogram.getDistribution()[i];
        }
        this.pendingFolderActors--;
        checkForCompletion();
    }

    private void suicide(PoisonPill poisonPill) {
        service.tell(new akka.actor.Status.Failure(new FinalFailureException("At least one FolderActor could" +
                "not finish its tasks.")), getSelf());
    }

    private void handleUnknownMessage(Object unknownMessage) {
        UnknownMessage message = new UnknownMessage(unknownMessage.getClass().toString());
        outputActor.tell(message, getSelf());
    }

    private void startFolderActor(String directory, int actorNumber) {
        Props folderActorProps = FolderActor.props(loadBalancer, outputActor);
        ActorRef folderActor = getContext().actorOf(folderActorProps, "FolderActor" + actorNumber);
        folderActor.tell(new ParseDirectory(directory, fileExtension), getSelf());
    }

    private void checkForCompletion() {
        if (pendingFolderActors == 0) {
            outputActor.tell(new LogMessage(this.histogram, this.rootDirectory, LogMessageType.PROJECT), getSelf());
            service.tell(new ReturnResult(this.histogram), getSelf());
        }
    }
}
