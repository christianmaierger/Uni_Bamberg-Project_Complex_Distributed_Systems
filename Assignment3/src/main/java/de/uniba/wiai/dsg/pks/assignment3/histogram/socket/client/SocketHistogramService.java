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

		try (Socket server = new Socket()) {

			SocketAddress serverAddress = new InetSocketAddress(
					hostname, port);

			System.out.println("CLIENT: New Client tries to connect to " + hostname + ":" + port);
			server.connect(serverAddress);

			try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();

				traverseDirectory(rootDirectory, out, fileExtension);


				System.out.println("CLIENT: sending GetResult...");
				GetResult get = new GetResult();
				out.writeObject(get);
				out.flush();


				try (ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
					System.out.println("ClIENT: reading ReturnResult...");
					Object object = in.readObject();


					System.out.println("CLIENT: got Result from Server, Connection is now terminated...");
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);

					if (object instanceof ReturnResult) {
						resultMessage = (ReturnResult) object;

						if (resultMessage.getHistogram() != null) {
							System.out.println("CLIENT: ReturnResult contained an Histogram, which is returned...");
							return resultMessage.getHistogram();
						} else {
							System.err.println("CLIENT: ReturnResult contained not an Histogram, Error!");
							// vielleicht custom Ex thrown, oder einfach null returnen?
							// histogram exeption?
							throw new UnsupportedOperationException("Implement here");
						}
					}


				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();

				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// soweit ich es sehe, kann nur traverse dir interrupted werden,
				//wird dort aber angeblich nie geworfen?
				// dennoch unterbricht der interrupt button die Ausführung? Ja weil ich hier sleep drin hatte
				// bissel sleepen ist keine schlechte idee, da es interruptable ist und sicherstellt, dass
				//wir das Ergebnis erst an einem Zeitpunkt erfragen, wo es shcon generiert wurde
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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
					throw new InterruptedIOException();
				}
				if (Files.isDirectory(path)) {
					traverseDirectory(path.toString(), out, fileExtension);

				}
			}
		}
		ParseDirectory dir = new ParseDirectory(currentFolder, fileExtension);
		out.writeObject(dir);
		out.flush();
	}

}
