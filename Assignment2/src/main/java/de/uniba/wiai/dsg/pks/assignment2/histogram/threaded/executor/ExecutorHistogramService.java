package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.shared.MasterThread;

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

		//ioExceptionThrown = false;
		Histogram histogram = new Histogram();

		// neuen ThreadPool erzeugen und callable wohl master starten, das dann alles handelt und callables für Verzeichnisse startet
		ExecutorService masterExcecutor = Executors.newCachedThreadPool();


		MasterCallable masterCallable = new MasterCallable(masterExcecutor, rootDirectory, fileExtension);
		Future<Histogram> masterResultFuture = masterExcecutor.submit(masterCallable);

		// überlegen ob man hier was machen soll, während master callable läuft


		// auf wert aus master callable warten und dann shutdown denk ich mal!
		// das get hier blockiert ja dann erst
	// try catch aus übung um schön das Zeug was get werfen kann abzufangen


		// was amchen bei IO Exeption?
		try {
			// get blockiert immer außerhalb von ForkJoinTasks, eben bis erg in future fertig ist
			Histogram resultHistogram = masterResultFuture.get();

		} catch (InterruptedException e) {
			e.printStackTrace();
			// zb minvalue um auf Fehler hinzuweisen, hier besseres zu überlegen
			//TODo
			throw new HistogramServiceException("Execution has been interrupted.");
		} catch (ExecutionException e) {
			//todo
			throw new HistogramServiceException("Execution has been interrupted.");
		} finally {

			// korrektes herunterfahren des masterServiceThreadpools so aus Übung kopiert
			masterExcecutor.shutdown();
			try {
				// Wait a while for existing tasks to terminate
				if (!masterExcecutor.awaitTermination(60, TimeUnit.SECONDS)) {
					masterExcecutor.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!masterExcecutor.awaitTermination(60, TimeUnit.SECONDS))
						System.err.println("Pool did not terminate");
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				masterExcecutor.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}

		return histogram;
	}

	@Override
	public String toString() {
		return "ExecutorHistogramService";
	}

}
