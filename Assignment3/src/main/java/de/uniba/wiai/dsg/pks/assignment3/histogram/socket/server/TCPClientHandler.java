package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.messageprocessing.DirectoryProcessor;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.messageprocessing.ResultCalculator;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TCPClientHandler implements ClientHandler {
	private final Socket clientSocket;
	private final DirectoryServer parentServer;
	private final ExecutorService threadPool;
	private final List<Future<Histogram>> futureList;
	private volatile boolean running;
	private final int number;
	private final Histogram histogram;
	private final Semaphore semaphore;

	public TCPClientHandler(Socket socket, DirectoryServer parentServer, int number){
		this.clientSocket = socket;
		this.parentServer = parentServer;
		this.threadPool = Executors.newCachedThreadPool();
		this.futureList = new ArrayList<>();
		this.running = true;
		this.number = number;
		this.histogram = new Histogram();
		this.semaphore = new Semaphore(1);
	}

	@Override
	public void run() {
		System.out.println("ClientHandler #" + number + ":\tConnection established to a new client.");
		try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
			out.flush();
			try(ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
				while (running) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					Object object = in.readObject();
					System.out.print("ClientHandler #" + number + ":\tReceived a message: ");
					if (object instanceof ParseDirectory) {
						System.out.println("parse request.");
						process((ParseDirectory) object);
					} else if (object instanceof GetResult) {
						System.out.println("result request.");
						ResultCalculator resultCalculator = new ResultCalculator(out, this, number);
						threadPool.submit(resultCalculator);
					} else if (object instanceof TerminateConnection) {
						System.out.println("Client terminated connection.");
						process((TerminateConnection) object);
					} else {
						System.out.println("Unknown message type. Message was ignored.");
					}
				}
			}
		} catch (IOException ioException) {
			//TODO: Wann kann die hier überhaupt geworfen werden? Dann ist ja eigentlich der Stream im Arsch, oder? --> shutdown
			System.err.println("ClientHandler #" + number + ":\tIOException: " + ioException.getMessage() + ".");
		} catch (ClassNotFoundException classNotFoundException){
			//TODO: Wann kann die hier überhaupt geworfen werden? Selbst mit der unbekannten Klasse Test passiert das nicht...
			System.err.println("ClientHandler #" + number + ":\tClassNotFoundException: " + classNotFoundException.getMessage() + ".");
		}
		finally {
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
			} catch (InterruptedException exception){
				shutdownAndAwaitTermination();
			} catch (ExecutionException exception) {
				if(exception.getCause() instanceof InterruptedException){
					shutdownAndAwaitTermination();
				} else {
					System.err.println("ClientHandler  #" + number + ":\tException in DirectoryProcessor: " + exception.getMessage());
					System.err.println("ClientHandler  #" + number + ":\tResult is corrupt. Null is sent to client instead of ReturnResult.");
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
		this.running = false;
	}

	public void addToHistogram(Histogram inputHistogram) throws InterruptedException {
		try{
			semaphore.acquire();
			DirectoryUtils.addUpAllFields(this.histogram, inputHistogram);
		} finally {
			semaphore.release();
		}
	}

	private void shutdownAndAwaitTermination() {
		System.out.println("ClientHandler #" + number + ":\tInitiate shutdown.");
		this.running = false;
		parentServer.disconnect(this);
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
				threadPool.shutdownNow();
				if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
					System.err.println("ClientHandler  #" + number + ":\tThreadPool did not terminate.");
				} else{
					System.out.println("ClientHandler #" + number + ":\tShutdown completed.");
				}
			} else{
				System.out.println("ClientHandler #" + number + ":\tShutdown completed.");
			}
		} catch (InterruptedException ie) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
