package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPDirectoryServer implements DirectoryServer {

	private List<TCPClientHandler> handlerList = new LinkedList<>();
	private ConcurrentHashMap<ParseDirectory, Histogram> directoryHistogramHashMap =  new ConcurrentHashMap();
	private ServerSocket serverSocket=null;
	private ExecutorService service;


	public List<TCPClientHandler> getHandlerList() {
		return handlerList;
	}

	public ConcurrentHashMap<ParseDirectory, Histogram> getDirectoryHistogramHashMap() {
		return directoryHistogramHashMap;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public ExecutorService getService() {
		return service;
	}

	@Override
	public void start(int port) throws DirectoryServerException {
		// TODO: implement me
		try  {
			serverSocket = new ServerSocket(port);
			System.out.println("Server started successfully...");
		} catch (IOException e) {
			System.err.println("Server could not be started successfully: " + e.getMessage());
		}
	}


	/**
	 *
	 */
	private void shutdownExecutorService() {
			service.shutdown();
			try {
				if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
					service.shutdownNow();
					if(!service.awaitTermination(60, TimeUnit.SECONDS)) {
						System.err.println("Server did not terminate");
					}
				}
			} catch (InterruptedException e) {
				service.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}



	@Override
	public void disconnect(ClientHandler clientHandler) {
		// TODO implement me
		// was muss hier noch alles gemacht werden?
		handlerList.remove(clientHandler);

	}


	// wo soll denn in der Methode die DirSerEx herkommen?
	@Override
	public void shutdown() throws DirectoryServerException {
		// TODO: implement me
		shutdownExecutorService();
		try {
			this.serverSocket.close();
			System.out.println("Server shutdown as intended");
		} catch (IOException e) {
			System.err.println("Server Shutdown encountered a problem: " + e.getMessage());

		}

	}

	@Override
	public void run() {
		// TODO: implement me
		boolean running = true;
		service = Executors.newCachedThreadPool();


		try {
			serverSocket.setSoTimeout(10000);
		} catch (SocketException e) {
			// herunterfahren wenn keine neuen Connections kommen?
			e.printStackTrace();
		}

		while (running) {

			try {
				// blockiert bis neue Verbindung aufgebaut wird von einem Client
				System.out.println("Server is waiting for new clients to connect...");
				Socket client = serverSocket.accept();
				// zwischen accept und threads erstellen möglich keine bis sehr wenig Programmlogik, damit man gleich wieder zu accept kommt
				// und so ständig auf Verbindungen warten kann
				System.out.println("Server accepted client connection...");

				// connect hier verwenden, dass erzeugt ja Handler?!
				//TCPClientHandler handler = new TCPClientHandler(client);
				TCPClientHandler handler = (TCPClientHandler) connect(client);
				handlerList.add(handler);
				System.out.println("Server created and started new ClientHandler...");
				service.submit(handler);
				// frage ob ich timeout wirjlich will, vorallem wenn handler noch laufen?!
			} catch (SocketTimeoutException e) {
				// hier kann man auch andere Sachen machen statt breaken
				// zb neuen trhead starten auf Konsoleneingabe hören, ob man runterfahren soll etc
				//break;
				running = false;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.out.println("Server is shutingdown it´s ExecutorService...");
				try {
					shutdown();
				} catch (DirectoryServerException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public Optional<Histogram> getCachedResult(ParseDirectory request) {
		// TODO: implement me
		return null;
	}

	@Override
	public void putInCache(ParseDirectory request, Histogram result) {
		// TODO: implement me
	}

	// was bringt das denn gegenüber direkter Erzeugung, was fehlt?
	@Override
	public ClientHandler connect(Socket socket) {
		// TODO: implement me
		return new TCPClientHandler(socket, this);
	}

}
