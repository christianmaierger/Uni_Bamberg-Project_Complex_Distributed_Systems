package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.helpers;


import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.TCPClientHandler;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.TerminateConnection;

import java.io.IOException;
import java.io.ObjectOutputStream;



public class ResultCalculator implements Runnable {
    private final ObjectOutputStream out;
    private final TCPClientHandler clientHandler;
    private final int number;

    public ResultCalculator(ObjectOutputStream out, TCPClientHandler clientHandler, int number){
        this.out = out;
        this.clientHandler = clientHandler;
        this.number = number;
    }

    @Override
    public void run() {
        ReturnResult result = clientHandler.process(new GetResult());
        try {
            out.writeObject(result);
            out.flush();
        } catch (IOException exception) {
            System.err.println("ClientHandler #" + number + ":\tIOException: " + exception.getMessage() + ".");
            clientHandler.process(new TerminateConnection());
        }
    }
}
