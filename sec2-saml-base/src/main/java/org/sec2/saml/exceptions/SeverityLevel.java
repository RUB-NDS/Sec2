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
package org.sec2.saml.exceptions;

/**
 * Severity labels for a log message.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 08, 2012
 */
public enum SeverityLevel {
    /**
        * Messages of trace-level are for extended debugging.
        */
    TRACE,
    /**
        * Messages of debug-level are for debugging.
        */
    DEBUG,
    /**
        * Messages of info-level are for non-critical status-information.
        */
    INFO,
    /**
        * Messages of warn-level are for critical infomation, that do not
        * prevent further processing.
        */
    WARN,
    /**
        * Messages of warn-level are for critical infomation, that
        * prevent further processing.
        */
    ERROR;
}
