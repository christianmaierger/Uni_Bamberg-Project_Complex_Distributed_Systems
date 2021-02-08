package de.uniba.wiai.dsg.pks.assignment4.histogram.actor;

public class FinalFailureException extends Exception {


    public FinalFailureException() {
        super();
    }

    public FinalFailureException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public FinalFailureException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public FinalFailureException(String arg0) {
        super(arg0);
    }

    public FinalFailureException(Throwable arg0) {
        super(arg0);
    }
}
