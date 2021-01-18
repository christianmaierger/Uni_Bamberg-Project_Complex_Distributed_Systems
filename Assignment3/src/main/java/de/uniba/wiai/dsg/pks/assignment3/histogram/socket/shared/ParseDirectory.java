package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import java.io.Serializable;

public class ParseDirectory implements Serializable {
	// TODO: implement immutable message class
    private static final long serialVersionUID = 3L;

    private final String path;
    private final String fileExtension;

    public ParseDirectory(String path, String fileExtension) {
        this.path = path;
        this.fileExtension = fileExtension;
    }

    public String getPath() {
        return path;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String toString() {
        return "ParseDirectory{" +
                "path='" + path + '\'' +
                '}';
    }
}
