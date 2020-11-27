package de.uniba.wiai.dsg.pks.assignment1.histogram.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import net.jcip.annotations.Immutable;

@Immutable
public class Message {
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
        return type;
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
