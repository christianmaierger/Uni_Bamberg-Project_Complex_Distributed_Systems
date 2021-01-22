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

	public TCPClientHandler(Socket socket, DirectoryServer parentServer){
		this.clientSocket = socket;
		this.parentServer = parentServer;
		this.threadPool = Executors.newCachedThreadPool();
		this.futureList = new ArrayList<>();
	}

	@Override
	public void run() {
		try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
			out.flush();
			try(ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

				while (true) {

					if (Thread.currentThread().isInterrupted()) {
						shutdownAndAwaitTermination();
						return;
					}

					Object object = in.readObject();
					if (object instanceof ParseDirectory) {
						process((ParseDirectory) object);
					} else if (object instanceof GetResult) {
						ResultCalculator resultCalculator = new ResultCalculator(out, this);
						threadPool.submit(resultCalculator);
					} else if (object instanceof TerminateConnection) {
						process((TerminateConnection) object);
						return;
					}
				}
			}
		} catch (IOException | ClassNotFoundException exception) {
			// TODO: handle
		}
	}

	@Override
	public void process(ParseDirectory parseDirectory) {
		DirectoryProcessor directoryProcessor = new DirectoryProcessor(parseDirectory, parentServer);
		Future<Histogram> histogramFuture = threadPool.submit(directoryProcessor);
		futureList.add(histogramFuture);
	}

	@Override
	public ReturnResult process(GetResult getResult) {
		Histogram histogram = new Histogram();
		for (Future<Histogram> future : futureList) {
			try {
				histogram = Utils.addUpAllFields(histogram, future.get());
			} catch (InterruptedException exception){
				parentServer.disconnect(this);
				shutdownAndAwaitTermination();
			}	catch (ExecutionException exception) {
				//TODO: handle
			}
		}
		return new ReturnResult(histogram);
	}

	@Override
	public void process(TerminateConnection terminateConnection) {
		shutdownAndAwaitTermination();
	}

	private void shutdownAndAwaitTermination() {
		parentServer.disconnect(this);
		threadPool.shutdown();
		try {
			if (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
				threadPool.shutdownNow();
				if (!threadPool.awaitTermination(5, TimeUnit.SECONDS))
					System.err.println("ThreadPool in TCPClientHandler did not terminate.");
			}
		} catch (InterruptedException ie) {
			threadPool.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
