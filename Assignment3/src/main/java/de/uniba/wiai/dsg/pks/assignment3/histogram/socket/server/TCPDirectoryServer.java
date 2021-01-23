package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPDirectoryServer implements DirectoryServer {
	private int port;
	private final List<ClientHandler> clientHandlers;
	private final ExecutorService threadPool;
	private final ExecutorService serverExecutor;
	private final ConcurrentHashMap<ParseDirectory, Histogram> cache;
	private volatile boolean running;
	private int serialNumberClientHandlers;


	 public TCPDirectoryServer(){
		this.clientHandlers = new LinkedList<>();
		this.threadPool = Executors.newCachedThreadPool();
		this.cache = new ConcurrentHashMap<>();
		this.serverExecutor = Executors.newSingleThreadExecutor();
		this.serialNumberClientHandlers = 1;
	 }


	@Override
	public void start(int port) throws DirectoryServerException {
			this.port = port;
			this.running = true;
			serverExecutor.submit(this);
		//TODO: wie soll denn hier die DirectoryServerException geworfen werden??
	}

	@Override
	public void disconnect(ClientHandler clientHandler) {
		clientHandlers.remove(clientHandler);
	}

	@Override
	public void shutdown() throws DirectoryServerException {
		this.running = false;
		shutdownAndAwaitTermination(threadPool);
		shutdownAndAwaitTermination(serverExecutor);
		System.out.println("TCPDirectoryServer:\tServer shutdown completed.");
		//TODO: where shall the exception come from?!
	}

	@Override
	public void run() {

		try(ServerSocket serverSocket = new ServerSocket(this.port)){
			serverSocket.setSoTimeout(1000);
			System.out.println("TCPDirectoryServer:\tServer has been started successfully.");
			while(running){
				try{
					Socket client = serverSocket.accept();
					ClientHandler clientHandler = connect(client);
					clientHandlers.add(clientHandler);
				} catch (SocketTimeoutException exception){
					continue;
				}
			}
		} catch (IOException exception){
			//todo: handle
		}
	}

	@Override
	public Optional<Histogram> getCachedResult(ParseDirectory request) {
	 	Histogram result = cache.get(request);
	 	if(Objects.isNull(result)){
	 		return Optional.empty();
		}
		return Optional.of(result);
	}

	@Override
	public void putInCache(ParseDirectory request, Histogram result) {
	 	cache.putIfAbsent(request, result);
	}

	@Override
	public ClientHandler connect(Socket socket) {
		ClientHandler clientHandler = new TCPClientHandler(socket, this, serialNumberClientHandlers);
		threadPool.submit(clientHandler);
		serialNumberClientHandlers++;
		return clientHandler;
	}

	private void shutdownAndAwaitTermination(ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				if (!executorService.awaitTermination(3, TimeUnit.SECONDS))
					System.err.println("TCPDirectoryServer:\tThreadPool did not terminate.");
			}
		} catch (InterruptedException ie) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
