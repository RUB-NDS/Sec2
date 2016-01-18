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

package org.sec2.logging.procedures;

/**
 * Interface for all procedures.
 * Defines mandatory function calls that have to be implemented.
 *
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * 03.01.2011
 */
public interface IProcedure {
    /**
     * Entry point of the procedure.
     *
     * @param message       Delivered message
     * @param stackTrace    Delivered stackTrace (optional)
     */
    void handle(final String message, final StackTraceElement[] stackTrace);

    /**
     * Getter for result after execution of the procedure.
     *
     * @return Result message divided in parts
     */
    String[] getResult();
}
