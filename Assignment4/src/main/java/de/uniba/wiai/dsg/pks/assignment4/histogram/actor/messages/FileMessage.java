package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import akka.actor.ActorRef;

import java.nio.file.Path;


public final class FileMessage {
    private static final long serialVersionUID = 1L;
    private final Path path;
    private final ActorRef outputActor;

    public FileMessage(Path path, ActorRef outputActor) {
        this.path = path;
        this.outputActor = outputActor;
    }

    public Path getPath() {
        return path;
    }


    public ActorRef getOutputActor() {
        return outputActor;
    }
}
