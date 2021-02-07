package de.uniba.wiai.dsg.pks.assignment4.histogram.actor;

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
		ActorSystem actorSystem = ActorSystem.create();
		ActorRef projectActor = actorSystem.actorOf(ProjectActor.props(rootDirectory, fileExtension), "ProjectActor");
		projectActor.tell(new Histogram(), ActorRef.noSender());
		CompletableFuture<Object> future =
				Patterns.ask(projectActor, new HistogramRequest(), Duration.ofSeconds(30)).toCompletableFuture();
		try{
			Object returnResult = future.get();
			if(returnResult instanceof ReturnResult){
				Histogram result = ((ReturnResult) returnResult).getHistogram();
				if(Objects.isNull(result)){
					throw new HistogramServiceException("No histogram is present.");
				} else {
					return result;
				}
			} else {
				throw new HistogramServiceException("Wrong message type was returned.");
			}
		} catch (InterruptedException | ExecutionException interruptedException) {
			//TODO: Add shutdown policy
			throw new HistogramServiceException(interruptedException.getMessage(), interruptedException.getCause());
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
