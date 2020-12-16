package de.uniba.wiai.dsg.pks.assignment.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import de.uniba.wiai.dsg.pks.assignment.Main;
import de.uniba.wiai.dsg.pks.assignment.model.Result;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment.shared.HistogramExecutor;
import de.uniba.wiai.dsg.pks.assignment.shared.Stopwatch;

public class AsyncInterruptableExecutor {

	private final Map<Service, Thread> runningSolutions;
	private final Map<Service, Result> resultMap;
	private final Stopwatch interruptionStopwatch;

	private final ExecutorService executor;
	private final HistogramExecutor histogramExecutor;

	public AsyncInterruptableExecutor(HistogramExecutor histogramExecutor) {
		super();
		this.runningSolutions = new ConcurrentHashMap<>();
		this.resultMap = new ConcurrentHashMap<>();
		this.interruptionStopwatch = new Stopwatch();

		this.executor = Executors.newCachedThreadPool();
		this.histogramExecutor = histogramExecutor;
	}

	public void execute(Service service, BiConsumer<Result, Optional<String>> onResultAvailable) {
		Runnable r = new Runnable() {
			public void run() {
				resultMap.put(service, histogramExecutor.calculateHistogram(Main.getServiceMap().get(service)));
			}
		};
		Thread handle;
		synchronized (this.runningSolutions) {
			if (this.runningSolutions.size() != 0) {
				// allow only one running solution
				histogramExecutor.printLine("There is already a service running!");
				return;
			}
			handle = new Thread(r);
			handle.start();
			runningSolutions.put(service, handle);
		}

		// wait for termination
		this.executor.execute(new Runnable() {
			public void run() {
				while (true) {
					try {
						handle.join();
						Optional<String> interruptionTime = Optional.empty();
						if (interruptionStopwatch.isStarted()) {
							interruptionStopwatch.stop();
							interruptionTime = Optional.of(interruptionStopwatch.formattedDiff());
						}
						interruptionStopwatch.reset();
						Result res = resultMap.get(service);
						histogramExecutor.printSummary(res);
						onResultAvailable.accept(res, interruptionTime);
						// remove already terminated solution
						runningSolutions.remove(service);
						break;
					} catch (InterruptedException e) {
						// Ignore when waiting for join
					}
				}
			}
		});
	}

	public void printResultsSummary() {
		// show summary
		List<Result> resultList = new ArrayList<>();
		for (Service key : resultMap.keySet()) {
			resultList.add(resultMap.get(key));
		}
		this.histogramExecutor.printResultsSummary(resultList);
	}

	public void interrupt(Service name) {
		Thread handle = this.runningSolutions.get(name);
		if (handle == null) {
			histogramExecutor.printError(name.getText() + " is not running!");
			return;
		}

		histogramExecutor.printLine(name.getText() + " is interrupted!");
		this.interruptionStopwatch.start();
		handle.interrupt();
	}

	// Copied from the Java API
	public void shutdownAndAwaitTermination() {
		executor.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
				executor.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!executor.awaitTermination(60, TimeUnit.SECONDS))
					histogramExecutor.printLine("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
