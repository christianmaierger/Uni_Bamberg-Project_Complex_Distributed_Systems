package de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.executor;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import net.jcip.annotations.ThreadSafe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;


@ThreadSafe
public class ExecutorHistogramService implements HistogramService {


    public ExecutorHistogramService() {
        // REQUIRED FOR GRADING - DO NOT REMOVE DEFAULT CONSTRUCTOR
        // but you can add code below
    }

    @Override
    public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
        if (Objects.isNull(rootDirectory) || Objects.isNull(fileExtension)) {
            throw new HistogramServiceException("Neither root directory nor file extension must be null.");
        }
        if (rootDirectory.isBlank() || fileExtension.isBlank()) {
            throw new HistogramServiceException("Neither root directory nor file extension must be empty.");
        }
        Path rootPath = Paths.get(rootDirectory);
        if (!Files.exists(rootPath)) {
            throw new HistogramServiceException("Root directory does not exist.");
        }
        if (!Files.isDirectory(rootPath)) {
            throw new HistogramServiceException("Root directory must be a directory");
        }

        ExecutorService masterExecutor = Executors.newCachedThreadPool();
        MasterCallable masterCallable = new MasterCallable(masterExecutor, rootDirectory, fileExtension);
        Future<Histogram> result;
        result = masterExecutor.submit(masterCallable);
        Histogram resultHistogram;

        try {
            resultHistogram = result.get();
        } catch (InterruptedException e) {
            throw new HistogramServiceException("Execution has been interrupted.", e);
        } catch (ExecutionException e) {
            throw new HistogramServiceException(e.getMessage(), e.getCause());
        } finally {
            masterExecutor.shutdownNow();
            try {
                if (!masterExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    System.err.println("Master Executor pool did not terminate");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        return resultHistogram;
    }

    @Override
    public void setIoExceptionThrown(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "ExecutorHistogramService";
    }
}
