package de.uniba.wiai.dsg.pks.assignment.shared;

public class Stopwatch {

	public static void main(String[] args) {
		System.out.println("Example use of Stopwatch");

		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();

		try {
			Thread.sleep(1001);
		} catch (InterruptedException ignore) {

		}

		stopwatch.stop();

		System.out.println(stopwatch);
	}

	private long start;
	private long stop;

	public void start() {
		start = System.currentTimeMillis();
	}

	public void stop() {
		stop = System.currentTimeMillis();
	}

	public long diff() {
		return stop - start;
	}

	public void reset() {
		start = 0;
		stop = 0;
	}

	public boolean isStarted() {
		return start != 0;
	}

	/**
	 * @return formatted diff
	 */
	public String formattedDiff() {
		long seconds = diff() / 1000;
		long milliseconds = diff() % 1000;

		return String.format("%d.%03ds", seconds, milliseconds);
	}

	@Override
	public String toString() {
		return "Duration: " + formattedDiff();
	}

}
