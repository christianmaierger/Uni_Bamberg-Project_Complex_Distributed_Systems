package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MasterThread;

public class HighlevelHistogramService implements HistogramService {

	public HighlevelHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		Histogram histogram = new Histogram();
		Thread masterThread = new MasterThread(rootDirectory, fileExtension, histogram, Service.HIGH_LEVEL, 0.5);

		try{
			masterThread.start();
			masterThread.join();
		} catch (InterruptedException | RuntimeException exception){
			masterThread.interrupt();
			throw new HistogramServiceException(exception.getMessage());
		}
		return histogram;
	}

	@Override
	public String toString() {
		return "HighlevelHistogramService";
	}
}
