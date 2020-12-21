package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import net.jcip.annotations.Immutable;

@Immutable
/**
 * Contains a message for the OutputServiceThread to print. Messages can be of type Message.FOLDER,
 * Message.FILE or Message.FINISH and can contain a path to print and/or a Histogram.
 */
public final class Message {
    private final MessageType type;
    private final String path;
    private final Histogram histogram;

    public Message(MessageType type){
        this.type = type;
        this.path = null;
        this.histogram = null;
    }

    public Message(MessageType type, String path){
        this.type = type;
        this.path = path;
        this.histogram = null;
    }

    public Message(MessageType type, String path, Histogram histogram){
        this.type = type;
        this.path = path;
        this.histogram = new Histogram();
        this.histogram.setFiles(histogram.getFiles());
        this.histogram.setLines(histogram.getLines());
        this.histogram.setDirectories(histogram.getDirectories());
        this.histogram.setProcessedFiles(histogram.getProcessedFiles());
        this.histogram.setDistribution(histogram.getDistribution().clone());
    }

    public String getPath() {
        return path;
    }

    public MessageType getType() {
        if(type.equals(MessageType.FILE)){
            return MessageType.FILE;
        } else if(type.equals(MessageType.FOLDER)){
            return MessageType.FOLDER;
        } else{
            return MessageType.FINISH;
        }
    }

    public Histogram getHistogram(){
        Histogram copy = new Histogram();
        copy.setFiles(histogram.getFiles());
        copy.setLines(histogram.getLines());
        copy.setDirectories(histogram.getDirectories());
        copy.setProcessedFiles(histogram.getProcessedFiles());
        copy.setDistribution(histogram.getDistribution().clone());
        return copy;
    }


}