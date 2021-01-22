package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.helpers;

import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.ClientHandler;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketException;


public class ResultCalculator implements Runnable {
    private final ObjectOutputStream out;
    private final ClientHandler clientHandler;

    public ResultCalculator(ObjectOutputStream out, ClientHandler clientHandler){
        this.out = out;
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        ReturnResult result = clientHandler.process(new GetResult());
        try {
            out.writeObject(result);
            out.flush();
        } catch (IOException exception) {
            System.err.println("OutputStream problem. Client has probably closed socket.");
        }
    }
}
