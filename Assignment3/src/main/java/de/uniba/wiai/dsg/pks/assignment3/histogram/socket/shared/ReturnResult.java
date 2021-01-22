package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.Serializable;

public class ReturnResult implements Serializable {
    // TODO: implement immutable message class
    private static final long serialVersionUID = 2L;

   private final Histogram resultHistogram;
   private final Exception e;

    public ReturnResult(Histogram resultHistogram) {
        this.resultHistogram = resultHistogram;
      e=null;
    }


    public Histogram getHistogram() {
        return resultHistogram;
    }

    public Exception getException() {
        return e;
    }

    @Override
    public String toString() {
        return "ReturnResult{" +
                "resultHistogram=" + resultHistogram +
                ", e=" + e +
                '}';
    }
}
