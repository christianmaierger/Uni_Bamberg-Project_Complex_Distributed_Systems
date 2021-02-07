package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.uniba.wiai.dsg.pks.assignment.model.Histogram;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.HistogramRequest;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.LogMessage;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.LogMessageType;

import java.util.Optional;

public class OutputActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(){
        return Props.create(OutputActor.class, () -> new OutputActor());
    }

    @Override
    public void preStart() {
        log.debug("Starting OutputActor");
    }

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) {
        log.error(
                reason,
                "Restarting due to [{}] when processing [{}]",
                reason.getMessage(),
                message.orElse(""));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LogMessage.class, this::logMessage)
                .matchAny(msg -> log.warning("Received unknown message: {}", msg))
                .build();
    }

    private void logMessage(LogMessage logMessage){
        log.info("\n\tProcessing of {} {} has finished. Result Histogram:\n\t{}",
                logMessage.getLogMessageType().toString(), logMessage.getPath(), logMessage.getHistogram().toString());
    }
}