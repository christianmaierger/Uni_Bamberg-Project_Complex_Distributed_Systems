package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import java.io.Serializable;
import java.util.Objects;

public final class ParseDirectory implements Serializable {
    private static final long serialVersionUID = 1L;

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
        return "ParseDirectory[path = '" + path + "', extension = '" + fileExtension + "'";
    }

    @Override
    public boolean equals(Object object){
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()){
            return false;
        }
        ParseDirectory other = (ParseDirectory) object;
        if (!path.equals(other.path)) {
            return false;
        }
        if (!fileExtension.equals(other.fileExtension)) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hash(path, fileExtension);
    }
}
