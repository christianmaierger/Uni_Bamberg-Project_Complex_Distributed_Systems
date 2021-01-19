package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

	public static void main(String[] args) throws DirectoryServerException,
			IOException {


		TCPDirectoryServer server = new TCPDirectoryServer();

		//fraglich ob start jetzt mit in server.run() aufgerufen werden soll, damit alles schön in einem Thread ist,
		// aber auf der anderen Seite klang Florian so, als ob was man vorfindet, man eher so lässt
		server.start(1337);
		System.out.println("Server started. Press enter to terminate.");





		System.in.read();

		server.shutdown();
		System.out.println("Server is shut down...");
	}
}
