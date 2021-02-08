package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import de.uniba.wiai.dsg.pks.assignment.model.Histogram;

import java.util.concurrent.CompletableFuture;

public final class FutureMessage {
    private final CompletableFuture<Object> future;


    public FutureMessage(CompletableFuture<Object> future) {
        this.future = future;
    }

    public CompletableFuture<Object> getFuture(){
        return future;
    }
}
