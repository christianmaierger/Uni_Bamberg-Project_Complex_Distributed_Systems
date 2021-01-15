package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;

public class SocketHistogramService implements HistogramService {

	public SocketHistogramService(String hostname, int port) {
		// REQUIRED FOR GRADING - DO NOT CHANGE SIGNATURE
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory,
			String fileExtension) throws HistogramServiceException {
		throw new UnsupportedOperationException("Implement here");
	}

	@Override
	public String toString() {
		return "SocketHistogramService";
	}


	/**
	 * Unneeded legacy method from Assignment 1.
	 */
	@Override
	public void setIoExceptionThrown(boolean value){
		throw new UnsupportedOperationException();
	}

}
