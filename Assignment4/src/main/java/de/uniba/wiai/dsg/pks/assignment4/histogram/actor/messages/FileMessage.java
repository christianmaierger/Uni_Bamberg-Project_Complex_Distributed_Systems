package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import akka.actor.ActorRef;

import java.nio.file.Path;


public final class FileMessage {
    private static final long serialVersionUID = 1L;
    private final Path path;
    private final ActorRef folderActor;
    private final ActorRef outputActor;

    public FileMessage(Path path, ActorRef foderActor, ActorRef outputActor) {
        this.path = path;
        this.folderActor = foderActor;
        this.outputActor = outputActor;
    }

    public Path getPath() {
        return path;
    }

    public ActorRef getFolderActor() {
        return folderActor;
    }

    public ActorRef getOutputActor() {
        return outputActor;
    }
}
