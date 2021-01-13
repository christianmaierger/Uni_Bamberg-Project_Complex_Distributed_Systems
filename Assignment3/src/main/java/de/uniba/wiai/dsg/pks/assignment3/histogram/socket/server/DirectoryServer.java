package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import java.net.Socket;

public interface DirectoryServer extends Runnable, Cache {

	// server lifecycle

	void start(int port) throws DirectoryServerException;

	void shutdown() throws DirectoryServerException;

	// connection filecycle

	ClientHandler connect(Socket socket);

	void disconnect(ClientHandler clientHandler);

}
