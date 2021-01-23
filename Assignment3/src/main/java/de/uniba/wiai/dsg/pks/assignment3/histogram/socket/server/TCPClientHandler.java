package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.helpers.DirectoryProcessor;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.helpers.ResultCalculator;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

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
	private Histogram histogram;
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
					System.out.println("ClientHandler #" + number + ":\tReceived a message.");
					if (object instanceof ParseDirectory) {
						process((ParseDirectory) object);
					} else if (object instanceof GetResult) {
						ResultCalculator resultCalculator = new ResultCalculator(out, this, number);
						threadPool.submit(resultCalculator);
					} else if (object instanceof TerminateConnection) {
						System.out.println("ClientHandler #" + number + ":\tClient terminated connection.");
						process((TerminateConnection) object);
					}
				}
			}
		} catch (IOException | ClassNotFoundException exception) {
			System.err.println("ClientHandler #" + number + ":\tException: " + exception.getMessage() + ".");
		} finally {
			System.out.println("ClientHandler #" + number + ":\tInitiate shutdown.");
			shutdownAndAwaitTermination();
			System.out.println("ClientHandler #" + number + ":\tShutdown completed.");
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
		Histogram histogram = new Histogram();
		for (Future<Histogram> future : futureList) {
			try {
				future.get();
			} catch (InterruptedException exception){
				shutdownAndAwaitTermination();
			} catch (ExecutionException exception) {
				System.err.println("ClientHandler  #" + number + ":\tException occurred - " + exception.getMessage());
				return null;
			}
		}
		return new ReturnResult(histogram);
	}

	@Override
	public void process(TerminateConnection terminateConnection) {
		this.running = false;
	}

	public void addToHistogram(Histogram inputHistogram) throws InterruptedException {
		semaphore.acquire();
		this.histogram = Utils.addUpAllFields(this.histogram, inputHistogram);
		semaphore.release();
	}

	private void shutdownAndAwaitTermination() {
		this.running = false;
		parentServer.disconnect(this);
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
				threadPool.shutdownNow();
				if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
					System.err.println("ClientHandler  #" + number + ":\tThreadPool did not terminate.");
			}
		} catch (InterruptedException ie) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}


}
