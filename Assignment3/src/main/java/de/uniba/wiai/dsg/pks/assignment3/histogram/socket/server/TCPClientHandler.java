package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TCPClientHandler implements ClientHandler {
	private final Socket client;
	private final TCPDirectoryServer server;
	private final LinkedList<Future<Histogram>> futureList;
	private int directoryMessageCounter;



	// zweiter Konst mit server könnte bei Kommunikation mit Server helfen
	public TCPClientHandler(Socket client, TCPDirectoryServer server) {
	this.client = client;
	this.server=server;
	this.futureList = new LinkedList<>();
	this.directoryMessageCounter = 0;
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
					// der counter soll mir die dirs zählen, damit ich weiß wann ich das result schicken kann
					//das ist ja dann fertig wenn ich genau so viele futures wie dirs hab, da keine concurrency
					// sollte ein einfaches int langen statt long adder Spielereien oder?
					directoryMessageCounter++;
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

     futureList.add(folderFuture);


	}

	@Override
	public ReturnResult process(GetResult getResult) {
		// TODO: implement me
		// wieder überleben was bei null machen
		// denke einfach an client und der weiß, oh das war ne exception
		ReturnResult returnResult=null;
		ReturnMessageCallable returnMessageCallable = new ReturnMessageCallable(futureList, directoryMessageCounter);
		Future<ReturnResult> resultHistogramMessageFuture =server.getService().submit(returnMessageCallable);
		try {
			 returnResult = resultHistogramMessageFuture.get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

      return returnResult;
	}

	@Override
	public void process(TerminateConnection terminateConnection) {
		// TODO: implement me



	}

}
