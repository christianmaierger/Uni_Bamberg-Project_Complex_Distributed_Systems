package de.uniba.wiai.dsg.pks.assignment.gui;

import java.util.ArrayList;
import java.util.List;

import de.uniba.wiai.dsg.pks.assignment.Main;
import de.uniba.wiai.dsg.pks.assignment.shared.HistogramExecutor;
import javafx.application.Application;
import javafx.stage.Stage;

public class HistogramApplication extends Application {

	private AsyncInterruptableExecutor aysncInterruptableExecutor;
	private ExecutionsGUIModel executionsModel;

	public HistogramApplication() {
		super();
	}

	@Override
	public void init() {
		List<String> args = this.getParameters().getUnnamed();
		this.aysncInterruptableExecutor = new AsyncInterruptableExecutor(
				new HistogramExecutor(args.get(1), args.get(2)));
		this.executionsModel = new ExecutionsGUIModel(this.aysncInterruptableExecutor,
				new ArrayList<>(Main.getServiceMap().keySet()));
	}

	@Override
	public void start(Stage mainStage) throws Exception {
		ExecutionsGUIController executionsController = ExecutionsGUIController
				.createExecutionController(this.executionsModel);
		executionsController.show();
	}

	@Override
	public void stop() {
		aysncInterruptableExecutor.shutdownAndAwaitTermination();
		aysncInterruptableExecutor.printResultsSummary();
	}

}
