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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.slf4j.LoggerFactory;

/**
 * This testsetup is used to deactivate logging while tests run.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 16, 2012
 */
public class SuppressLogTestSetup extends TestSetup {

    /**
     * The appender to restore.
     */
    private Appender<ILoggingEvent> appender;

    /**
     * The root logger.
     */
    private Logger root;

    /**
     * Create the test setup.
     *
     * @param test the test case
     */
    public SuppressLogTestSetup(final Test test) {
        super(test);
    }

    /**
     * Detaches the stdout-appender.
     */
    @Override
    public void setUp() {
        LoggerContext lc;
        try {
            lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        } catch (ClassCastException e) {
            LoggerFactory.getLogger(SuppressLogTestSetup.class).warn(
                    "Obviously you are not using logback as logging framework. "
                    + "Logging cannot be deactivated.");
            return;
        }
        root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        root.info("Log-Output disabled");
        appender = root.getAppender("STDOUT");
        root.detachAppender("STDOUT");
    }

    /**
     * Attaches the stdout-appender.
     */
    @Override
    public void tearDown() {
        if (appender != null) {
            root.addAppender(appender);
            root.info("Log-Output re-enabled");
        }
    }
}
