package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.messageprocessing.DirectoryProcessor;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.messageprocessing.ResultCalculator;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TCPClientHandler implements ClientHandler {
    private final int number;
    private final Socket clientSocket;
    private final DirectoryServer parentServer;

    private final Histogram histogram = new Histogram();
    private final List<Future<Histogram>> futureList = new ArrayList<>();
    private final Semaphore semaphore = new Semaphore(1, true);
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public TCPClientHandler(Socket socket, DirectoryServer parentServer, int number) {
        this.clientSocket = socket;
        this.parentServer = parentServer;
        this.number = number;
    }

    @Override
    public void run() {
        printToOut("Connection established to a new client.");
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            out.flush();
            try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                clientSocket.setSoTimeout(1000);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Object object = in.readObject();
                        if (object instanceof ParseDirectory) {
                            printToOut("Received a message: parse request.");
                            process((ParseDirectory) object);
                        } else if (object instanceof GetResult) {
                            printToOut("Received a message: result request.");
                            ResultCalculator resultCalculator = new ResultCalculator(out, this, number);
                            threadPool.submit(resultCalculator);
                        } else if (object instanceof TerminateConnection) {
                            printToOut("Received a message: Client terminated connection.");
                            process((TerminateConnection) object);
                        } else {
                            //TODO: Dieser Printout ist evtl nicht nötig, je nachdem wann die ClassNotFoundEx feuert..?
                            printToErr("Received a message: Unknown message type. Message was ignored.");
                        }
                    } catch (SocketTimeoutException exception) {
                        continue;
                    }
                }
            }
        } catch (IOException ioException) {
            //TODO: Wann kann die hier überhaupt geworfen werden? Dann ist ja eigentlich der Stream im Arsch, oder? --> shutdown
            printToErr("IOException: " + ioException.getMessage() + ".");
        } catch (ClassNotFoundException classNotFoundException) {
            //TODO: Wann kann die hier überhaupt geworfen werden? Selbst mit der unbekannten Klasse Test passiert das nicht...
            printToErr("ClassNotFoundException: " + classNotFoundException.getMessage() + ".");
        } finally {
            shutdownAndAwaitTermination();
        }
    }

    @Override
    public void process(ParseDirectory parseDirectory) {
        DirectoryProcessor directoryProcessor = new DirectoryProcessor(parseDirectory, parentServer, this);
        Future<Histogram> histogramFuture = threadPool.submit(directoryProcessor);
        futureList.add(histogramFuture);
    }

    @Override
    public ReturnResult process(GetResult getResult) {
        for (Future<Histogram> future : futureList) {
            try {
                future.get();
            } catch (InterruptedException exception) {
                shutdownAndAwaitTermination();
            } catch (ExecutionException exception) {
                if (exception.getCause() instanceof InterruptedException) {
                    shutdownAndAwaitTermination();
                } else {
                    printToErr("Exception in DirectoryProcessor: " + exception.getMessage());
                    printToErr("Result is corrupt. Null is sent to client instead of ReturnResult.");
                    return null;
                    //TODO: macht das sinn? Return null, weil hier zwangsweise etwas zurückgegeben werden muss und
                    // es nach einem IO Fehler keine Möglichkeit mehr gibt, ein korrektes Histogramm zurückzugeben.
                }
            }
        }
        return new ReturnResult(this.histogram);
    }

    @Override
    public void process(TerminateConnection terminateConnection) {
        Thread.currentThread().interrupt();
    }

    public void addToHistogram(Histogram inputHistogram) throws InterruptedException {
        try {
            semaphore.acquire();
            DirectoryUtils.addUpAllFields(this.histogram, inputHistogram);
        } finally {
            semaphore.release();
        }
    }

    private void shutdownAndAwaitTermination() {
        printToOut("Initiate shutdown.");
        parentServer.disconnect(this);
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    printToErr("ThreadPool did not terminate.");
                } else {
                    printToOut("Shutdown completed.");
                }
            } else {
                printToOut("Shutdown completed.");
            }
        } catch (InterruptedException ie) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void printToOut(String message) {
        System.out.println("ClientHandler #" + number + ":\t" + message);
    }

    private void printToErr(String message) {
        System.err.println("ClientHandler #" + number + ":\t" + message);
    }
}
