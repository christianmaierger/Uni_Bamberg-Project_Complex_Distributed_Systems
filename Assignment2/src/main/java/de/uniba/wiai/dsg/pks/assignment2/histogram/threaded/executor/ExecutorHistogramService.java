package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.OutputServiceCallable;
import net.jcip.annotations.GuardedBy;
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



		// neuen ThreadPool erzeugen und callable wohl master starten, das dann alles handelt und callables für Verzeichnisse startet
		ExecutorService masterExcecutor = Executors.newCachedThreadPool();




		MasterCallable masterCallable = new MasterCallable(masterExcecutor, rootDirectory, fileExtension);
		Future<Histogram> result;
		result = masterExcecutor.submit(masterCallable);


		// auf wert aus master callable warten und dann shutdown denk ich mal!
		// das get hier blockiert ja dann erst
	// try catch aus übung um schön das Zeug was get werfen kann abzufangen

		Histogram resultHistogram = new Histogram();
		// was amchen bei IO Exeption?


		try {
			// get blockiert immer außerhalb von ForkJoinTasks, eben bis erg in future fertig ist
			resultHistogram = result.get();

		} catch (InterruptedException e) {
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} catch (ExecutionException e) {
			//todo anderer print
			throw new HistogramServiceException("Execution has been interrupted.", e);
		} finally {
			// korrektes herunterfahren mit direktem Abbruch der Verarbeitung
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
