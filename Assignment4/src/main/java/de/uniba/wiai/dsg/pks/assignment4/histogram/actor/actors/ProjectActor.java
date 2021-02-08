package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectActor extends AbstractActor {
    private final int NUMBER_FILE_ACTORS = 8;
    private int pendingFolderActors = 0;
    private final String rootDirectory;
    private final String fileExtension;
    private ActorRef loadBalancer;
    private ActorRef outputActor;
    private final Histogram histogram = new Histogram();
    private final SupervisorStrategy strategy = new OneForOneStrategy(10, Duration.ofMinutes(1),
            DeciderBuilder
                    .match(ArithmeticException.class, e -> SupervisorStrategy.resume())
                    .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                    .match(IllegalArgumentException.class, e -> SupervisorStrategy.stop())
                    .matchAny(o -> SupervisorStrategy.escalate())
                    .build());
            //FIXME: Welche Arten von Exceptions müssen hier aus FileActor und FolderActor gehandelt werden?
            // Könnte es evtl. sinnvoll sein, den LoadBalancer zum Supervisor von FileActors zu machen, falls
            // das exception handling von FileActor und FolderActor sich sehr unterscheidet? Aber in den Folien
            // aus der Übung war es nicht so...

    public ProjectActor(String rootDirectory, String fileExtension){
        this.rootDirectory = rootDirectory;
        this.fileExtension = fileExtension;
    }

    public static Props props(String rootDirectory, String fileExtension) {
        return Props.create(ProjectActor.class, () -> new ProjectActor(rootDirectory, fileExtension));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(HistogramRequest.class, this::handleRequest)
                .match(ReturnResult.class, this::addHistogram)
                .matchAny(this::handleUnknownMessage)
                .build();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        List<Routee> routees = new ArrayList<>();
        for (int i = 0; i < NUMBER_FILE_ACTORS; i++) {
            ActorRef fileActor = getContext().actorOf(FileActor.props());
            routees.add(new ActorRefRoutee(fileActor));
        }
        this.loadBalancer = getContext().actorOf(LoadBalancer.props(routees), "LoadBalancer");
        this.outputActor = getContext().actorOf(OutputActor.props(), "OutputActor");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private void handleRequest(HistogramRequest request) {
        try {
            int actorCount = 0;
            Path rootDirectoryPath = Path.of(rootDirectory);
            actorCount++;
            startFolderActor(rootDirectory, actorCount);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectoryPath)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        actorCount++;
                        startFolderActor(path.toString(), actorCount);
                    }
                }
            }
            this.pendingFolderActors = actorCount;
        } catch (IOException ioException){
            getSender().tell(new akka.actor.Status.Failure(ioException), getSelf());
        }
    }

    private void addHistogram(ReturnResult returnResult){
        Histogram newHistogram = returnResult.getHistogram();
        if(Objects.nonNull(newHistogram)){
            this.histogram.setFiles(this.histogram.getFiles() + newHistogram.getFiles());
            this.histogram.setProcessedFiles(this.histogram.getProcessedFiles() + newHistogram.getProcessedFiles());
            this.histogram.setDirectories(this.histogram.getDirectories() + newHistogram.getDirectories());
            this.histogram.setLines(this.histogram.getLines() + newHistogram.getLines());
            for(int i=0; i<26 ; i++) {
                this.histogram.getDistribution()[i]= this.histogram.getDistribution()[i] + newHistogram.getDistribution()[i];
            }
        } else {
            //FIXME: Was tun, wenn kein Histogram da? Gibt es überhaupt einen use case, in dem folder actor null schickt?
        }
        this.pendingFolderActors--;
        checkForCompletion();
    }

    private void handleUnknownMessage(Object unknownMessage){
        UnknownMessage message = new UnknownMessage(unknownMessage.getClass().toString());
        outputActor.tell(message, getSelf());
    }

    private void startFolderActor(String directory, int actorNumber){
        Props folderActorProps = FolderActor.props(directory, fileExtension, loadBalancer, getSelf(), outputActor);
        ActorRef folderActor = getContext().actorOf(folderActorProps, "FolderActor" + actorNumber);
        //FIXME: Welche Nachricht hier in FolderActor schicken klären!
        folderActor.tell(new ParseDirectory(directory, fileExtension), getSelf());
    }

    private void checkForCompletion(){
        if(pendingFolderActors == 0){
            outputActor.tell(new LogMessage(this.histogram, this.rootDirectory, LogMessageType.PROJECT), getSelf());
            getSender().tell(new ReturnResult(this.histogram), getSelf());
        }
    }


}
