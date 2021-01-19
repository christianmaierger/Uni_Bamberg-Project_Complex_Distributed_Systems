package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.Serializable;
import java.util.Objects;

public final class ReturnResult implements Serializable {
    private static final long serialVersionUID = 1L;

    // TODO: Problem mit exception in dieser Klasse ist, dass der Client a) wahrscheinlich ncihtr daran interessiert ist
    // zu wissen, was beim Serevr passiert und welche Exceptions auftreten. b) exception muss man dann hier auch deepCopy-en
    // viel zu aufwendig
    private final Histogram resultHistogram;

    public ReturnResult(Histogram histogram) {
        this.resultHistogram = deepCopyHistogram(histogram);
    }

    public Histogram getHistogram() {
        return deepCopyHistogram(resultHistogram);
    }

    @Override
    public String toString() {
        return "ReturnResult[histogram = " + resultHistogram.toString() + "]";
    }

    private Histogram deepCopyHistogram(Histogram histogram){
        if(Objects.isNull(histogram)){
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