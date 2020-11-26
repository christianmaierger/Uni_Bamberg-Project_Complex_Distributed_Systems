package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.SyncType;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;

public class LowlevelHistogramService implements HistogramService {

	public LowlevelHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		MasterThread masterThread = new MasterThread(0.5, fileExtension, SyncType.LOWLEVEL);
		Histogram histogram;
		try{
			 histogram = masterThread.traverseRootDirectory(rootDirectory);
		} catch (RuntimeException | IOException exception){
			throw new HistogramServiceException(exception.getMessage());
		}
		return histogram;
	}


	@Override
	public String toString() {
		return "LowlevelHistogramService";
	}

}
