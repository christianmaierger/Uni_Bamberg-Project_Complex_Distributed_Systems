package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@ThreadSafe
public class TCPClientHandler implements ClientHandler {
	@GuardedBy(value ="itself")
	private final Socket client;
	@GuardedBy(value ="itself")
	private final TCPDirectoryServer server;
	@GuardedBy(value ="semaphore") // kann man so auch nicht direkt sagen, nur beim schreiben, nicht beim lesen und removen
	private final LinkedList<Future<Histogram>> futureList;
	@GuardedBy(value ="semaphore") // auch nur beim schreiben, muss das sein beim lesen?
	private Histogram subResultHistogram;
	@GuardedBy(value ="itself")
	private Semaphore semaphore = new Semaphore(1, true);


	public TCPClientHandler(Socket client, TCPDirectoryServer server) {
		this.client = client;
		this.server = server;
		this.futureList = new LinkedList<>();
		subResultHistogram=new Histogram();
	}

	public TCPDirectoryServer getServer() {
		return server;
	}

	public Socket getClient() {
		return client;
	}

	public Histogram getSubResultHistogram() {
		return subResultHistogram;
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}

	public LinkedList<Future<Histogram>> getFutureList() {
		return futureList;
	}

	@Override
	public void setSubResultHistogram(Histogram addUpAllFields) {

		this.subResultHistogram=addUpAllFields;
	}

	@Override
	public void run() {
		// TODO implement me
		boolean running = true;
		try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {

			while (running) {
				Object object = in.readObject();
				// was eigentlich wenn ein objekt keine der drei message Klassen wäre?
				if (object instanceof ParseDirectory) {
					ParseDirectory directoryMessage = (ParseDirectory) object;
					process(directoryMessage);
				} else if (object instanceof GetResult) {
					GetResult getResultMessage = (GetResult) object;
					ReturnMessageRunnable returnMessageRunnable = new ReturnMessageRunnable(this, getResultMessage);
					server.getService().submit(returnMessageRunnable);
				} else if (object instanceof TerminateConnection) {
					TerminateConnection terminateMessage = (TerminateConnection) object;
					process(terminateMessage);
					running = false;
				}
			}

			// Frage was bei Fehler hier wirklich passieren soll, nehme an kein cmpletter shutdown

			// leider sind bei der ioex cause und message immer null
			// soll ich hier auch disconnecten?
			// Frage auch ob je Ex echt ganz Schluss ein soll, denke schon weil Ergebnis ist sonst Quatsch
		} catch (IOException e) {
			running = false;
			System.err.println("CLIENTHANDLER: Connection error " + e.getCause());
			server.disconnect(this);
		} catch (ClassCastException e) {
			running = false;
			System.err.println("CLIENTHANDLER: Error parsing message object " + e.getMessage());
			server.disconnect(this);
		} catch (ClassNotFoundException e) {
			running = false;
			System.err.println("CLIENTHANDLER: Incoming message type could not be handled " + e.getMessage());
			server.disconnect(this);
		}
	}

	@Override
	public void process(ParseDirectory parseDirectory) {
		// TODO: implement me

		// echt thread auslagern nötig, sind ja ncihtmal 20 Zeilen vor dem nächsten Durchlauf, sollte schon passen
		if (server.getCachedResult(parseDirectory).isPresent()) {
			setSubResultHistogram(Utils.addUpAllFields(server.getCachedResult(parseDirectory).get(), getSubResultHistogram()));

		} else {

			TraverseFolderCallable folderTask = new TraverseFolderCallable(parseDirectory, this);
			Future<Histogram> folderFuture = server.getService().submit(folderTask);

			futureList.add(folderFuture);

		}


	}

	@Override
	public ReturnResult process(GetResult getResult) {
		// TODO: implement me
	// Frage muss/soll/kann überhaupt je null an client gehen, oder einfach nichts und der timed out?!?
		ReturnResult returnResult = null;

		returnResult= new ReturnResult(this.getSubResultHistogram());

		return returnResult;
	}

	@Override
	public void process(TerminateConnection terminateConnection) {
		// TODO: implement me
		try {
			client.close();
		} catch (IOException e) {
			System.err.println("CLIENTHANDLER: Socket could not be closed without exception");
		} finally {
			server.disconnect(this);
		}

	}

}
