package de.uniba.wiai.dsg.pks.assignment1.histogram.sequential;

		import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
		import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
		import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;

		import java.nio.charset.StandardCharsets;
		import java.nio.file.DirectoryStream;
		import java.nio.file.Files;
		import java.nio.file.Path;
		import java.nio.file.Paths;
		import java.util.List;
		import java.util.concurrent.atomic.AtomicLong;

public class SequentialHistogramService implements HistogramService {
	private Histogram histogram = new Histogram();
	// vielleicht unnätig, aber dachte ich machs gleich atomic mit incrementAndGet
	AtomicLong dirCounter = new AtomicLong();
	AtomicLong fileCounter = new AtomicLong();
	AtomicLong processedFileCounter = new AtomicLong();

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
	 * Diese Methode muss eine HistogramServiceException werfen, wenn es einen Interrupt gibt. Der Interrupt
	 * muss nicht hier in dieser Methode erkannt werden, aber auf jeden Fall soll diese hier beim einem Interrupt
	 * letztlich eine HistogramServiceException werfen. Die Prozessierung sollte danach abgebrochen werden und
	 * nichts mehr auf der Console ausgegeben werden.
	 *
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
		throw new UnsupportedOperationException("Implement here");
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
	private void processDirectory(String rootDirectory, String fileExtension){
		Path folder = Paths.get(rootDirectory) ;
		 try ( DirectoryStream<Path> stream = Files.newDirectoryStream(folder) ) {
			 for ( Path path : stream ) {
				if ( Files.isDirectory(path)) {
					 // TODO DIRECTORY
					//  should be done by making dir a string and calling method recursively?
					String currentDirectory = path.toString();
					this.processDirectory(currentDirectory, fileExtension);
					histogram.setDirectories(dirCounter.incrementAndGet());
				} else if ( Files . isRegularFile ( path ) ) {
					// hier müssten dann die files incrementiert werden, ist aber ne Einfügung!?
					histogram.setFiles(fileCounter.incrementAndGet());
					 if ( path.getFileName( ).toString( ).endsWith(fileExtension)) {
					 	// kein try catch fürs Einlesen?
						 List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8 ) ;
						 // hier gleich die processedFiles increment: why not dear franzi?
						 histogram.setFiles(processedFileCounter.incrementAndGet());
						 // TODO Process lines
						 processFile(lines);
						 }
					 }
				 }
			 // dir müsste hier finished sein, log callen?
			 // ich weiß was du meinst, ausgeben, dass dir durch ist, aber finde die nicht, muss die erstellt werden?

			 }
	}

	/**
	 * Takes a file represented as a List of Strings and counts its lines as well as each letter.
	 * Afterwards, it has to call the logging method in Output Service.
	 * It updates the histogram with respect to:
	 * - letter array
	 * - number of lines
	 * - number of processed files
	 *
	 * @param lines
	 */
	private void processFile(List<String> lines){

	}
}
