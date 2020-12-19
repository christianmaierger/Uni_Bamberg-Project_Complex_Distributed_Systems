package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MasterThread;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Message;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceCallable;
import net.jcip.annotations.GuardedBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

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



		// neuen ThreadPool erzeugen und callable wohl master starten, das dann alles handelt und callables für Verzeichnisse startet
		ExecutorService masterExcecutor = Executors.newCachedThreadPool();

		// ich denke es ist besser den outPut hier zu starten fürs shutwown management
		OutputServiceCallable outputCallable = new OutputServiceCallable();
		ExecutorService singleThrededPoolForOutput = Executors.newSingleThreadExecutor();
		singleThrededPoolForOutput.submit(outputCallable);

		MasterCallable masterCallable = new MasterCallable(masterExcecutor, rootDirectory, fileExtension, outputCallable);
		Future<Histogram> result;
		result = masterExcecutor.submit(masterCallable);

		// überlegen ob man hier was machen soll, während master callable läuft


		// auf wert aus master callable warten und dann shutdown denk ich mal!
		// das get hier blockiert ja dann erst
	// try catch aus übung um schön das Zeug was get werfen kann abzufangen

		Histogram resultHistogram = new Histogram();
		// was amchen bei IO Exeption?

		// abfarage macht doch keinen SIn oder? get wartet ja eh, wenns noch nicht da ist
		// kann ich Future.cancel() irgendwie brauchen, eigentlich nicht? wenn get nicht klappt ist ja eh schluss mit der Task?
		boolean resultIsDone = result.isDone();


		try {
			// get blockiert immer außerhalb von ForkJoinTasks, eben bis erg in future fertig ist
			resultHistogram = result.get();

		} catch (InterruptedException e) {
			//TODo
			// outPutPool gleich schonmal zum shutdown auffordern, dass der nix neues mehr nimmt?
			// und dann später normale beenden Prozedur?
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} catch (ExecutionException e) {
			//todo
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} finally {
			// korrektes herunterfahren des masterServiceThreadpools so aus Übung kopiert
			masterExcecutor.shutdown();
			try {
				// Wait a while for existing tasks to terminate
				if (!masterExcecutor.awaitTermination(60, TimeUnit.MILLISECONDS)) {
					masterExcecutor.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!masterExcecutor.awaitTermination(60, TimeUnit.MILLISECONDS))
						System.err.println("Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				masterExcecutor.shutdownNow();
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
		return "ExecutorHistogramService";
	}

}
