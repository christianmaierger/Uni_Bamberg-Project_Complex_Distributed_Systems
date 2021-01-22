package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import java.io.Serializable;

public final class TerminateConnection implements Serializable {
	// TODO: implement immutable message class
    private static final long serialVersionUID = 2L;
    private final boolean itsallgoodman;

    public TerminateConnection(boolean itsallgoodman) {
        this.itsallgoodman = itsallgoodman;
    }

    public boolean isItsallgoodman() {
        return itsallgoodman;
    }

    @Override
    public String toString() {
        return "TerminateConnection{" +
                "itsallgoodman=" + itsallgoodman +
                '}';
    }
}
