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

import org.sec2.exceptions.ACommonException;
import org.sec2.logging.Logger;

/**
 * Procedure template. Defines basic primitives for all procedures.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * 03.01.2011
 * PACKAGE-PRIVATE
 */
abstract class AProcedure implements IProcedure {

    /**
     * Own cannonical package name.
     */
    private static final String PROCEDURES_PKG =
            AProcedure.class.getCanonicalName().substring(0, AProcedure.class.
            getCanonicalName().lastIndexOf('.'));
    /**
     * Cannonical package name of exceptions.
     */
    private static final String EXCEPTIONS_PKG =
            ACommonException.class.getCanonicalName().substring(0,
            ACommonException.class.getCanonicalName().lastIndexOf('.'));
    /**
     * Cannonical name of the logger.
     */
    private static final String LOGGER_CNAME =
            Logger.class.getCanonicalName();
    /**
     * Cannonical name of thread class.
     */
    private static final String THREAD_CNAME =
            java.lang.Thread.class.getCanonicalName();
    /**
     * Result string.
     */
    private String[] result;

    /**
     * This routine tries to determine the origin of the log call.
     *
     * @return      Origin of the log call as precise as possible
     */
    protected String getOrigin() {
        final StackTraceElement[] stackTrace = Thread.currentThread().
                getStackTrace();

        return getOrigin(stackTrace);
    }

    /**
     * This routine tries to determine the origin of the log call.
     *
     * @param stackTrace    Stack trace elements to look for origin
     * @return              Origin of the log call as precise as possible
     */
    protected String getOrigin(final StackTraceElement[] stackTrace) {
        final StringBuilder origin = new StringBuilder("undetermined");
        String className;

        // look for log causing originator
        for (StackTraceElement curSTE : stackTrace) {
            // skip NULLs
            if (curSTE == null) {
                continue;
            }

            className = curSTE.getClassName();
            // skip ourselves
            if (!className.contains(PROCEDURES_PKG)
                    && !className.contains(EXCEPTIONS_PKG)
                    && !className.equals(LOGGER_CNAME)
                    && !className.equals(THREAD_CNAME)) {
                // found originator
                origin.setLength(0);
                origin.append("Origin: ");
                origin.append(curSTE.getClassName());
                origin.append(".");
                origin.append(curSTE.getMethodName());
                origin.append(":");
                origin.append(curSTE.getLineNumber());
                break;
            }
        }

        return origin.toString();
    }

    /**
     * Getter for result.
     * @return      Result after processing
     */
    @Override
    public String[] getResult() {
        return this.result;
    }

    /**
     * Setter for result.
     * @param operationResult    Result to set
     */
    protected void setResult(final String[] operationResult) {
        this.result = new String[operationResult.length];
        System.arraycopy(operationResult, 0, this.result, 0,
                operationResult.length);
    }
}
