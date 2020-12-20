package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.stream;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

public class StreamHistogramService implements HistogramService {

	public StreamHistogramService() {
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

		StreamWorker streamWorker = new StreamWorker(rootDirectory, fileExtension);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Histogram> histogramFuture = executor.submit(streamWorker);

		while(!histogramFuture.isDone()){
			if(Thread.currentThread().isInterrupted()){
				streamWorker.stopProcessing();
				shutDown(executor);
				throw new HistogramServiceException("Execution has been interrupted.");
			}
		}

		try {
			return histogramFuture.get();
		} catch (InterruptedException exception) {
			throw new HistogramServiceException("Execution has been interrupted.");
		} catch (ExecutionException exception) {
			throw new HistogramServiceException(exception.getMessage(), exception.getCause());
		} finally {
			shutDown(executor);
		}
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		// TODO: Was soll hiermit passieren? Methode hatten wir für Ass1 im Interface gebraucht...
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "StreamHistogramService";
	}

	private void shutDown(ExecutorService executorPool) throws HistogramServiceException {
		// TODO: Macht das Ganze hier überhaupt Sinn, wenn der StreamWorker eh nicht auf einen Interrupt reagiert?
		executorPool.shutdown();
		try {
			if (!executorPool.awaitTermination(100, TimeUnit.MILLISECONDS)) {
				executorPool.shutdownNow();
				if (!executorPool.awaitTermination(3, TimeUnit.SECONDS)){
					throw new HistogramServiceException("Thread pool did not terminate.");
				}
			}
		} catch (InterruptedException exception) {
			executorPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}

