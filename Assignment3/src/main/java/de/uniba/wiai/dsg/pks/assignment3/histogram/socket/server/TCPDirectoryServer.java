package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

// eig Threadsafe durch impl, aber den Eigenschaften nach NotThreadsafe?
@ThreadSafe
public class TCPDirectoryServer implements DirectoryServer {

	private final List<ClientHandler> handlerList = new LinkedList<>();
	@GuardedBy(value ="itself")
	private final ConcurrentHashMap<ParseDirectory, Histogram> cache;

	private ServerSocket serverSocket;

	private ExecutorService service;

	private boolean running;




	public TCPDirectoryServer() {
		cache =  new ConcurrentHashMap();
		serverSocket=null;
		running = true;
	}


	public List<ClientHandler> getHandlerList() {
		return handlerList;
	}

	public ConcurrentHashMap<ParseDirectory, Histogram> getCache() {
		return cache;
	}

	public ExecutorService getService() {
		return service;
	}


	@Override
	public void start(int port) throws DirectoryServerException {
		// TODO: implement me
		try  {
			serverSocket = new ServerSocket(port);
			System.out.println("SERVER: started successfully");

			service = Executors.newCachedThreadPool();

			service.submit(this);
		} catch (IOException e) {
			System.err.println("SERVER: could not be started successfully: " + e.getMessage());
			throw new DirectoryServerException(e.getCause());
		}
	}


	/**
	 *
	 * Shutsdown a Threadpool that is given as param orderly according to the way advised in the ORACLE API
	 *
	 * @param service
	 */
	private void shutdownExecutorService(ExecutorService service) {
		System.out.println("SERVER: attempting to shutdown ExecutorService");
			service.shutdown();
			try {
				if (!service.awaitTermination(1, TimeUnit.SECONDS)) {
					service.shutdownNow();
					if(!service.awaitTermination(10, TimeUnit.SECONDS)) {
						System.err.println("SERVER: threadpool did not terminate correctly");
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
		handlerList.remove(clientHandler);
		System.out.println("SERVER: successfully disconnected a CLIENTHANDLER");
	}


	// wo soll denn in der Methode die DirSerEx herkommen?
	// ist des Rätesls Lösung echt die Es einfach darin abzuwrappen und zu werfen? Denke fast
	@Override
	public void shutdown() throws DirectoryServerException {
		// TODO: implement me
		this.running = false;

		try {
			shutdownExecutorService(service);
			this.serverSocket.close();
			System.out.println("SERVER: shutdown as intended");
		} catch (IOException e) {
			System.err.println("SERVER: Shutdown encountered a problem: " + e.getMessage());
			throw new DirectoryServerException(e.getCause());
		}
		}



	@Override
	public void run() {
		// TODO: implement me


		while (running) {
			try {
				System.out.println("SERVER: waiting for new clients to connect...");
				Socket client = serverSocket.accept();
				System.out.println("SERVER: accepted client connection...");

				ClientHandler handler = connect(client);

				handlerList.add(handler);
				System.out.println("SERVER: created and started new ClientHandler...");

			} catch (IOException e) {
				System.out.println("SERVER: a connection request from a client could not be handled correctly");
			}
		}

	}

	@Override
	public Optional<Histogram> getCachedResult(ParseDirectory request) {
		// TODO: implement me
		Histogram result = cache.get(request);
		if(Objects.isNull(result)){
			return Optional.empty();
		}
		return Optional.of(result);
	}

	@Override
	public void putInCache(ParseDirectory request, Histogram result) {
		// TODO: implement me
		cache.putIfAbsent(request, result);
	//	System.out.println("SERVER: successfully put new histogram in cache...");
	}


	@Override
	public ClientHandler connect(Socket socket) {
		// TODO: implement me
		ClientHandler handler = new TCPClientHandler(socket, this);
		service.submit(handler);
		return handler;
	}


}
