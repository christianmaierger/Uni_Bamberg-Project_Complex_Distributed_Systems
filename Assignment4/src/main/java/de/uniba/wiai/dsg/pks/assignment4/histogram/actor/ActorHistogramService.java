package de.uniba.wiai.dsg.pks.assignment4.histogram.actor;

import akka.ConfigurationException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors.ProjectActor;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.HistogramRequest;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ReturnResult;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ActorHistogramService implements HistogramService {

	public ActorHistogramService() {
		// REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
		// but you can add code below
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		ActorSystem actorSystem;
		try{
			actorSystem = ActorSystem.create();
		} catch (ConfigurationException configurationException){
			throw new HistogramServiceException("Start up failed due to interrupt");
		}
		ActorRef projectActor = actorSystem.actorOf(ProjectActor.props(rootDirectory, fileExtension), "ProjectActor");
		CompletableFuture<Object> future =
				Patterns.ask(projectActor, new HistogramRequest(), Duration.ofSeconds(60)).toCompletableFuture();

		try{
			Object returnResult = future.get();
			Histogram result = ((ReturnResult) returnResult).getHistogram();
			if (Objects.nonNull(result)) {
				actorSystem.terminate();
				return result;
			} else {
				throw new HistogramServiceException("No histogram is present.");
			}
		} catch (ClassCastException classCastException){
			actorSystem.terminate();
			throw new HistogramServiceException("Wrong message type was returned.");
		} catch (InterruptedException interruptedException) {
			actorSystem.terminate();
			throw new HistogramServiceException("Execution has been interrupted.");
		} catch (ExecutionException executionException){
			//FIXME: Einfach bei Exception alles plattmachen. Ist das ok?
			actorSystem.terminate();
			throw new HistogramServiceException(executionException.getMessage(), executionException.getCause());
		}
	}

	@Override
	public void setIoExceptionThrown(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
