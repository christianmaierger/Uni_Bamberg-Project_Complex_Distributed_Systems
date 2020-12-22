package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.PrintService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

public class ForkJoinHistogramService implements HistogramService {

	public ForkJoinHistogramService() {
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

		PrintService printService = new PrintService();
		ExecutorService singleThreadedPoolForOutput = Executors.newSingleThreadExecutor();
		singleThreadedPoolForOutput.submit(printService);

		ForkJoinPool mainPool = new ForkJoinPool();
		TraverseTask traverseTask = new TraverseTask(rootDirectory, fileExtension, printService, singleThreadedPoolForOutput, mainPool);
		mainPool.execute(traverseTask);

		Histogram resultHistogram;
		try {
			resultHistogram = traverseTask.get();
		} catch (InterruptedException e) {
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} catch (ExecutionException e) {
			throw new HistogramServiceException(e.getMessage(), e.getCause());
		} finally {
			shutDownPools(mainPool, printService, singleThreadedPoolForOutput);
		}
		return resultHistogram;
	}

	private void shutDownPools(ForkJoinPool mainPool, PrintService outputRunnable, ExecutorService singleThreadedPoolForOutput) throws HistogramServiceException {
		try {
			mainPool.shutdownNow();
			outputRunnable.put(new Message(MessageType.FINISH));
			if (!mainPool.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				System.err.println("Main pool did not terminate");
			}
			singleThreadedPoolForOutput.shutdown();
			if (!singleThreadedPoolForOutput.awaitTermination(60, TimeUnit.MILLISECONDS)) {
				singleThreadedPoolForOutput.shutdownNow();
				if (!singleThreadedPoolForOutput.awaitTermination(60, TimeUnit.MILLISECONDS)){
					System.err.println("Output pool did not terminate");
				}
			}
		} catch (InterruptedException e) {
			throw new HistogramServiceException("Execution has been interrupted.", e);
		}
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "ForkJoinHistogramService";
	}
}
