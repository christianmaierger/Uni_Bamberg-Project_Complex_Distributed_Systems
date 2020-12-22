package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import net.jcip.annotations.ThreadSafe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

@ThreadSafe
public class StreamHistogramService implements HistogramService {

	//TODO: Printer sollte nach dem interrupt noch einige Nachrichten ausgeben?

	public StreamHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		if(Objects.isNull(rootDirectory) || Objects.isNull(fileExtension)){
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

		StreamWorker streamWorker = new StreamWorker(rootDirectory, fileExtension);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Histogram> histogramFuture = executor.submit(streamWorker);

		try {
			return histogramFuture.get();
		} catch (InterruptedException exception) {
			streamWorker.stopProcessing();
			throw new HistogramServiceException("Execution has been interrupted.");
		} catch (ExecutionException exception) {
			throw new HistogramServiceException(exception.getMessage(), exception.getCause());
		} finally {
			shutDown(executor);
		}
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "StreamHistogramService";
	}

	private void shutDown(ExecutorService executorPool){
		executorPool.shutdownNow();
		try {
			if (!executorPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				System.err.println("Thread pool did not terminate.");
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}

