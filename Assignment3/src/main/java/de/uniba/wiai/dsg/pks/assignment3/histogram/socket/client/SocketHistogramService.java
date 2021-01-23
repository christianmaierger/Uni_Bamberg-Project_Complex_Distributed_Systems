package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramService;
import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
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

	@Override
	public Histogram calculateHistogram(String rootDirectory, String fileExtension) throws HistogramServiceException {
		ReturnResult resultMessage;
		try (Socket server = new Socket()) {
			SocketAddress serverAddress = new InetSocketAddress(hostname, port);
			server.connect(serverAddress);
			try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream())) {
				out.flush();
				sendDirectoryParseMessages(out, rootDirectory, fileExtension);
				requestResult(out);
				try (ObjectInputStream in = new ObjectInputStream(server.getInputStream())) {
					resultMessage = receiveResult(in, out, server);
					terminateConnection(out);
				}
			}
			verifyResultIsNotNull(resultMessage);
			return resultMessage.getHistogram();
		} catch (IOException exception) {
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
	private void sendDirectoryParseMessages(ObjectOutputStream out, String currentFolder, String fileExtension) throws HistogramServiceException {
		Path folder = Paths.get(currentFolder);
		try{
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
				for (Path path : stream) {
					checkForInterrupt(out);
					if (Files.isDirectory(path)) {
						sendDirectoryParseMessages(out, path.toString() , fileExtension);
					}
				}
			}
			ParseDirectory parseDirectory = new ParseDirectory(currentFolder, fileExtension);
			out.writeObject(parseDirectory);
			out.flush();
		} catch (IOException exception){
			terminateConnection(out);
			throw new HistogramServiceException(exception.getMessage(), exception.getCause());
		}
	}

	private void requestResult(ObjectOutputStream out) throws HistogramServiceException {
		try{
			out.writeObject(new GetResult());
			out.flush();
		} catch	(IOException exception){
			terminateConnection(out);
			throw new HistogramServiceException(exception.getMessage(), exception.getCause());
		}
	}

	private ReturnResult receiveResult(ObjectInputStream in, ObjectOutputStream out, Socket server) throws HistogramServiceException {
		ReturnResult result;
		while(true){
			try{
				server.setSoTimeout(200);
				Object object = in.readObject();
				result = (ReturnResult) object;
				return result;
			} catch (SocketException exception){
				checkForInterrupt(out);
			} catch (IOException | ClassNotFoundException exception){
				terminateConnection(out);
				throw new HistogramServiceException(exception.getMessage(), exception.getCause());
			}
		}
	}

	private void terminateConnection(ObjectOutputStream out) throws HistogramServiceException {
		try {
			TerminateConnection poisonPill = new TerminateConnection();
			out.writeObject(poisonPill);
		} catch (IOException exception){
			throw new HistogramServiceException(exception.getMessage(), exception.getCause());
		}
	}

	private void checkForInterrupt(ObjectOutputStream out) throws HistogramServiceException {
		if (Thread.currentThread().isInterrupted()) {
			terminateConnection(out);
			throw new HistogramServiceException("Execution has been interrupted.");
		}
	}

	private void verifyResultIsNotNull(ReturnResult result) throws HistogramServiceException {
		if(Objects.isNull(result)){
			throw new HistogramServiceException("No result histogram present.");
		}
	}
}
