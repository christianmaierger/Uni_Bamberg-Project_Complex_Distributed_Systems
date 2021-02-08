package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.LogMessage;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.LogMessageType;
import de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages.UnknownMessage;

import java.util.Optional;

public class OutputActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(){
        return Props.create(OutputActor.class, () -> new OutputActor());
    }

    @Override
    public void preStart() {
        log.debug("Starting OutputActor");
    }
    //FIXME: nachschauen wie man debug nachrichten sehen kann

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LogMessage.class, this::logMessage)
                .match(UnknownMessage.class, this::logUnknownMessage)
                .matchAny(this::logUnknownMessage)
                .build();
    }

    private void logMessage(LogMessage logMessage) {

        if (logMessage.getLogMessageType()== LogMessageType.FOLDER) {
            log.info("\n\tProcessing of {} {} has finished. Result Histogram:\n\t{}",
                    logMessage.getLogMessageType().toString(), logMessage.getPath(), logMessage.getHistogram().toString());
        } else {
            log.info("\n\tProcessing of {} {} has finished.",
                    logMessage.getLogMessageType().toString(), logMessage.getPath());
        }
    }

    private void logUnknownMessage(UnknownMessage message){
        log.warning(
                "\n\t{} received an unknown message of type {}", getSender(), message.getMessageType());
    }

    private void logUnknownMessage(Object message){
        log.warning(
                "\n\t{} received an unknown message of type {}", getSelf(), message.getClass().toString());
    }
}