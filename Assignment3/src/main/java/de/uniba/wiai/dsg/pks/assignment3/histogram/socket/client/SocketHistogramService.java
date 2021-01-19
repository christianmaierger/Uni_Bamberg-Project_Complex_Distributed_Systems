package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SocketHistogramService implements HistogramService {
	private String hostname;
	private int port;

	public SocketHistogramService(String hostname, int port) {
		// REQUIRED FOR GRADING - DO NOT CHANGE SIGNATURE
		// but you can add code below
		this.hostname=hostname;
		this.port = port;
	}

	@Override
	public Histogram calculateHistogram(String rootDirectory,
			String fileExtension) {

			ReturnResult resultMessage;

		try(Socket server = new Socket()) {

			SocketAddress serverAddress = new InetSocketAddress(
					hostname, port);

			System.out.println("New Client tries to connect to " + hostname + ":" + port);
			server.connect(serverAddress);

			try(ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();

				traverseDirectory(rootDirectory, out, fileExtension);

				//jetzt sollte alles beim Server in Verarbeitung sein, wir können langsam nachfragen, zur Sicherheit
				//erstmal ein Schläfchen
				Thread.currentThread().sleep(1000);
				System.out.println("Client is sending GetResult...");
				GetResult get = new GetResult();
				out.writeObject(get);
				out.flush();


				try(ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
					System.out.println("Client is reading ReturnResult...");
					Object object = in.readObject();


					System.out.println("As the Client got Result from Server, Connection is now terminated...");
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
					if(object instanceof ReturnResult) {
						resultMessage = (ReturnResult) object;
						if(resultMessage.getException()!=null) {
							// hier überlegen ob man das einfach wirft, wrapped etc die e vom server
							System.out.println("ReturnResult contained an Exeption, Server was terminated...");
							throw resultMessage.getException();
						}
						System.out.println("ReturnResult contained an Histogram, which is returned...");
						return resultMessage.getHistogram();
					}
				} catch (Exception e) {
					// provisorisch fange ich hier die e vom Server
					e.printStackTrace();
				}


			} catch (InterruptedException e) {
				e.printStackTrace();
			}




		} catch (IOException e) {
			e.printStackTrace();
		}
	// histogram exeption?
		throw new UnsupportedOperationException("Implement here");
	}

	@Override
	public String toString() {
		return "SocketHistogramService";
	}


	/**
	 * Unneeded legacy method from Assignment 1.
	 */
	@Override
	public void setIoExceptionThrown(boolean value){
		throw new UnsupportedOperationException();
	}

	/**
	 * Scans through the root folder and looks for directories. After the root folder has been fully scanned,
	 * the files in it are processed.
	 *
	 * @param currentFolder folder to scan through
	 * @throws IOException          if I/O error occurred during processing of the folder
	 * @throws InterruptedException if Thread is interrupted
	 */
	private void traverseDirectory(String currentFolder, ObjectOutputStream out, String fileExtension) throws IOException, InterruptedException {
		Path folder = Paths.get(currentFolder);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
			for (Path path : stream) {
				if (Thread.currentThread().isInterrupted()) {
					//shutdownPrinter(outputPool);
					throw new InterruptedIOException();
				}
				if (Files.isDirectory(path)) {
					traverseDirectory(path.toString(), out, fileExtension);

				}
			}
		}
		//Future<Histogram> result = processFilesInFolder(currentFolder);
		//listOfFuturesRepresentingEachFolder.add(result);
		ParseDirectory dir = new ParseDirectory(currentFolder, fileExtension);
		out.writeObject(dir);
		out.flush();
	}

}
