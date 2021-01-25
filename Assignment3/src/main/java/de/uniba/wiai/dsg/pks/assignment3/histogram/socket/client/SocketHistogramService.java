package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.*;
import java.net.*;
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

	// das schmeißt an sich keine Histogram Exc
	@Override
	public Histogram calculateHistogram(String rootDirectory,
			String fileExtension) throws HistogramServiceException {

		ReturnResult resultMessage;

		try (Socket server = new Socket()) {

			SocketAddress serverAddress = new InetSocketAddress(
					hostname, port);

			if (Thread.currentThread().isInterrupted()) {
				throw new HistogramServiceException("Execution has been interrupted before connection was esatblished.");
			}

			System.out.println("CLIENT: New Client tries to connect to " + hostname + ":" + port);
			server.connect(serverAddress);

			if (Thread.currentThread().isInterrupted()) {
				throw new HistogramServiceException("Execution has been interrupted right after establishing connection.");
			}

			// mit menschlichen Reflexen ist es scheinabr unmöglich zuvor zu interrupten! selbt wenn man direkt nach klicken enter hämmert

			try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();

				// timeout klappt so nicht, hab ewig auf Antwort vom server gewartet
				server.setSoTimeout(10000);

				traverseDirectory(rootDirectory, out, fileExtension);
				if (Thread.currentThread().isInterrupted()) {
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
					throw new HistogramServiceException("Execution has been interrupted.");
				}

				System.out.println("CLIENT: sending GetResult...");
				GetResult get = new GetResult();
				if (Thread.currentThread().isInterrupted()) {
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
					throw new HistogramServiceException("Execution has been interrupted.");
				}
				out.writeObject(get);
				out.flush();
				if (Thread.currentThread().isInterrupted()) {
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
					throw new HistogramServiceException("Execution has been interrupted.");
				}


				try (ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
					System.out.println("ClIENT: reading ReturnResult...");
					Object object = in.readObject();
					if (Thread.currentThread().isInterrupted()) {
						TerminateConnection poisonPill = new TerminateConnection();
						out.writeObject(poisonPill);
						throw new HistogramServiceException("Execution has been interrupted.");
					}

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
							throw new HistogramServiceException("CLIENT: No valid result from Server");
						}
					}
					// komisch das intelli ja mir das garnicht vorschlägt, ist scheinbar eine SocketExc, die Timeout triggert nicht
				} catch (SocketTimeoutException |  java.net.SocketException e) {
					System.err.print("CLIENT: Timeout occured, connection to server is terminated now");
					throw new HistogramServiceException(e.getCause());
				} catch (IOException e) {
					System.err.print("CLIENT: IOException occured, connection to server is terminated now");
					throw new HistogramServiceException(e.getCause());
				} catch (ClassNotFoundException e) {
					System.err.print("CLIENT: Error occured while parsing ResultMessage, connection to server is terminated now");
					throw new HistogramServiceException(e.getCause());
				}
			} catch (IOException e) {
				System.err.print("CLIENT: IOException occured, connection to server is terminated now");
				throw new HistogramServiceException(e.getCause());
			} catch (InterruptedException e) {
				// bissel sleepen ist keine schlechte idee, da es interruptable ist und sicherstellt, dass
				//wir das Ergebnis erst an einem Zeitpunkt erfragen, wo es shcon generiert wurde
				System.err.println("CLIENT: was interupted during reading file System");
				throw new HistogramServiceException("Execution has been interrupted.");
			}
		// denke ConnectEx fällt unter IOEx
		} catch (ConnectException e) {
			System.err.println("CLIENT: was not able to establish Connection to Server");
			throw new HistogramServiceException("Execution has been interrupted.");
		} catch (IOException e) {
			System.err.println("CLIENT: there was a problem starting and connecting this c lient");
			throw new HistogramServiceException("Execution has been interrupted.");
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
					throw new InterruptedException();
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
