package de.uniba.wiai.dsg.pks.assignment1.histogram.sequential;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.OutputService;
import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@NotThreadSafe
public class SequentialHistogramService implements HistogramService {
	private Histogram histogram;
	private OutputService outputService;

	public SequentialHistogramService() {
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

		histogram = new Histogram();
		outputService = new OutputService();

		try{
			processDirectory(rootDirectory, fileExtension);
			incrementNumberOfDirectories();
			outputService.logProcessedDirectory(rootDirectory, histogram);
		} catch (InterruptedException | IOException exception) {
			throw new HistogramServiceException(exception.getMessage());
		}

		return histogram;
	}

	@Override
	public String toString() {
		return "SequentialHistogramService";
	}

	/**
	 * Scans a directory with all its subdirectories and processes the content with respect to statistical measures and
	 * letter distribution. Also, logging messages are printed to console.
	 *
	 * It updates the histogram with respect to:
	 *  - number of processed directories
	 * 	- number of files in general (not processed)
	 *
	 * @param rootDirectory directory to scan
	 * @param fileExtension file extension of file that shall be processed
	 */
	private void processDirectory(String rootDirectory, String fileExtension) throws InterruptedException, IOException {
		Path folder = Paths.get(rootDirectory);
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder)){
			for(Path path: stream){
				if(Thread.currentThread().isInterrupted()){
					throw new InterruptedException("Execution has been interrupted.");
				}
				if (Files.isDirectory(path)){
					processDirectory(path.toString(), fileExtension);
					incrementNumberOfDirectories();
					outputService.logProcessedDirectory(path.toString(), histogram);
				} else if (Files.isRegularFile(path)){
					incrementNumberOfFiles();
					if (path.getFileName().toString().endsWith(fileExtension)){
						List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
						processFile(lines);
						outputService.logProcessedFile(path.toString());
					}
				}
			}
		} catch (IOException io){
			throw new IOException( "I/O error occurred while reading folders and files.");
		}

	}

	/**
	 * Takes a file represented as a List of Strings and counts its lines as well as each letter.
	 * It updates the histogram with respect to:
	 * - number of lines processed
	 * - number of processed files
	 * - distribution of letters found in the processed file
	 *
	 * @param lines the lines which together form a file
	 */
	private void processFile(List<String> lines){
		int linesInFile = lines.size();
		addToNumberOfLines(linesInFile);
		incrementNumberOfProcessedFiles();
		for (String line: lines) {
			countLettersInLine(line);
		}
	}

	private void countLettersInLine(String line){
		for(int x = 0; x < line.length(); x++){

			char character = line.charAt(x);
			int asciiValue = (int) character;

			if(asciiValue >= 'A' && asciiValue <= 'Z'){
				incrementDistributionAtX(asciiValue - 'A');
			}
			if(asciiValue >= 'a' && asciiValue <= 'z'){
				incrementDistributionAtX(asciiValue - 'a');
			}
		}
	}

	private void incrementNumberOfFiles(){
		histogram.setFiles(histogram.getFiles() + 1);
	}

	private void incrementNumberOfProcessedFiles(){
		histogram.setProcessedFiles(histogram.getProcessedFiles() + 1);
	}

	private void addToNumberOfLines(int x){
		histogram.setLines(histogram.getLines() + x);
	}

	private void incrementNumberOfDirectories(){
		histogram.setDirectories(histogram.getDirectories() + 1);
	}

	private void incrementDistributionAtX(int x){
		histogram.getDistribution()[x]++;
	}
}
