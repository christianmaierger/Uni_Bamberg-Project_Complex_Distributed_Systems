package de.uniba.wiai.dsg.pks.assignment3.histogram.socket.server;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment2.histogram.threaded.shared.Utils;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.GetResult;
import de.uniba.wiai.dsg.pks.assignment3.histogram.socket.shared.ReturnResult;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ReturnMessageCallable implements Callable {
    private final LinkedList<Future<Histogram>> futureList;
    private boolean resultNotReady;
    private final TCPClientHandler tcpClientHandler;
    private final GetResult getResultMessage;
    private final boolean calculationCallable;


    public ReturnMessageCallable(LinkedList<Future<Histogram>> futureList, TCPClientHandler tcpClientHandler, GetResult getResultMessage, boolean calculationCallable) {
        this.futureList=futureList;
        this.calculationCallable = calculationCallable;
        this.resultNotReady=true;
        this.tcpClientHandler=tcpClientHandler;
        this.getResultMessage=getResultMessage;
        this.calculationCallable=calculationCallable;
    }

    public ReturnResult call() {
        //todo Hier beachten wir brauchen auch die utils Klasse oder wir verlagern addUpAllFields in
        // überlegen ob es passt, bzw was machen wenn es null ist

        Histogram resultHistogram = null;
        ReturnResult resultMessage = null;

        if (!calculationCallable) {

           resultMessage = tcpClientHandler.process(getResultMessage);

            try (ObjectOutputStream out = new ObjectOutputStream(tcpClientHandler.getClient().getOutputStream())) {
                out.flush();

                out.writeObject(resultMessage);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {



            while (resultNotReady) {


                resultNotReady = false;

                for (Future<Histogram> future : futureList) {
                    Histogram subResult;
                    try {
                        subResult = future.get();
                        resultHistogram = Utils.addUpAllFields(subResult, resultHistogram);

                        //todo Es handling ist nur ein draft
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }


                resultMessage = new ReturnResult(resultHistogram);

            }


        }
        return resultMessage; }
}
