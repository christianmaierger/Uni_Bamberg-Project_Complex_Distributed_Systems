package de.uniba.wiai.dsg.pks.assignment.shared;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.Result;

import java.util.Arrays;
import java.util.List;

public class HistogramExecutor {

	private final static Object consoleLock = new Object();

	private final String path;
	private final String fileExtension;

	public HistogramExecutor(String path, String fileExtension) {
		super();
		this.path = path;
		this.fileExtension = fileExtension;
	}

	public Result calculateHistogram(HistogramService histogramService) {
		printLine("STARTING RUN WITH " + histogramService);
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		Result result;
		try {
			Histogram histogram = histogramService.calculateHistogram(path, fileExtension);
			stopwatch.stop();
			result = new Result(stopwatch, histogram, histogramService.toString());
		} catch (Exception e) {
			stopwatch.stop();
			result = new Result(stopwatch, histogramService.toString(), e);
		}

		this.printLine("FINISHED RUN WITH " + histogramService);

		return result;
	}

	public void printResultsSummary(List<Result> resultList) {
		// show summary
		printLine("\nSUMMARY\n=======\n");
		for (Result result : resultList) {
			printSummary(result);
		}
	}

	public void printSummary(Result result) {
		if (result.getException() != null) {
			this.format("%s \tFAILED \t\t%s%n", result.getType(), result.getException().getMessage());
		} else {
			this.format("%s \ttook %s \tfor %d directories and %d files from which %d have been processed with %d lines%n distribution %s\n",
					result.getType(), result.getStopwatch().formattedDiff(), result.getHistogram().getDirectories(),
					result.getHistogram().getFiles(), result.getHistogram().getProcessedFiles(), result.getHistogram().getLines(),
					Arrays.toString(result.getHistogram().getDistribution()));
		}
	}

	public void printLine(String s) {
		synchronized (consoleLock) {
			System.out.println(s);
		}
	}

	public void printError(String s) {
		synchronized (consoleLock) {
			System.err.println(s);
		}
	}

	public void format(String format, Object... args) {
		synchronized (consoleLock) {
			System.out.format(format, args);
		}
	}
}