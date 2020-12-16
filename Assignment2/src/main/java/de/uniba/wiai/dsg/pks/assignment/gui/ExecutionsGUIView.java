package de.uniba.wiai.dsg.pks.assignment.gui;

import de.uniba.wiai.dsg.pks.assignment.model.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ExecutionsGUIView extends Stage {
	private final ExecutionsGUIModel executionsModel;
	private final ExecutionsGUIController executionsController;

	public ExecutionsGUIView(ExecutionsGUIModel executionsModel, ExecutionsGUIController executionsController) {

		this.executionsModel = executionsModel;
		this.executionsController = executionsController;

		BorderPane rootPane = new BorderPane();

		GridPane gridPane = new GridPane();

		// make it look a little better
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		for (Service service : executionsModel.getAvailableServices()) {
			addHistogramExecutionControls(gridPane, service);
		}

		rootPane.setCenter(gridPane);

		Scene scene = new Scene(rootPane);
		this.setScene(scene);

		this.setMinWidth(800);
		this.setTitle("PKS - GUI Mode");
		this.getIcons().add(new Image(ExecutionsGUIView.class.getResourceAsStream("/icon.png")));
	}

	private void addHistogramExecutionControls(GridPane gridPane, Service name) {

		Label time = new Label();
		executionsModel.registerTimeValue(name, time.textProperty());

		Label success = new Label();
		executionsModel.registerSuccessValue(name, success.textProperty());

		Button start = new Button("Start");
		executionsModel.registerStartButton(start.disableProperty());
		start.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				executionsController.startServiceRun(name);
			}
		});
		Button stop = new Button("Interrupt");
		executionsModel.registerStopButton(name, stop.disableProperty());
		stop.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				executionsController.stopServiceRun(name);
			}
		});

		int row = gridPane.getRowCount();
		gridPane.add(new Label("  " + name.getText() + "  "), 0, row);
		gridPane.add(start, 1, row);
		gridPane.add(stop, 2, row);
		gridPane.add(time, 3, row);
		gridPane.add(success, 4, row);
	}
}
