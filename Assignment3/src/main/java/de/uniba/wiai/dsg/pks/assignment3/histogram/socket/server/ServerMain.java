package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import java.io.IOException;

public class ServerMain {

	public static void main(String[] args) throws DirectoryServerException,
			IOException {

		TCPDirectoryServer server = new TCPDirectoryServer();
		server.start(1337);
		System.out.println("Server started. Press enter to terminate.");

		System.in.read();

		System.out.println("Server is shut down...");
		server.shutdown();
	}
}
