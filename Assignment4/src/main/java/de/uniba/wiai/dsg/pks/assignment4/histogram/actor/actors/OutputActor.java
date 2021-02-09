package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.LogMessage;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.UnknownMessage;

public class OutputActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(OutputActor.class, OutputActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LogMessage.class, this::logMessage)
                .match(UnknownMessage.class, this::logUnknownMessage)
                .matchAny(this::logUnknownMessageForSelf)
                .build();
    }

    private void logMessage(LogMessage logMessage) {
        log.info("\n\tProcessing of {} {} has finished. Result Histogram:\n\t{}",
                logMessage.getLogMessageType().toString(), logMessage.getPath(), logMessage.getHistogram().toString());
    }

    private void logUnknownMessage(UnknownMessage message) {
        log.warning(
                "\n\t{} received an unknown message of type {}", getSender(), message.getMessageType());
    }

    private void logUnknownMessageForSelf(Object message) {
        log.warning(
                "\n\t{} received an unknown message of type {}", getSelf(), message.getClass().toString());
    }
}