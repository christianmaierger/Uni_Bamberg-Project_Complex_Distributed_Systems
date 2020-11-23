package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;

public class LowlevelHistogramService implements HistogramService {
	MasterThread masterThread = new MasterThread(1);

	public LowlevelHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}


	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		try{
			processDirectory(rootDirectory, fileExtension);
			// increment number of directories because now root directory has been processed as well
			incrementNumberOfDirectories();
		} catch (InterruptedException | IOException exception) {
			throw new HistogramServiceException(exception.getMessage());
		}
		return histogram;
	}


	@Override
	public String toString() {
		return "LowlevelHistogramService";
	}

}
