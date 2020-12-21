package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.PrintService;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;


public class ForkJoinHistogramService implements HistogramService {



	private boolean ioExceptionThrown;
	public ForkJoinHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		// TODO: Implement me

		if (Objects.isNull(rootDirectory) || Objects.isNull(fileExtension)) {
			throw new HistogramServiceException("Neither root directory nor file extension must be null.");
		}
		if (rootDirectory.isBlank() || fileExtension.isBlank()) {
			throw new HistogramServiceException("Neither root directory nor file extension must be empty.");
		}

		Path root = Paths.get(rootDirectory);
		if (!Files.exists(root)) {
			throw new HistogramServiceException("Root Directory does not exist.");
		}
		if (!Files.isDirectory(root)) {
			throw new HistogramServiceException("Root directory must be a directory.");
		}

		setIoExceptionThrown(false);
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		TraverseTask traverseTask = new TraverseTask(forkJoinPool, rootDirectory, fileExtension);

		Future<Histogram> result;

		result = forkJoinPool.submit(traverseTask);

		Histogram resultHistogram = new Histogram();

		PrintService printService = new PrintService();

		executorService.submit(printService);

		try {
			resultHistogram = result.get();
		} catch (InterruptedException e) {
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} catch (ExecutionException e) {
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} finally {
			forkJoinPool.shutdownNow();
			try {
				if (forkJoinPool.awaitTermination(60, TimeUnit.MILLISECONDS)) {
					System.err.println("Pool did not terminate.");
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		return resultHistogram;
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		this.ioExceptionThrown = value;
		// throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "ForkJoinHistogramService";
	}

}
