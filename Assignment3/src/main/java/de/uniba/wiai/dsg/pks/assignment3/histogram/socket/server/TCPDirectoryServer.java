package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPDirectoryServer implements DirectoryServer {
	private int port;
	private final List<ClientHandler> clientHandlers;
	private final ExecutorService threadPool;
	private final ConcurrentHashMap<ParseDirectory, Histogram> cache;
	private boolean running;


	 public TCPDirectoryServer(){
		this.clientHandlers = new LinkedList<>();
		this.threadPool = Executors.newCachedThreadPool();
		this.cache = new ConcurrentHashMap<>();
	 }


	@Override
	public void start(int port) throws DirectoryServerException {
			this.port = port;
			ExecutorService serverExecutorService = Executors.newSingleThreadExecutor();
			serverExecutorService.submit(this);
			this.running = true;
		//TODO: wie soll denn hier die DirectoryServerException geworfen werden??
	}

	@Override
	public void disconnect(ClientHandler clientHandler) {
		clientHandlers.remove(clientHandler);
	}

	@Override
	public void shutdown() throws DirectoryServerException {
		this.running = false;
		// TODO: shut down all running clientHandlers
	}

	@Override
	public void run() {
		try(ServerSocket serverSocket = new ServerSocket(this.port)){
			while(running){
				Socket client = serverSocket.accept();
				ClientHandler clientHandler = connect(client);
				clientHandlers.add(clientHandler);
			}
		} catch (IOException exception){
			throw new RuntimeException(exception.getMessage(), exception.getCause());
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
		ClientHandler clientHandler = new TCPClientHandler(socket, this);
		threadPool.submit(clientHandler);
		return clientHandler;
	}

}
