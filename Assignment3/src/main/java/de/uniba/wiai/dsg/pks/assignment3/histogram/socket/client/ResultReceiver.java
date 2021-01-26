package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;


import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import net.jcip.annotations.NotThreadSafe;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

/**
 * Offers a way to wait asynchronously and interruptibly for the next ReturnResult message on InputObjectStream in .
 */
@NotThreadSafe
public class ResultReceiver implements Callable<ReturnResult> {
    private final ObjectInputStream in;
    private final Socket server;

    public ResultReceiver(ObjectInputStream in, Socket server){
        this.in = in;
        this.server = server;
    }


    /**
     * Waits for the next message to be received via the instance variable InputObjectStream in and then returns this
     * message if it is of type ReturnResult. Otherwise, a HistogramServiceException will be thrown. Waiting is interruptible
     * and an interrupt will produce an InterruptedException.
     *
     */
    @Override
    public ReturnResult call() throws Exception {
        server.setSoTimeout(100);
        while(true){
            try{
                Object object = in.readObject();
                if (object instanceof ReturnResult){
                    System.out.println("Result has been received.");
                    return (ReturnResult) object;
                } else {
                    throw new HistogramServiceException("Unexpected message type.");
                }
            } catch (SocketTimeoutException exception){
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Execution has been interrupted.");
                }
            }
        }
    }
}