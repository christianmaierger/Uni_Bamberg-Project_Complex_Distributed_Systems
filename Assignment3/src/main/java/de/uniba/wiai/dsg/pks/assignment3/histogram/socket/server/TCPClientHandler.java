package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TCPClientHandler implements ClientHandler {
	private final Socket client;
	private final TCPDirectoryServer server;
	private final LinkedList<Future<Histogram>> futureList;


	//urspr. Konst behelfsmäsig
	public TCPClientHandler(Socket client) {
		this.client=client;
		this.server=null;
		this.futureList = new LinkedList<>();
	}

// zweiter Konst mit server könnte bei Kommunikation mit Server helfen
	public TCPClientHandler(Socket client, TCPDirectoryServer server) {
	this.client = client;
	this.server=server;
	this.futureList = new LinkedList<>();
	}

	@Override
	public void run() {
		// TODO implement me
		try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {


			// evtl in anderen Thread auslagern der messages annimt, aber der muss ja trotzdem die methoden hier aufrufen?!
			// messages vielleicht in eine queue, aber ist das nicht irgendwie sequentielll, gut irgendwie ist es immer nacheinander
			boolean running = true;
			while (running) {

				Object object = in.readObject();


				// nicht ohne test casten
				if (object instanceof ParseDirectory) {
					ParseDirectory directoryMessage = (ParseDirectory) object;
					process(directoryMessage);
				} else if (object instanceof GetResult) {
					GetResult getResultMessage = (GetResult) object;
					process(getResultMessage);
					running=false;
					process(getResultMessage);
				} else if (object instanceof TerminateConnection) {
					TerminateConnection terminateMessage = (TerminateConnection) object;
					process(terminateMessage);
					running=false;
				}

			}


			// evtl es einzeln behandenl, classCast dürfte durch instanceOf test nicht mehr passieren
		} catch (IOException | ClassCastException e) {
			System.err.println("Connection error " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Incoming message type could not be handled " + e.getMessage());
		}
	}

	@Override
	public void process(ParseDirectory parseDirectory) {
		// TODO: implement me
	TraverseFolderCallable folderTask = new TraverseFolderCallable(parseDirectory.getPath(), parseDirectory.getFileExtension());

     Future<Histogram> folderFuture =server.getService().submit(folderTask);

     // wann aber die Ergebnisse getten und wo?
     futureList.add(folderFuture);


	}

	@Override
	public ReturnResult process(GetResult getResult) {
		// TODO: implement me

		return null;
	}

	@Override
	public void process(TerminateConnection terminateConnection) {
		// TODO: implement me


	}

}
