package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TCPDirectoryServer implements DirectoryServer {

	private List<TCPClientHandler> handlerList = new LinkedList<>();

	@Override
	public void start(int port) throws DirectoryServerException {
		// TODO: implement me


		ExecutorService service = Executors.newCachedThreadPool();

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			boolean running = true;


			while (running) {

				try {
					// blockiert bis neue Verbindung aufgebaut wird von einem Client
					Socket client = serverSocket.accept();
					// zwischen accept und threads erstellen möglich keine bis sehr wenig Programmlogik, damit man gleich wieder zu accept kommt
					// und so ständig auf Verbindungen warten kann

					TCPClientHandler handler = new TCPClientHandler(client);
					handlerList.add(handler);
					service.submit(handler);
				} catch (SocketTimeoutException e) {
					// hier kann man auch andere Sachen machen statt breaken
					// zb neuen trhead starten auf Konsoleneingabe hören, ob man runterfahren soll etc
					//break;
					running = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			shutdownExecutorService(service);
		}

	}



	private void shutdownExecutorService(ExecutorService service) {
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

	}

	@Override
	public void shutdown() throws DirectoryServerException {
		// TODO: implement me
	}

	@Override
	public void run() {
		// TODO: implement me
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

	@Override
	public ClientHandler connect(Socket socket) {
		// TODO: implement me
		return null;
	}

}
