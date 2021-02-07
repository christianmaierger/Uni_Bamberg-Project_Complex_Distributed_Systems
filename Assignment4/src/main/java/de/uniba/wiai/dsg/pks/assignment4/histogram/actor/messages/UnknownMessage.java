package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

public final class UnknownMessage {
    private final String messageType;

    public UnknownMessage(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType(){
        return messageType;
    }

}
