package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import java.nio.file.Path;

public final class ExeptionMessage {
    private static final long serialVersionUID = 1L;
    private final Exception exception;
    private final Path path;

    public ExeptionMessage(Exception exception, Path path) {
        this.exception = exception;
        this.path = path;
    }

    public Exception getException() {
        return exception;
    }

    public Path getPath() {
        return path;
    }

}
