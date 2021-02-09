package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.util.Objects;

public final class LogMessage {
    private final Histogram histogram;
    private final String path;
    private final LogMessageType logMessageType;

    public LogMessage(Histogram histogram, String path, LogMessageType logMessageType) {
        this.histogram = deepCopyHistogram(histogram);
        this.path = path;
        this.logMessageType = logMessageType;
    }

    public Histogram getHistogram() {
        return deepCopyHistogram(histogram);
    }

    public String getPath() {
        return path;
    }

    public LogMessageType getLogMessageType() {
        return logMessageType;
    }

    private Histogram deepCopyHistogram(Histogram histogram) {
        if (Objects.isNull(histogram)) {
            return null;
        }
        Histogram deepCopy = new Histogram();
        deepCopy.setLines(histogram.getLines());
        deepCopy.setFiles(histogram.getFiles());
        deepCopy.setProcessedFiles(histogram.getProcessedFiles());
        deepCopy.setDirectories(histogram.getDirectories());

        long[] distribution = new long[Histogram.ALPHABET_SIZE];
        for (int i = 0; i < Histogram.ALPHABET_SIZE; i++) {
            distribution[i] = histogram.getDistribution()[i];
        }
        deepCopy.setDistribution(distribution);
        return deepCopy;
    }

    @Override
    public String toString() {
        return "LogMessage{" +
                "histogram=" + histogram +
                ", path='" + path + '\'' +
                ", logMessageType=" + logMessageType +
                '}';
    }
}
