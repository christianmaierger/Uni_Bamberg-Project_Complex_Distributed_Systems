package de.uniba.wiai.dsg.pks.assignment.gui;

import java.util.function.Consumer;

import de.uniba.wiai.dsg.pks.assignment.model.Service;

public class ExecutionsGUIController {

	private final ExecutionsGUIModel executionsModel;
	private ExecutionsGUIView executionsView;

	public static ExecutionsGUIController createExecutionController(ExecutionsGUIModel executionsModel) {
		ExecutionsGUIController executionsController = new ExecutionsGUIController(executionsModel);
		ExecutionsGUIView executionsView = new ExecutionsGUIView(executionsModel, executionsController);
		executionsController.setExecutionsView(executionsView);
		return executionsController;
	}

	private void setExecutionsView(ExecutionsGUIView executionsView) {
		this.executionsView = executionsView;
	}

	private ExecutionsGUIController(ExecutionsGUIModel executionsModel) {
		super();
		this.executionsModel = executionsModel;
	}

	public void show() {
		executionsView.show();
	}

	public void startServiceRun(Service serviceName) {
		executionsModel.disableServiceStart();

		Consumer<Void> onRunFinished = (Void) -> {
			executionsModel.disableStopService(serviceName);
			executionsModel.enableServiceStart();
		};
		executionsModel.startServiceRun(serviceName, onRunFinished);

		executionsModel.enableStopService(serviceName);
	}

	public void stopServiceRun(Service serviceName) {
		executionsModel.interruptServiceRun(serviceName);
	}

}
