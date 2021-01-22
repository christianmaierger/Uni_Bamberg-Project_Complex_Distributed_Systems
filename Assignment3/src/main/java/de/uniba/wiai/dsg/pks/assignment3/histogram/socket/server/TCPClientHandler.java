package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Future;

public class TCPClientHandler implements ClientHandler {
	private final Socket client;
	private final TCPDirectoryServer server;
	private final LinkedList<Future<Histogram>> futureList;
	private int directoryMessageCounter;
	boolean running = true;
	private Future<ReturnResult> resultHistogramMessageFuture;


	// zweiter Konst mit server könnte bei Kommunikation mit Server helfen
	public TCPClientHandler(Socket client, TCPDirectoryServer server) {
		this.client = client;
		this.server = server;
		this.futureList = new LinkedList<>();
		this.directoryMessageCounter=0;
	}

	public TCPDirectoryServer getServer() {
		return server;
	}

	public Socket getClient() {
		return client;
	}

	public LinkedList<Future<Histogram>> getFutureList() {
		return futureList;
	}

	public int getDirectoryMessageCounter() {
		return directoryMessageCounter;
	}

	public void setDirectoryMessageCounter(int directoryMessageCounter) {
		this.directoryMessageCounter = directoryMessageCounter;
	}

	@Override
	public void run() {
		// TODO implement me
		try (ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {


			// evtl in anderen Thread auslagern der messages annimt, aber der muss ja trotzdem die methoden hier aufrufen?!
			// messages vielleicht in eine queue, aber ist das nicht irgendwie sequentielll, gut irgendwie ist es immer nacheinander

			while (running) {

				Object object = in.readObject();


				// nicht ohne test casten
				if (object instanceof ParseDirectory) {
					ParseDirectory directoryMessage = (ParseDirectory) object;
					// der counter soll mir die dirs zählen, damit ich weiß wann ich das result schicken kann
					//das ist ja dann fertig wenn ich genau so viele futures wie dirs hab, da keine concurrency
					// sollte ein einfaches int langen statt long adder Spielereien oder?
					setDirectoryMessageCounter(getDirectoryMessageCounter()+1);
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
		} catch (IOException | ClassCastException e) {
			running = false;
			System.err.println("Connection error " + e.getMessage());
		} catch (ClassNotFoundException e) {
			running = false;
			System.err.println("Incoming message type could not be handled " + e.getMessage());
		}
	}

	@Override
	public void process(ParseDirectory parseDirectory) {
		// TODO: implement me
		TraverseFolderCallable folderTask = new TraverseFolderCallable(parseDirectory.getPath(), parseDirectory.getFileExtension(), server);

		Future<Histogram> folderFuture = server.getService().submit(folderTask);


			futureList.add(folderFuture);




	}

	@Override
	public ReturnResult process(GetResult getResult) {
		// TODO: implement me
		// wieder überleben was bei null machen
		// denke einfach an client und der weiß, oh das war ne exception
		ReturnResult returnResult = null;



/*             alter Weg mit komplizierter future Liste

				returnResult = resultHistogramMessageFuture.get();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

*/
		returnResult= new ReturnResult(server.getSubResultHistogram());

		return returnResult;
	}

	@Override
	public void process(TerminateConnection terminateConnection) {
		// TODO: implement me
		// hier bin ich mir echt nicht sicher, denke aber disconnect aufrufen reicht erstmal?
		// vielleicht auch insgesamt beim exc handling nur disconnecten und dem Server so ein weiteres glückliches Leben ermöglichen?
		// sry für die Wortspielereien

		// in einem von 20 Fällen ca wird das zu spät denke ich aufgerufen und es kann passieren dass zb angeblich 8 Verzeichnisse traversiert wurden

		// ich denke wir kriegen die terminate message um festzustellen ist es gut oder schlechtFall Termination?

			server.disconnect(this);

	}

}
