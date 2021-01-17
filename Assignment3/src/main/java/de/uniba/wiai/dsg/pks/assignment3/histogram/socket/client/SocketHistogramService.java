package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
			server.connect(serverAddress);

			try(ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();

				traverseDirectory(rootDirectory, out);

				//jetzt sollte alles beim Server in Verarbeitung sein, wir können langsam nachfragen
				Thread.currentThread().sleep(1000);
				GetResult get = new GetResult();
				out.writeObject(get);


			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			try(ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {

				Object object = in.readObject();
				if(object instanceof ReturnResult) {
					resultMessage = (ReturnResult) object;
					if(resultMessage.getException()!=null) {
						// hier überlegen ob man das einfach wirft, wrapped etc die e vom server
						throw resultMessage.getException();
					}
					return resultMessage.getHistogram();
				}
			} catch (Exception e) {
				// provisorisch fange ich hier die e vom Server
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

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
	private void traverseDirectory(String currentFolder, ObjectOutputStream out) throws IOException, InterruptedException {
		Path folder = Paths.get(currentFolder);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
			for (Path path : stream) {
				if (Thread.currentThread().isInterrupted()) {
					//shutdownPrinter(outputPool);
					throw new InterruptedIOException();
				}
				if (Files.isDirectory(path)) {
					traverseDirectory(path.toString(), out);

				}
			}
		}
		//Future<Histogram> result = processFilesInFolder(currentFolder);
		//listOfFuturesRepresentingEachFolder.add(result);
		ParseDirectory dir = new ParseDirectory(currentFolder);
		out.writeObject(dir);
		out.flush();
	}

}
