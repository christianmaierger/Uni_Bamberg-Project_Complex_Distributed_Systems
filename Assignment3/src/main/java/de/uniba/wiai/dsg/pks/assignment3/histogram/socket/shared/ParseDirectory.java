package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import java.io.Serializable;

public class ParseDirectory implements Serializable {
	// TODO: implement immutable message class
    private final String path;

    public ParseDirectory(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ParseDirectory{" +
                "path='" + path + '\'' +
                '}';
    }
}
