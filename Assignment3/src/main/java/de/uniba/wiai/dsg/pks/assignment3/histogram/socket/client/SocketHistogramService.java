package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.*;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;

@ThreadSafe
public class SocketHistogramService implements HistogramService {
    @GuardedBy(value = "itself")
    private final String hostname;
    @GuardedBy(value = "itself")
    private final int port;

    public SocketHistogramService(String hostname, int port) {
        // REQUIRED FOR GRADING - DO NOT CHANGE SIGNATURE
        // but you can add code below
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
        validateInput(rootDirectory, fileExtension);
        ReturnResult resultMessage;
        try (Socket server = new Socket()) {
            SocketAddress serverAddress = new InetSocketAddress(hostname, port);
            server.connect(serverAddress);
            checkForInterrupt();
            try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
                out.flush();
                sendDirectoryParseMessages(out, rootDirectory, fileExtension);
                requestResult(out);
                try (ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
                    resultMessage = receiveResult(in, out, server);
                    terminateConnection(out);
                }
            }
            checkForInterrupt();
            verifyResultIsValid(resultMessage);
            return resultMessage.getHistogram();
        } catch (IOException exception) {
            throw new HistogramServiceException(exception.getMessage(), exception.getCause());
        }
    }

    @Override
    public String toString() {
        return "SocketHistogramService";
    }

    /**
     * Unneeded legacy method from Assignment 1.
     */
    @Override
    public void setIoExceptionThrown(boolean value) {
        throw new UnsupportedOperationException();
    }

    private void sendDirectoryParseMessages(ObjectOutputStream out, String currentFolder, String fileExtension) throws HistogramServiceException {
        Path folder = Paths.get(currentFolder);
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                for (Path path : stream) {
                    //Important: put this check here, so that before all file types check is performed, not only if is Directory
                    checkForInterrupt(out);
                    if (Files.isDirectory(path)) {
                        sendDirectoryParseMessages(out, path.toString(), fileExtension);
                    }
                }
            }
            ParseDirectory parseDirectory = new ParseDirectory(currentFolder, fileExtension);
            out.writeObject(parseDirectory);
            out.flush();
        } catch (IOException exception) {
            terminateConnection(out);
            throw new HistogramServiceException(exception.getMessage(), exception.getCause());
        }
    }

    private void requestResult(ObjectOutputStream out) throws HistogramServiceException {
        try {
            out.writeObject(new GetResult());
            out.flush();
        } catch (IOException exception) {
            terminateConnection(out);
            throw new HistogramServiceException(exception.getMessage(), exception.getCause());
        }
    }

    private ReturnResult receiveResult(ObjectInputStream in, ObjectOutputStream out, Socket server) throws HistogramServiceException {
        ResultReceiver resultReceiver = new ResultReceiver(in, server);
        ExecutorService receiverExecutor = Executors.newSingleThreadExecutor();
        Future<ReturnResult> resultFuture = receiverExecutor.submit(resultReceiver);
        try {
            return resultFuture.get();
        } catch (InterruptedException | ExecutionException exception) {
            terminateConnection(out);
            throw new HistogramServiceException(exception.getMessage(), exception.getCause());
        } finally {
            shutdownAndAwaitTermination(receiverExecutor);
        }
    }

    private void terminateConnection(ObjectOutputStream out) throws HistogramServiceException {
        try {
            TerminateConnection poisonPill = new TerminateConnection();
            out.writeObject(poisonPill);
        } catch (IOException exception) {
            throw new HistogramServiceException(exception.getMessage(), exception.getCause());
        }
    }

    private void checkForInterrupt(ObjectOutputStream out) throws HistogramServiceException {
        if (Thread.currentThread().isInterrupted()) {
            terminateConnection(out);
            throw new HistogramServiceException("Execution has been interrupted.");
        }
    }

    private void checkForInterrupt() throws HistogramServiceException {
        if (Thread.currentThread().isInterrupted()) {
            throw new HistogramServiceException("Execution has been interrupted.");
        }
    }

    private void verifyResultIsValid(ReturnResult result) throws HistogramServiceException {
        if (Objects.isNull(result)) {
            throw new HistogramServiceException("No result histogram present.");
        }
    }

    private void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(3, TimeUnit.SECONDS))
                    System.err.println("Executor of ResultReceiver did not terminate.");
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void validateInput(String rootDirectory, String fileExtension) throws IllegalArgumentException {
        if(Objects.isNull(rootDirectory) || Objects.isNull(fileExtension)){
            throw new IllegalArgumentException("Root directory or file extension is null.");
        }
        if(rootDirectory.isBlank() || fileExtension.isBlank()){
            throw new IllegalArgumentException("Root directory or file extension is empty.");
        }
        Path rootPath = Paths.get(rootDirectory);
        if(!Files.exists(rootPath)){
            throw new IllegalArgumentException("Root directory does not exist.");
        }
        if(!Files.isDirectory(rootPath)){
            throw new IllegalArgumentException("Root directory is not a directory");
        }
    }
}
