package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.concurrent.Future;

public class ReturnMessageRunnable implements Runnable {
    private final LinkedList<Future<Histogram>> futureList;
    private final TCPClientHandler tcpClientHandler;
    private final GetResult getResultMessage;



    public ReturnMessageRunnable(LinkedList<Future<Histogram>> futureList, TCPClientHandler tcpClientHandler, GetResult getResultMessage, boolean calculationCallable) {
        this.futureList=futureList;
        this.tcpClientHandler=tcpClientHandler;
        this.getResultMessage=getResultMessage;

    }

    public void run() {
        //todo Hier beachten wir brauchen auch die utils Klasse oder wir verlagern addUpAllFields in
        // überlegen ob es passt, bzw was machen wenn es null ist

        ReturnResult resultMessage = null;



        while (futureList.size()>0) {
            futureList.removeIf(Future::isDone);
        }



        resultMessage = tcpClientHandler.process(getResultMessage);


            try {
                ObjectOutputStream out = new ObjectOutputStream(tcpClientHandler.getClient().getOutputStream());
                out.flush();

                out.writeObject(resultMessage);

            } catch (IOException e) {
                System.err.println("SERVER | ReturnMessageCallable: ResultMessage could not be sent to Client");
                // evtl nochmal versuchen?
                //todo
            }

    }
}
