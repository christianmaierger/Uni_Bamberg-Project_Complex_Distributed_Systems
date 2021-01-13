package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.util.Optional;

public interface Cache {

	Optional<Histogram> getCachedResult(ParseDirectory request);

	void putInCache(ParseDirectory request, Histogram result);

}
