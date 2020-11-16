package de.uniba.wiai.dsg.pks.assignment;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.uniba.wiai.dsg.pks.assignment.console.SyncConsoleExecutor;
import de.uniba.wiai.dsg.pks.assignment.gui.HistogramApplication;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import de.uniba.wiai.dsg.pks.assignment.shared.HistogramExecutor;
import de.uniba.wiai.dsg.pks.assignment1.histogram.sequential.SequentialHistogramService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.highlevel.HighlevelHistogramService;
import de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel.LowlevelHistogramService;
import javafx.application.Application;

public class Main {

	// Global state of the program
	private static final Map<Service, HistogramService> serviceMap = new ConcurrentHashMap<>();

	/**
	 * Returns all services for the mentioned assignment. Initializing the map
	 * within main-method throws an error when executing the program via gradle.
	 * <br/>
	 * The returned map is thread safe.
	 * 
	 * @return
	 */
	public static Map<Service, HistogramService> getServiceMap() {
		if (serviceMap.isEmpty()) {
			initServiceMap();
		}
		return serviceMap;
	}

	private static void initServiceMap() {
		// used for GUI and CONSOLE mode
		// assignment 1
		serviceMap.putIfAbsent(Service.SEQUENTIAL, new SequentialHistogramService());
		serviceMap.putIfAbsent(Service.LOW_LEVEL, new LowlevelHistogramService());
		serviceMap.putIfAbsent(Service.HIGH_LEVEL, new HighlevelHistogramService());

//		// assignment 2
//		serviceMap.putIfAbsent(Service.EXECUTOR, new ExecutorHistogramService());
//		serviceMap.putIfAbsent(Service.FORK_JOIN, new ForkJoinHistogramService());
//		serviceMap.putIfAbsent(Service.STREAM, new StreamHistogramService());

//		// assignment 3
//		serviceMap.putIfAbsent(Service.SOCKET, new SocketHistogramService("localhost", 1337))

//		// assignment 4
//		serviceMap.putIfAbsent(Service.ACTOR, new ActorHistogramService());
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: MODE(GUI or CONSOLE) ROOT_DIRECTORY FILE_EXTENSION");
			System.exit(-1);
		}

		final String mode = args[0];
		final String rootDirectory = args[1];
		final String fileExtension = args[2];

		if ("GUI".equals(mode)) {
			Application.launch(HistogramApplication.class, args);
		} else if ("CONSOLE".equals(mode)) {
			SyncConsoleExecutor syncConsoleExecutor = new SyncConsoleExecutor(new ArrayList<HistogramService>(getServiceMap().values()),
					new HistogramExecutor(rootDirectory, fileExtension));
			syncConsoleExecutor.execute();
		} else {
			System.err.println("Wrong application mode: Choose 'GUI' or 'CONSOLE'");
			System.exit(-1);
		}
	}
}
