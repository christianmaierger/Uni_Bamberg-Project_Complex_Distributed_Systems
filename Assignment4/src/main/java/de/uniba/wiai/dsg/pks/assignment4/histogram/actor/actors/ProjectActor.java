package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.RequestHistogram;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectActor extends AbstractActor {
    private final String rootDirectory;
    private final String fileExtension;
    private final ActorRef loadBalancer;

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
                .match(RequestHistogram.class, this::handleRequest)
                .matchAny(this::handleUnknownMessage)
                .build();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        getContext().

    }

    //TODO: Take care of exception here
    private void handleRequest(RequestHistogram request) throws IOException {
        Path rootDirectoryPath = Path.of(rootDirectory);
        // FIXME: create FolderActor for rootDir
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectoryPath)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    //create FolderActor for rootDir
                }
            }
        }
    }

    private void handleUnknownMessage(Object unknownMessage){
        //TODO add handling
    }

}
