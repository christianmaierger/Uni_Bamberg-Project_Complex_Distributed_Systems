package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;
import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.concurrent.Future;

// so gesehen threadsafe, es geht ja nichts raus, die liste wird halt verändert
public class ReturnMessageRunnable implements Runnable {

    private final LinkedList<Future<Histogram>> futureList;
    @GuardedBy(value="itself")
    private final TCPClientHandler tcpClientHandler;
    @GuardedBy(value="itself")
    private final GetResult getResultMessage;



    public ReturnMessageRunnable(TCPClientHandler tcpClientHandler, GetResult getResultMessage) {
        this.tcpClientHandler=tcpClientHandler;
        this.getResultMessage=getResultMessage;
        this.futureList=tcpClientHandler.getFutureList() ;

    }

    public void run() {
        //todo Hier beachten wir brauchen auch die utils Klasse oder wir verlagern addUpAllFields in
        // überlegen ob es passt, bzw was machen wenn es null ist

        ReturnResult resultMessage = null;

        // ich kann doch aber so nicht garantieren, dass die liste schon voll ist, doch message counter?

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
