package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

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
public class ExecutorHistogramService implements HistogramService {


	public ExecutorHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		// TODO: Implement me
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

		ExecutorService masterExcecutor = Executors.newCachedThreadPool();
		MasterCallable masterCallable = new MasterCallable(masterExcecutor, rootDirectory, fileExtension);
		Future<Histogram> result;
		result = masterExcecutor.submit(masterCallable);
		Histogram resultHistogram = new Histogram();

		try {
			resultHistogram = result.get();

		} catch (InterruptedException e) {
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} catch (ExecutionException e) {
			//todo anderer print
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} finally {
			masterExcecutor.shutdownNow();
			try {
					if (!masterExcecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
						System.err.println("Pool did not terminate");
					}
		} catch (InterruptedException ie) {
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
		return resultHistogram;
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "ExecutorHistogramService";
	}

}
