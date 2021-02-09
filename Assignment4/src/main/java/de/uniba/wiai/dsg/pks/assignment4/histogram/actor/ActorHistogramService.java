package de.uniba.wiai.dsg.pks.assignment4.histogram.actor;

import akka.ConfigurationException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors.ProjectActor;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.ReturnResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        validateInput(rootDirectory, fileExtension);
        ActorSystem actorSystem = getActorSystem();
        ActorRef projectActor = actorSystem.actorOf(ProjectActor.props(), "ProjectActor");
        ParseDirectory parseDirectory = new ParseDirectory(rootDirectory, fileExtension);
        CompletableFuture<Object> future = Patterns.ask(projectActor, parseDirectory, Duration.ofSeconds(60))
                .toCompletableFuture();
        try {
            Object returnResult = future.get();
            Histogram result = ((ReturnResult) returnResult).getHistogram();
            actorSystem.terminate();
            return result;
        } catch (ClassCastException classCastException) {
            actorSystem.terminate();
            throw new HistogramServiceException("Wrong message type was returned.");
        } catch (InterruptedException interruptedException) {
            actorSystem.terminate();
            throw new HistogramServiceException("Execution has been interrupted.");
        } catch (ExecutionException executionException) {
            actorSystem.terminate();
            throw new HistogramServiceException(executionException.getMessage(), executionException.getCause());
        }
    }

    private ActorSystem getActorSystem() throws HistogramServiceException {
        try {
            return ActorSystem.create();
        } catch (ConfigurationException configurationException) {
            throw new HistogramServiceException("Start up failed due to interrupt.");
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

    private void validateInput(String rootDirectory, String fileExtension) throws HistogramServiceException {
        if (Objects.isNull(rootDirectory) || Objects.isNull(fileExtension)) {
            throw new HistogramServiceException("Root directory or file extension is null.");
        }
        if (rootDirectory.isBlank() || fileExtension.isBlank()) {
            throw new HistogramServiceException("Root directory or file extension is empty.");
        }
        Path rootPath = Paths.get(rootDirectory);
        if (!Files.exists(rootPath)) {
            throw new HistogramServiceException("Root directory does not exist.");
        }
        if (!Files.isDirectory(rootPath)) {
            throw new HistogramServiceException("Root directory is not a directory");
        }
    }
}