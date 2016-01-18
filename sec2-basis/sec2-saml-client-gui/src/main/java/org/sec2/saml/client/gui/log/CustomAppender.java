package org.sec2.saml.client.gui.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.observablecollections.ObservableCollections;

public class CustomAppender extends ConsoleAppender<LoggingEvent> {

    private static List<CustomLogEntry> myLog = ObservableCollections.observableList(new ArrayList<CustomLogEntry>());

    public List<CustomLogEntry> getMyLog() {
        return myLog;
    }

    @Override
    protected void append(LoggingEvent e) {
        super.append(e);
        addLoggingEvent(e);
    }

    public void clear() {
        myLog.clear();
    }

    public void addLoggingEvent(LoggingEvent e) {
        CustomLogEntry entry;
        if (myLog.isEmpty()) {
            entry = new CustomLogEntry("0");
            myLog.add(entry);
        } else {
            entry = myLog.get(0);
        }
        String msg = e.getFormattedMessage();
        if (msg.startsWith(CustomLogEntry.PREFIX_REQUEST_PLAIN)) {
            if (!CustomLogEntry.NULL_VALUE.equals(entry.getRequestPlain())) {
                entry = new CustomLogEntry(String.format("%d", myLog.size()));
                myLog.add(0, entry);
            }
            String formatted = XMLUtil.formatMessage(msg);
            entry.setRequestPlain(formatted);

        } else if (msg.startsWith(CustomLogEntry.PREFIX_REQUEST_ENCRYPTED)) {
            if (!CustomLogEntry.NULL_VALUE.equals(entry.getRequestEncrypted())) {
                entry = new CustomLogEntry(String.format("%d", myLog.size()));
                myLog.add(0, entry);
            }
            String formatted = XMLUtil.formatMessage(msg);
            entry.setRequestEncrypted(formatted);
        } else if (msg.startsWith(CustomLogEntry.PREFIX_RESPONSE_ENCRYPTED)) {
            if (!CustomLogEntry.NULL_VALUE.equals(entry.getResponseEncrypted())) {
                entry = new CustomLogEntry(String.format("%d", myLog.size()));
                myLog.add(0, entry);
            }
            String formatted = XMLUtil.formatMessage(msg);
            entry.setResponseEncrypted(formatted);
        } else if (msg.startsWith(CustomLogEntry.PREFIX_RESPONSE_PLAIN)) {
            if (!CustomLogEntry.NULL_VALUE.equals(entry.getResponsePlain())) {
                entry = new CustomLogEntry(String.format("%d", myLog.size()));
                myLog.add(0, entry);
            }
            String formatted = XMLUtil.formatMessage(msg);
            entry.setResponsePlain(formatted);
        }
    }
}
