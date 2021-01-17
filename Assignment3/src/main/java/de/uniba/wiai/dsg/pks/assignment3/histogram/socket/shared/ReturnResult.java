package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.io.Serializable;

public class ReturnResult implements Serializable {
    // TODO: implement immutable message class
   final Histogram resultHistogram;
   final Exception e;

    public ReturnResult(Histogram resultHistogram) {
        this.resultHistogram = resultHistogram;
      e=null;
    }

    public ReturnResult(Exception e) {
        this.e = e;
        resultHistogram=null;
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