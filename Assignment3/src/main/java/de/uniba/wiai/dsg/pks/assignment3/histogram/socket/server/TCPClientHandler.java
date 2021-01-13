package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ParseDirectory;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

public class TCPClientHandler implements ClientHandler {

	@Override
	public void run() {
		// TODO implement me

	}

	@Override
	public void process(ParseDirectory parseDirectory) {
		// TODO: implement me
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
