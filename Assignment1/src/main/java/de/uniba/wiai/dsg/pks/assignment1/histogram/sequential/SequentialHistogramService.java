package de.uniba.wiai.dsg.pks.assignment1.histogram.sequential;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment1.histogram.shared.OutputService;
import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@NotThreadSafe
public class SequentialHistogramService implements HistogramService {
	private Histogram histogram;
	private OutputService out;

	public SequentialHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	// Was bei mehreren Thread kritisch wird:
	// - Jeder Thread muss ein anderes Verzeichnis machen, es dürfen nicht zwei Threads dasselbe Verzeichnis anschauen.
	// - Die Ausgabe der Zeilennummern am Anfang muss richtig aufsteigend sein --> Producer/Consumer Pattern nutzen
	//       von allen Threads an den Ausgabethread
	// - Es darf immer nur ein Thread gleichzeitig auf das Histogram zugreifen bzw. eben nur in atomarer
	//        Art und Weise, sonst stimmt die Zählung am Ende nicht.

	// Idee: Histogramm sollte im Master-Thread erstellt werden und dann an alle Threads gegeben werden. Bei der
	// sequentiellen Abarbeitung kann jedoch direkt oben als Variable ein neues erstellt werden, da es hier nur eins gibt.


	/**
	 *
	 * @param rootDirectory
	 *            the directory to start from
	 * @param fileExtension
	 *            the filter which files are used to compute the histogram
	 * @return
	 * @throws HistogramServiceException
	 */
	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		histogram= new Histogram();
		out = new OutputService(histogram);
		try{
			processDirectory(rootDirectory, fileExtension);
			// increment number of directories because now root directory has been processed as well also has to be printed
			incrementNumberOfDirectories();
            out.logProcessedDirectory(rootDirectory);
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
					out.logProcessedDirectory(path.toString());
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

	/**
	 * Takes a file represented as a List of Strings and counts its lines as well as each letter.
	 * It updates the histogram with respect to:
	 * - number of lines processed
	 * - number of processed files
	 * - distribution of letters found in the processed file
	 *
	 * @param lines the lines while together form a file
	 */
	private void processFile(List<String> lines){
		// lines
		int linesInFile = lines.size();
		addToNumberOfLines(linesInFile);

		// processed file
		incrementNumberOfProcessedFiles();

		// letter distribution
		for (String line: lines) {
			countLettersInLine(line);
		}
	}

	private void countLettersInLine(String line){
		for(int x = 0; x < line.length(); x++){

			char character = line.charAt(x);
			int asciiValue = (int) character;

			if(asciiValue >= 'A' && asciiValue <= 'Z'){
				// Uppercase letters to lowercase
				asciiValue = (int) String.valueOf(character).toLowerCase().toCharArray()[0];
			}
			if(asciiValue >= 'a' && asciiValue <= 'z'){
				// will only increment for lowercase letters
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
