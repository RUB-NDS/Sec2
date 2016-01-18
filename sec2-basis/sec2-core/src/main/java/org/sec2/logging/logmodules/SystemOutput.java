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
package org.sec2.logging.logmodules;

import org.sec2.logging.exceptions.ExCouldNotCloseLog;
import org.sec2.logging.exceptions.ExCouldNotLog;
import org.sec2.logging.exceptions.ExCouldNotOpenLog;
import java.io.PrintStream;

/**
 * Log module for System output. Logs everything to System.out.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * 03.01.2011
 */
public final class SystemOutput implements ILogModule {

    /**
     * Output stream.
     */
    private PrintStream outputStream;

    /**
     * {@inheritDoc}
     */
    public void openLog() throws ExCouldNotOpenLog {
        setOutputStream(new PrintStream(System.out));
    }

    /**
     * {@inheritDoc}
     */
    public void closeLog() throws ExCouldNotCloseLog {
        if (getOutputStream() == null) {
            // remember that activating logging here would cause loops
            throw new ExCouldNotCloseLog(null, null, null);
        }

        getOutputStream().close();
    }

    /**
     * {@inheritDoc}
     */
    public void log(final String message) throws ExCouldNotLog {
        if (getOutputStream() == null) {
            // remember that activating logging here would cause loops
            throw new ExCouldNotLog(null, null, null);
        }

        getOutputStream().print(message);
    }

    /**
     * Getter for output stream.
     *
     * @return The output stream
     */
    private PrintStream getOutputStream() {
        return outputStream;
    }

    /**
     * Setter for output stream.
     *
     * @param outputPrintStream Output stream to set
     */
    private void setOutputStream(final PrintStream outputPrintStream) {
        this.outputStream = outputPrintStream;
    }
}
