package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.MasterThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LowlevelHistogramService implements HistogramService {
	MasterThread masterThread = new MasterThread(1);

	public LowlevelHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}


	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		try{
			processDirectory(rootDirectory, fileExtension);
			// increment number of directories because now root directory has been processed as well
			incrementNumberOfDirectories();
		} catch (InterruptedException | IOException exception) {
			throw new HistogramServiceException(exception.getMessage());
		}
		return histogram;
	}

	/**
	 * Scans a directory with the given Code Snippet 2 from the Assignment sheet and
	 * starts the processing of either directories by calling this method again or the
	 * processing of a file by calling method fileprocessing.
	 * Increments the number of processed directories by one and also calls the log-method for finished
	 * directories. Also increments the number of files in the histogram (just files, not processed files).
	 * The number of processed files is considered in the processFile method.
	 *
	 * @param rootDirectory
	 * @param fileExtension
	 */
	private void processDirectoryLowLevel(String rootDirectory, String fileExtension) throws InterruptedException, IOException {
		Path folder = Paths.get(rootDirectory);
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
			for(Path path: stream){
				if(Thread.currentThread().isInterrupted()){
					throw new InterruptedException("Execution has been interrupted.");
				}
				if (Files.isDirectory(path)){

				} else if (Files.isRegularFile(path)){
					incrementNumberOfFiles();
					if (path.getFileName().toString().endsWith(fileExtension)){
						List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
						processFile(lines);
						out.logProcessedFile(path.toString());
					}
				}
			}
		} catch (IOException io){
			throw new IOException( "I/O error occurred while reading folders and files.");
		}

	}


	@Override
	public String toString() {
		return "LowlevelHistogramService";
	}

}
