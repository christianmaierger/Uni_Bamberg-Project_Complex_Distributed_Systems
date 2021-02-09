package de.uniba.wiai.dsg.pks.assignment4.histogram.actor.messages;

import java.util.Locale;

public enum LogMessageType {
    FILE, FOLDER, PROJECT;

    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
