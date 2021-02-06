package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import java.nio.file.Path;


public class FileMessage {
    private static final long serialVersionUID = 1L;
    private final Path path;

    public FileMessage(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
