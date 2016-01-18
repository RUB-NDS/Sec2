/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.logging;

import org.sec2.core.Constants;
import org.sec2.core.TSDateFormatter;
import org.sec2.logging.logmodules.ILogModule;
import org.sec2.logging.procedures.IProcedure;
import java.util.Date;
import org.sec2.logging.exceptions.ExCouldNotLog;
import org.sec2.logging.exceptions.ExCouldNotOpenLog;

/**
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * 03.01.2011
 *
 * Central logging service for every module.
 */
public final class Logger {

    /**
     * Logging module behind the scenes, log module backend.
     */
    private static ILogModule logModule = null;

    /**
     * Private constructor.
     * No direct instances of this class are allowed.
     */
    private Logger() {
    }

    /**
     * Setup routine to bind a log module.
     *
     * @param moduleForLogging     Log module to bind
     * @throws ExCouldNotOpenLog    The log channel could not be opened
     */
    public static void setup(final ILogModule moduleForLogging)
            throws ExCouldNotOpenLog {
        Logger.logModule = moduleForLogging;
        Logger.logModule.openLog();
    }

    /**
     * Logs a message depending on its log level.
     *
     * @param message      Message to be logged
     * @param logLevel     Associated log level
     */
    public static void log(final String message, final LogLevel logLevel) {
        log(message, logLevel, null);
    }

    /**
     * Logs a message depending on its log level.
     *
     * @param message      Message to be logged
     * @param logLevel     Associated log level
     * @param stackTrace   StackTrace (optional)
     */
    public static void log(final String message, final LogLevel logLevel,
            final StackTraceElement[] stackTrace) {
        final StringBuilder newMessage = new StringBuilder(27);
        final TSDateFormatter dateFormatter =
                new TSDateFormatter("yyyy.MM.dd HH:mm:ss z");
        final IProcedure procedure = logLevel.getProcedure();
        String[] logMessageParts;
        String prefix;

        // handle
        procedure.handle(message, stackTrace);
        logMessageParts = procedure.getResult();

        // build prefix
        newMessage.append(logLevel.getMnemonic());
        newMessage.append("|");
        newMessage.append(dateFormatter.format(new Date()));
        newMessage.append("# ");
        prefix = newMessage.toString();
        newMessage.setLength(0);

        // prepare new message
        for (String line : logMessageParts) {
            newMessage.append(prefix);
            newMessage.append(line);
            newMessage.append(Constants.LINE_SEPARATOR);
        }

        // log and forget!
        Logger.log(newMessage.toString());
    }

    /**
     * Handle the message to the logging module.
     * This routine is thread safe!
     *
     * @param message   Message to be logged
     */
    private static synchronized void log(final String message) {
        // TODO we need a logging queue here
        // and another thread, dedicated to run logging modules
        try {
            if (Logger.logModule != null) {
                Logger.logModule.log(message);
            }
        } catch (ExCouldNotLog e) {
            // logging here may cause problems!
            System.out.println("Logging not possible - messages could not be "
                    + "logged by the configured log module!");
        }
    }
}
