package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import java.nio.file.Path;

public final class FileMessage {
    private final Path path;

    public FileMessage(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "FileMessage[path=" + path + ']';
    }
}
