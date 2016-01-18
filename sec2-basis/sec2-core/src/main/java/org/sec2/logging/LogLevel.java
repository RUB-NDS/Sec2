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

import org.sec2.logging.procedures.Attention;
import org.sec2.logging.procedures.Debug;
import org.sec2.logging.procedures.Emergency;
import org.sec2.logging.procedures.IProcedure;
import org.sec2.logging.procedures.Problem;
import org.sec2.logging.procedures.Status;
import org.sec2.logging.procedures.Trace;

/**
 * Listing of all valid log levels.
 *
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * 03.01.2011
 */
public enum LogLevel {

    /**
     * Critical Failure - further intervention by an operator is required.
     */
    ATTENTION(new Attention(), "A"),
    /**
     * Debugging output - should only be used by developers.
     */
    DEBUG(new Debug(), "D"),
    /**
     * Emergency case - further operation not possible.
     */
    EMERGENCY(new Emergency(), "E"),
    /**
     * Non-critical failure - no further handling required.
     */
    PROBLEM(new Problem(), "P"),
    /**
     * Status information - no further handling required.
     */
    STATUS(new Status(), "S"),
    /**
     * Trace information - just to tell about the current the control flow.
     */
    TRACE(new Trace(), "T");
    /**
     * Associated procedure.
     */
    private IProcedure procedure;
    /**
     * Associated mnemonic.
     */
    private String mnemonic;

    /**
     * Default constructor for every log level.
     *
     * @param procedureToCall   Procedure to call on incidence
     * @param logLevelMnemonic  Mnemonic of this log level
     */
    LogLevel(final IProcedure procedureToCall, final String logLevelMnemonic) {
        this.procedure = procedureToCall;
        this.mnemonic = logLevelMnemonic;
    }

    /**
     * Getter for procedure.
     *
     * @return  Associated procedure
     */
    public IProcedure getProcedure() {
        return this.procedure;
    }

    /**
     * Getter for mnemonic.
     *
     * @return  Associated mnemonic
     */
    public String getMnemonic() {
        return this.mnemonic;
    }
}
