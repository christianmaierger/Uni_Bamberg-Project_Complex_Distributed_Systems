package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputThread;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.SyncType;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LowlevelHistogramService implements HistogramService {
	MasterThread masterThread;

	//tut says he would start start and end masterthread in this class
	//private final Histogram histogram = new Histogram();
	// private final OutputService out = new OutputService(histogram);

	public LowlevelHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}


	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		masterThread= new MasterThread(0.5, SyncType.LOWLEVEL, rootDirectory, fileExtension);
		try{
			// calculatehistogram is called in main method, I guess there is no better point then starting masterThread here
			// and "he" should than start to process the directories
			masterThread.start();

		} catch (Exception exception) {
			throw new HistogramServiceException(exception.getMessage());
		}
		try {
			masterThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		masterThread.getOut().logProcessedDirectory(rootDirectory);
		return masterThread.getHistogram();
	}

	@Override
	public String toString() {
		return "LowlevelHistogramService";
	}


}
