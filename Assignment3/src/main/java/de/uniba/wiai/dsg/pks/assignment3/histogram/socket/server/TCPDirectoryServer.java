package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;

import java.net.Socket;
import java.util.Optional;

public class TCPDirectoryServer implements DirectoryServer {

	@Override
	public void start(int port) throws DirectoryServerException {
		// TODO: implement me
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
