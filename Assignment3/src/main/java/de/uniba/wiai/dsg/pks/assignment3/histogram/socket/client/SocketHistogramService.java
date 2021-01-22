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

			try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();
				traverseDirectory(rootDirectory, out, fileExtension);
				out.writeObject(new GetResult());
				out.flush();

				try (ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
					Object object = in.readObject();
					resultMessage = (ReturnResult) object;
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
				}
			}

			if(Objects.isNull(resultMessage)){
				throw new HistogramServiceException("No result Histogram present.");
			} else{
				return resultMessage.getHistogram();
			}

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
					TerminateConnection poisonPill = new TerminateConnection();
					out.writeObject(poisonPill);
					throw new InterruptedException("Execution has been interrupted.");
				}
				if (Files.isDirectory(path)) {
					traverseDirectory(path.toString(), out, fileExtension);
				}
			}
		}
		ParseDirectory parseDirectory = new ParseDirectory(currentFolder, fileExtension);
		out.writeObject(parseDirectory);
		out.flush();
	}


}
