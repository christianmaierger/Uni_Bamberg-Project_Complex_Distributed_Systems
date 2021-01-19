package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPDirectoryServer implements DirectoryServer {

	private List<ClientHandler> handlerList = new LinkedList<>();
	private ConcurrentHashMap<ParseDirectory, Histogram> directoryHistogramHashMap =  new ConcurrentHashMap();
	private ServerSocket serverSocket;
	private ExecutorService service;
	boolean running = true;


	// eventuell mach ich den auch wieder weg und wir nehmen den default konst
	public TCPDirectoryServer() {
		serverSocket=null;
	}


	public List<ClientHandler> getHandlerList() {
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

		// wie von Franzi vorgeschlagen mein Vorschlag das ganze starten mit pool und server hier rienzubringen,
		//finds eig. nice
		try  {
			serverSocket = new ServerSocket(port);
			System.out.println("Server started successfully...");

			service = Executors.newCachedThreadPool();


			// dass finde ich halt sehr geöhnungsbedürftig, this zu submitten
			// aber mei warum nicht
			service.submit(this);
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
		// handler muss ja selbstständig terminiert haben
		handlerList.remove(clientHandler);

	}


	// wo soll denn in der Methode die DirSerEx herkommen?
	// ist des Rätesls Lösung echt die Es einfach darin abzuwrappen und zu werfen? Denke fast
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

			try {
				while (running) {
					System.out.println("Server is waiting for new clients to connect...");
					Socket client = serverSocket.accept();

					System.out.println("Server accepted client connection...");

					// connect hier verwenden, dass erzeugt ja Handler?!
					//TCPClientHandler handler = new TCPClientHandler(client);
					ClientHandler handler = connect(client);
					handlerList.add(handler);
					service.submit(handler);
					System.out.println("Server created and started new ClientHandler...");
				}
			} catch (IOException e) {
				e.printStackTrace();


				// Fraglich ob ich den Server komplett runterfahren will, wenn eine Exception kommt
				// oder ob das eher ne Option ist wenn in Main einer Enter drückt
				// und ich hier die Exc so fange, dass es weiter gehen kann?
			} finally {

				System.out.println("Server is shutingdown it´s ExecutorService...");
				try {
					// weiß immernoch nicht wo diese Exc aus der Methode herkommen soll?
					// siehe Kommi bei Methode denke wird upgewrapt
					shutdown();
				} catch (DirectoryServerException e) {
					e.printStackTrace();
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
