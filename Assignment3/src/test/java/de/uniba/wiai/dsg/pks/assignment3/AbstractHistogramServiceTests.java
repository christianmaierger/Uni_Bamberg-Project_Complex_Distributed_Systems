package de.uniba.wiai.dsg.pks.assignment1;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;


public abstract class AbstractHistogramServiceTests {

	private static int COUNT_ROOT_FOLDER = 1;

	protected HistogramService histogramService;

	@Test
	public void testNoFiles() throws HistogramServiceException, URISyntaxException, IOException {
		// make sure empty folder exists (will only be created at test runtime, not in src/main/resources folder)
		Path source = Path.of(this.getClass().getClassLoader().getResource(".").toURI());
		Path newFolder = Paths.get(source.toAbsolutePath() + "/data/noFiles/input/subfolder");
		Files.createDirectories(newFolder);

		assertEquals(getHistogramString(new Histogram(new long[26], 0, 0, 0, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(Paths
						.get(this.getClass().getClassLoader().getResource("data/noFiles/input").toURI()).toString(),
						".txt")));

	}

	@Test
	public void testSimple() throws HistogramServiceException, URISyntaxException {
		assertEquals(
				getHistogramString(new Histogram(
						new long[] { 5, 0, 0, 3, 10, 2, 0, 2, 6, 0, 0, 5, 5, 1, 0, 4, 0, 2, 9, 4, 0, 1, 0, 1, 3, 0 }, 7,
						5, 4, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(
						Paths.get(this.getClass().getClassLoader().getResource("data/simple/input").toURI()).toString(),
						".txt")));
	}

	@Test
	public void testSimpleXml() throws HistogramServiceException, URISyntaxException {
		assertEquals(
				getHistogramString(new Histogram(
						new long[] { 2, 0, 0, 0, 5, 0, 0, 0, 3, 0, 0, 7, 3, 0, 0, 3, 0, 2, 3, 0, 0, 0, 0, 0, 2, 0 }, 1,
						5, 1, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(
						Paths.get(this.getClass().getClassLoader().getResource("data/simple/input").toURI()).toString(),
						".xml")));
	}

	@Test
	public void testSimpleNoFiles() throws HistogramServiceException, URISyntaxException {
		assertEquals(getHistogramString(new Histogram(new long[26], 0, 5, 0, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(
						Paths.get(this.getClass().getClassLoader().getResource("data/simple/input").toURI()).toString(),
						".xsd")));

	}

	@Test
	public void testOnlyInSubfolders() throws HistogramServiceException, URISyntaxException {
		assertEquals(
				getHistogramString(new Histogram(
						new long[] { 5, 0, 0, 3, 10, 2, 0, 2, 6, 0, 0, 5, 5, 1, 0, 4, 0, 2, 9, 4, 0, 1, 0, 1, 3, 0 }, 7,
						5, 4, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(
						Paths.get(this.getClass().getClassLoader().getResource("data/onlyInSubfolders/input").toURI())
								.toString(),
						".txt")));

	}

	@Test
	public void testOnlyInSubfoldersXml() throws HistogramServiceException, URISyntaxException {
		assertEquals(
				getHistogramString(new Histogram(
						new long[] { 2, 0, 0, 0, 5, 0, 0, 0, 3, 0, 0, 7, 3, 0, 0, 3, 0, 2, 3, 0, 0, 0, 0, 0, 2, 0 }, 1,
						5, 1, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(
						Paths.get(this.getClass().getClassLoader().getResource("data/onlyInSubfolders/input").toURI())
								.toString(),
						".xml")));

	}

	@Test
	public void testOnlyInSubfoldersNoFiles() throws HistogramServiceException, URISyntaxException {
		assertEquals(getHistogramString(new Histogram(new long[26], 0, 5, 0, 1 + COUNT_ROOT_FOLDER)),
				getHistogramString(histogramService.calculateHistogram(
						Paths.get(this.getClass().getClassLoader().getResource("data/onlyInSubfolders/input").toURI())
								.toString(),
						".xsd")));

	}

	private String getHistogramString(Histogram histogram) {
		StringBuilder result = new StringBuilder();
		long[] distribution = histogram.getDistribution();
		for (int i = 0; i < distribution.length; i++) {
			result.append("[" + i + "] " + distribution[i] + ", ");
		}
		result.append(" lines: " + histogram.getLines());
		result.append(" files: " + histogram.getFiles());
		result.append(" processedFiles: " + histogram.getProcessedFiles());
		result.append(" directories: " + histogram.getDirectories());
		return result.toString();
	}

}
