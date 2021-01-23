package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class TCPClientHandler implements ClientHandler {
	private final Socket client;
	private final TCPDirectoryServer server;
	private final LinkedList<Future<Histogram>> futureList;
	private Histogram subResultHistogram;
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


				if (object instanceof ParseDirectory) {
					ParseDirectory directoryMessage = (ParseDirectory) object;
					// der counter soll mir die dirs zählen, damit ich weiß wann ich das result schicken kann
					//das ist ja dann fertig wenn ich genau so viele futures wie dirs hab, da keine concurrency
					// sollte ein einfaches int langen statt long adder Spielereien oder?
					process(directoryMessage);
				} else if (object instanceof GetResult) {
					GetResult getResultMessage = (GetResult) object;
					ReturnMessageRunnable returnMessageRunnable = new ReturnMessageRunnable(futureList, this, getResultMessage, false);
					//resultHistogramMessageFuture =
					// versuche es mal ohne future, da dieses callable ähnlich wie ein runnable eigentlich nur den zweck hat process ansync aufzurufen und
					// die berechnete Message in den Outputstrem zu schreiben
					server.getService().submit(returnMessageRunnable);
				} else if (object instanceof TerminateConnection) {
					TerminateConnection terminateMessage = (TerminateConnection) object;
					process(terminateMessage);
					running = false;
				}

			}


			// evtl es einzeln behandenl, classCast dürfte durch instanceOf test nicht mehr passieren
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
		} catch (ClassNotFoundException e) {
			running = false;
			System.err.println("CLIENTHANDLER: Incoming message type could not be handled " + e.getMessage());
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
		// wieder überleben was bei null machen
		// denke einfach an client und der weiß, oh das war ne exception
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
