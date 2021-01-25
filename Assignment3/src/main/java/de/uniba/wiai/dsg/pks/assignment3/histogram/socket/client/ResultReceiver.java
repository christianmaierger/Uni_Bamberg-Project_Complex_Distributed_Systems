package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.client;


import de.uniba.wiai.dsg.pks.assignment.model.HistogramServiceException;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

public class ResultReceiver implements Callable<ReturnResult> {
    private final ObjectInputStream in;
    private final Socket server;

    public ResultReceiver(ObjectInputStream in, Socket server){
        this.in = in;
        this.server = server;
    }

    @Override
    public ReturnResult call() throws Exception {
        server.setSoTimeout(100);
        while(true){
            try{
                Object object = in.readObject();
                if (object instanceof ReturnResult){
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