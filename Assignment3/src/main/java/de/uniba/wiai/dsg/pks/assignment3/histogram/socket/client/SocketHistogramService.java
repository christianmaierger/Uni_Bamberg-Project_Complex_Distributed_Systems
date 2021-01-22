package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class SocketHistogramService implements HistogramService {
	private final String hostname;
	private final int port;

	public SocketHistogramService(String hostname, int port) {
		// REQUIRED FOR GRADING - DO NOT CHANGE SIGNATURE
		// but you can add code below
		this.hostname = hostname;
		this.port = port;
	}

	//TODO: discuss with team: does client have to give console outputs? I don't think so
	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		try (Socket server = new Socket()) {
			ReturnResult resultMessage;
			SocketAddress serverAddress = new InetSocketAddress(hostname, port);
			server.connect(serverAddress);
			System.out.println("connected to server");

			try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();
				traverseDirectory(rootDirectory, out, fileExtension);
				System.out.println("Try send get result");
				out.writeObject(new GetResult());
				out.flush();
				System.out.println("Get result sent");

				try (ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
					System.out.println("Try read returb result");
					Object object = in.readObject();
					resultMessage = (ReturnResult) object;
					System.out.println("Read returb result");
					System.out.println(resultMessage.getHistogram());
					System.out.println("Try send poison pill");
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
					System.out.println("Sent.");
				}
			}
			Histogram result = resultMessage.getHistogram();
			if(Objects.isNull(result)){
				throw new HistogramServiceException("No result Histogram present.");
			}
			return result;
		} catch (IOException | InterruptedException | ClassNotFoundException exception) {
			//TODO: exception handling
			exception.printStackTrace();
			throw new HistogramServiceException(exception.getMessage(), exception.getCause());
		}
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
					//TODO: add interruption handling
					throw new InterruptedIOException();
				}
				if (Files.isDirectory(path)) {
					traverseDirectory(path.toString(), out, fileExtension);
				}
			}
		}
		ParseDirectory parseDirectory = new ParseDirectory(currentFolder, fileExtension);
		out.writeObject(parseDirectory);
		out.flush();
		System.out.println("Sent a parseDir");
	}


}
