package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MasterThread;
import net.jcip.annotations.NotThreadSafe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@NotThreadSafe
public class LowlevelHistogramService implements HistogramService {
	private boolean ioExceptionThrown;

	public LowlevelHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		if(!Objects.nonNull(rootDirectory) || !Objects.nonNull(fileExtension)){
			throw new HistogramServiceException("Neither root directory nor file extension must be null.");
		}
		if(rootDirectory.isBlank() || fileExtension.isBlank()){
			throw new HistogramServiceException("Neither root directory nor file extension must be empty.");
		}
		Path rootPath = Paths.get(rootDirectory);
		if(!Files.exists(rootPath)){
			throw new HistogramServiceException("Root directory does not exist.");
		}
		if(!Files.isDirectory(rootPath)){
			throw new HistogramServiceException("Root directory must be a directory");
		}

		ioExceptionThrown = false;
		Histogram histogram = new Histogram();
		Thread masterThread = new MasterThread(rootDirectory, fileExtension, histogram, Service.LOW_LEVEL, 0.3, this);

		try{
			masterThread.start();
			masterThread.join();
		} catch (InterruptedException exception){
			masterThread.interrupt();
			throw new HistogramServiceException("Execution has been interrupted.");
		}
		if(ioExceptionThrown){
			throw new HistogramServiceException("IOException occurred while processing of folder.");
		}
		return histogram;
	}


	public void setIoExceptionThrown(boolean value){
		this.ioExceptionThrown = value;
	}

	@Override
	public String toString() {
		return "LowlevelHistogramService";
	}

}
