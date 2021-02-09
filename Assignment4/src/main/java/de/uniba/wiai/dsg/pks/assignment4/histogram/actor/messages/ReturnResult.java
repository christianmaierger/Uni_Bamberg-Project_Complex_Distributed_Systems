package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.nio.file.Path;
import java.util.Objects;

public final class ReturnResult {
    private final Histogram resultHistogram;
    private final Path filePath;


    public ReturnResult(Histogram histogram, Path filePath) {
        this.resultHistogram = deepCopyHistogram(histogram);
        this.filePath = filePath;
    }

    public ReturnResult(Histogram histogram) {
        this.resultHistogram = deepCopyHistogram(histogram);
        this.filePath = null;
    }

    public Histogram getHistogram() {
        return deepCopyHistogram(resultHistogram);
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "ReturnResult{" +
                "resultHistogram=" + resultHistogram +
                ", filePath=" + filePath +
                '}';
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
}