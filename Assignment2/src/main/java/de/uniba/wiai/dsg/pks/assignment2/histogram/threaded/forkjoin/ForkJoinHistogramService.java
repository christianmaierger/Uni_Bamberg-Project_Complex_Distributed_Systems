package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;

public class ForkJoinHistogramService implements HistogramService {

	public ForkJoinHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		// TODO: Implement me
		throw new UnsupportedOperationException("Implement here");
	}

	@Override
	public String toString() {
		return "ForkJoinHistogramService";
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		// TODO: Was soll hiermit passieren? Methode hatten wir für Ass1 im Interface gebraucht...
		throw new UnsupportedOperationException();
	}
}
