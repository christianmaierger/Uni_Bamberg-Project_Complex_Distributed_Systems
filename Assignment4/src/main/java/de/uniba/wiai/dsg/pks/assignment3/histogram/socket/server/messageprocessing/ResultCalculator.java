package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.messageprocessing;


import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server.TCPClientHandler;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * This class sums up the frequency analysis of a Client and send the result to the Client
 * via a ReturnResult message.
 */
@NotThreadSafe
public class ResultCalculator implements Runnable {
    private final ObjectOutputStream out;
    private final TCPClientHandler clientHandler;
    @GuardedBy(value = "itself")
    private final int number;

    public ResultCalculator(ObjectOutputStream out, TCPClientHandler clientHandler, int number){
        this.out = out;
        this.clientHandler = clientHandler;
        this.number = number;
    }

    /**
     * Waits until all parseDirectory tasks have been processed and summed up in a ReturnResult, which is then sent to
     * the Client.
     */
    @Override
    public void run() {
        ReturnResult result = clientHandler.process(new GetResult());
        try {
            if(!Thread.currentThread().isInterrupted()){
                out.writeObject(result);
                out.flush();
            }
        } catch (IOException exception) {
            System.err.println("ClientHandler #" + number + ":\tIOException while returning result: " + exception.getMessage() + ".");
        }
    }
}
