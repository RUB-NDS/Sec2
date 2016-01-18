/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */

package org.sec2.logging.procedures;

/**
 * Procedure for Trace log level.
 * Nothing special to do, simply copy the message.
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 25, 2012
 */
public final class Trace extends AProcedure {

    @Override
    public void handle(final String message,
            final StackTraceElement[] stackTrace) {
         setResult(new String[]{message});
    }
}
