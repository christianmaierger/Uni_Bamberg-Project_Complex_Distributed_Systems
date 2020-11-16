package de.uniba.wiai.dsg.pks.assignment.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import de.uniba.wiai.dsg.pks.assignment.model.Result;
import de.uniba.wiai.dsg.pks.assignment.model.Service;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExecutionsGUIModel {

	private final AsyncInterruptableExecutor asyncInterruptableExecutor;

	// LinkedHashMap provides insertion-order iteration
	private final Map<Service, Optional<Result>> results = new LinkedHashMap<>();
	private final Map<Service, StringProperty> timeResults = new HashMap<>();
	private final Map<Service, StringProperty> successResultFields = new HashMap<>();
	private final BooleanProperty startServiceDisabled;
	private final Map<Service, BooleanProperty> stopServiceDisabled = new HashMap<>();

	public ExecutionsGUIModel(AsyncInterruptableExecutor guiExecutor, List<Service> availableServices) {
		super();
		this.asyncInterruptableExecutor = guiExecutor;
		this.startServiceDisabled = new SimpleBooleanProperty(false);
		for (Service service : availableServices) {
			this.results.put(service, Optional.empty());
			this.timeResults.put(service, new SimpleStringProperty("#Time#"));
			this.successResultFields.put(service, new SimpleStringProperty("#Success#"));
			this.stopServiceDisabled.put(service, new SimpleBooleanProperty(true));
		}
	}

	public List<Service> getAvailableServices() {
		List<Service> availableServices = new ArrayList<Service>(results.keySet());
		Collections.sort(availableServices);
		return availableServices;
	}

	public void registerTimeValue(Service serviceName, StringProperty displayedTime) {
		displayedTime.bind(timeResults.get(serviceName));
	}

	public void registerSuccessValue(Service serviceName, StringProperty displayedSuccess) {
		displayedSuccess.bind(successResultFields.get(serviceName));
	}

	public void registerStartButton(BooleanProperty buttonState) {
		buttonState.bind(startServiceDisabled);
	}

	public void registerStopButton(Service serviceName, BooleanProperty buttonState) {
		buttonState.bind(stopServiceDisabled.get(serviceName));
	}

	public void disableServiceStart() {
		startServiceDisabled.setValue(true);
	}

	public void enableServiceStart() {
		startServiceDisabled.setValue(false);
	}

	public void enableStopService(Service serviceName) {
		stopServiceDisabled.get(serviceName).setValue(false);
	}

	public void disableStopService(Service serviceName) {
		stopServiceDisabled.get(serviceName).setValue(true);
	}

	public void startServiceRun(Service serviceName, Consumer<Void> onRunFinished) {
		BiConsumer<Result, Optional<String>> onResultAvailable = (Result result, Optional<String> interruptionTime) -> {
			Platform.runLater(() -> {
				results.put(serviceName, Optional.of(result));
				timeResults.get(serviceName).setValue(result.getStopwatch().formattedDiff());
				String time = "";
				if (interruptionTime.isPresent()) {
					time = " (interrupted " + interruptionTime.get() + ")";
				}

				if (result.getException() != null) {
					successResultFields.get(serviceName)
							.setValue(result.getException().getClass().getSimpleName() + time);
				} else {
					successResultFields.get(serviceName).setValue("Success" + time);
				}
				// Void Consumer can only be triggered with null
				onRunFinished.accept(null);
			});
		};

		// TODO reset fields
		asyncInterruptableExecutor.execute(serviceName, onResultAvailable);
	}

	public void interruptServiceRun(Service serviceName) {
		asyncInterruptableExecutor.interrupt(serviceName);
	}
}
