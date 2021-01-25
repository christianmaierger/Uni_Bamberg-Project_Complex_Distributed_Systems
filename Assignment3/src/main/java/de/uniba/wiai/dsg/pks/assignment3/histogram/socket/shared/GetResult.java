package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared;

import net.jcip.annotations.Immutable;

import java.io.Serializable;

@Immutable
public final class GetResult implements Serializable {
	// TODO: implement immutable message class
    private static final long serialVersionUID = 4L;


    @Override
    public String toString() {
        return "GetResult{}";
    }
}



