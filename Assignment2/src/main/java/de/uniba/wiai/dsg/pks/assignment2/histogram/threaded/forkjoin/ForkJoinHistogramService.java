package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.forkjoin;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.MessageType;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceRunnable;

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



		// neuen ForkJoin Pool anlegen
		ForkJoinPool mainPool = new ForkJoinPool();
		

		// output dann als recursive task oder?
		OutputServiceRunnable outputCallable = new OutputServiceRunnable();
		ExecutorService singleThrededPoolForOutput = Executors.newSingleThreadExecutor();
		singleThrededPoolForOutput.submit(outputCallable);



        // task anlegen, gibt wohl nur eine
		TraverseTask traverseTask = new TraverseTask(rootDirectory, fileExtension, outputCallable, singleThrededPoolForOutput, mainPool);


		mainPool.execute(traverseTask); // RootTask asynchron ausführen

		Histogram resultHistogram = new Histogram();




		try {
			// get blockiert immer außerhalb von ForkJoinTasks, eben bis erg in future fertig ist

			// wtf warum muss das gecasted werden? Klasse und compute haben Histogram als Datentyp

					// das wirft ja nix, wie damit umgehen? mit booleans?

			resultHistogram =  traverseTask.get();
			outputCallable.put(new Message(MessageType.FINISH));
		} catch (InterruptedException e) {
			//TODo
			// outPutPool gleich schonmal zum shutdown auffordern, dass der nix neues mehr nimmt?
			// und dann später normale beenden Prozedur?
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} catch (ExecutionException e) {
			//todo
			// throwen oder null returnen? beides verhindert return eines histograms?
			// gleiche Thematik beim MasterCallable
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} finally {
			// korrektes herunterfahren des masterServiceThreadpools so aus Übung kopiert
			mainPool.shutdown();
			try {
				// Wait a while for existing tasks to terminate
				if (!mainPool.awaitTermination(60, TimeUnit.MILLISECONDS)) {
					mainPool.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!mainPool.awaitTermination(60, TimeUnit.MILLISECONDS))
						System.err.println("Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				mainPool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}


		singleThrededPoolForOutput.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!singleThrededPoolForOutput.awaitTermination(60, TimeUnit.MILLISECONDS)) {
				singleThrededPoolForOutput.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!singleThrededPoolForOutput.awaitTermination(60, TimeUnit.MILLISECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			singleThrededPoolForOutput.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}


		return resultHistogram;




	}

	@Override
	public void setIoExceptionThrown(boolean value) {

	}

	@Override
	public String toString() {
		return "ForkJoinHistogramService";
	}

}
