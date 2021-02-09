package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import java.nio.file.Path;

public final class ExceptionMessage {
    private final Path path;

    public ExceptionMessage(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ExceptionMessage[path=" + path + ']';
    }
}