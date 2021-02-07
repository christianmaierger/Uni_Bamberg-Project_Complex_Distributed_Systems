package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.HistogramRequest;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ReturnResult;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectActor extends AbstractActor {
    private final int NUMBER_FILE_ACTORS = 8;
    private int pendingFolderActors = 0;
    private final String rootDirectory;
    private final String fileExtension;
    private ActorRef loadBalancer;
    private final Histogram histogram = new Histogram();

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
        this.loadBalancer = getContext().actorOf(LoadBalancer.props(routees));
    }

    //TODO: Take care of exception here
    private void handleRequest(HistogramRequest request) throws IOException {
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
    }

    private void addHistogram(ReturnResult returnResult){
        //TODO: evtl hier checken, ob überhaupt ein Histogram drin ist?
        Histogram newHistogram = returnResult.getHistogram();
        this.histogram.setFiles(this.histogram.getFiles() + newHistogram.getFiles());
        this.histogram.setProcessedFiles(this.histogram.getProcessedFiles() + newHistogram.getProcessedFiles());
        this.histogram.setDirectories(this.histogram.getDirectories() + newHistogram.getDirectories());
        this.histogram.setLines(this.histogram.getLines() + newHistogram.getLines());
        for(int i=0; i<26 ; i++) {
            this.histogram.getDistribution()[i]= this.histogram.getDistribution()[i] + newHistogram.getDistribution()[i];
        }
        this.pendingFolderActors--;
        if(pendingFolderActors == 0){
            getSender().tell(new ReturnResult(this.histogram), getSelf());
        }
    }

    private void handleUnknownMessage(Object unknownMessage){
        //TODO add handling
    }

    private void startFolderActor(String directory, int actorNumber){
        //TODO: add real output actor
        ActorRef output = getContext().actorOf(Props.create(OutputActor.class));
        Props folderActorProps = FolderActor.props(directory, fileExtension, loadBalancer, getSelf(), output);
        ActorRef folderActor = getContext().actorOf(folderActorProps, "FolderActor#" + actorNumber);
        folderActor.tell(new ParseDirectory(directory, fileExtension), getSelf());
    }
}
