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
 * Procedure for Emergency log level.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * 03.01.2011
 */
public final class Emergency extends AProcedure {

    /**
     * Header and footer for emergency messages.
     */
    private static final String HEADERFOOTER =
            "============= Emergency =============";

    @Override
    public void handle(final String message,
            final StackTraceElement[] stackTrace) {
        String[] result = new String[3];
        result[0] = HEADERFOOTER;
        result[1] = message;
        if (stackTrace == null) {
            result[2] = "  " + this.getOrigin();
        } else {
            result[2] = "  " + this.getOrigin(stackTrace);
        }

        setResult(result);

    }
}
