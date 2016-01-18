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

/**
 * Interface for log moules.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * 03.01.2011
 */
public interface ILogModule {

    /**
     * Opens the log channel.
     *
     * @throws ExCouldNotOpenLog    On problems during opening the log channel
     */
    void openLog() throws ExCouldNotOpenLog;

    /**
     * Closes the log channel.
     *
     * @throws ExCouldNotCloseLog   On problems while closing the log channel
     */
    void closeLog() throws ExCouldNotCloseLog;

    /**
     * Logs the handled message.
     *
     * @param message               Message to be logged
     * @throws ExCouldNotLog        On problems during logging attempt
     */
    void log(String message) throws ExCouldNotLog;
}
