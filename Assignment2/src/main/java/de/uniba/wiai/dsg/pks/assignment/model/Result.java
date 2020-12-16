package de.uniba.wiai.dsg.pks.assignment.model;

import de.uniba.wiai.dsg.pks.assignment.shared.Stopwatch;

public class Result {

	private final Stopwatch stopwatch;
	private final Histogram histogram;
	private final String type;
	private final Throwable exception;

	public Result(Stopwatch stopwatch, Histogram histogram, String type) {
		super();
		this.stopwatch = stopwatch;
		this.histogram = histogram;
		this.type = type;
		this.exception = null;
	}

	public Result(Stopwatch stopwatch, String type, Throwable exception) {
		super();
		this.stopwatch = stopwatch;
		this.type = type;
		this.exception = exception;
		this.histogram = new Histogram();
	}

	public Stopwatch getStopwatch() {
		return stopwatch;
	}

	public Histogram getHistogram() {
		return histogram;
	}

	public String getType() {
		return type;
	}

	public Throwable getException() {
		return exception;
	}

}