package de.uniba.wiai.dsg.pks.assignment.console;

import java.util.ArrayList;
import java.util.List;

import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.Result;
import de.uniba.wiai.dsg.pks.assignment.shared.HistogramExecutor;

public class SyncConsoleExecutor{
	
	private final List<HistogramService> services;
	private final HistogramExecutor histogramExecutor;
	
	public SyncConsoleExecutor(List<HistogramService> services, HistogramExecutor histogramExecutor) {
		super();
		this.services = services;
		this.histogramExecutor = histogramExecutor;
	}
	
	public void execute() {
		List<Result> resultList = computeResults();
		histogramExecutor.printResultsSummary(resultList);
	}

	private List<Result> computeResults() {
		List<Result> results = new ArrayList<>();
		for(HistogramService service : this.services) {
			results.add(histogramExecutor.calculateHistogram(service));
		}
		return results;
	}
	
}
