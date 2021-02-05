package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

public interface Cache {

	boolean alreadyProcessed(ParseDirectory request);

	Histogram getCachedResult(ParseDirectory request);

	void putInCache(ParseDirectory request, Histogram result);

}
