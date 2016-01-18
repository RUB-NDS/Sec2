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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Generic tests for keyserver exceptions.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @param <T> The exception to test
 * @version 0.1
 *
 * December 03, 2012
 */
public abstract class AbstractSelfLoggingExceptionTests<T extends
                AbstractSelfLoggingException> extends TestCase {

    /**
     * Test message for the exception.
     */
    protected static final String MSG = "testmessage";

    /**
     * Test exception used as cause for the exception.
     */
    protected static final Throwable EXC = new Exception("inner-exception");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(
            AbstractSelfLoggingExceptionTests.class);

    /**
     * The concrete class to test.
     */
    private Class<T> clazz = getClassLiteral();

    /**
     * @return A concrete literal for the generic type
     */
    protected abstract Class<T> getClassLiteral();

    /**
     * Logs an exception.
     * @param exception the exception to log
     */
    private void logException(final T exception) {
        assertNotNull(exception);
        try {
            clazz.getMethod("log").invoke(exception);
        } catch (IllegalAccessException e) {
            log.error("Method log() of {} is private", clazz);
            fail(e.toString());
        } catch (NoSuchMethodException e) {
            log.error("{} has no log() method", clazz);
            fail(e.toString());
        } catch (InvocationTargetException e) {
            log.error("Method log() of {} threw an exception", clazz);
            fail(e.toString());
        }
    }

    /**
     * Tests the message only constructor.
     */
    public void testMessageConstructor() {
        T ex;
        try {
            Constructor<T> c = clazz.getConstructor(String.class);
            ex = c.newInstance(MSG);
            assertNotNull(ex);
            logException(ex);
        } catch (InstantiationException e) {
            log.error("{} cannot be instantiated with a message", clazz);
            fail(e.toString());
        } catch (IllegalAccessException e) {
            log.error("Message constructor of {} is private", clazz);
            fail(e.toString());
        } catch (NoSuchMethodException e) {
            log.error("{} has no message constructor", clazz);
            fail(e.toString());
        } catch (InvocationTargetException e) {
            log.error("Message constructor of {} threw an exception", clazz);
            fail(e.toString());
        }
    }

    /**
     * Tests the cause only constructor.
     */
    public void testExceptionConstructor() {
        T ex;
        try {
            Constructor<T> c = clazz.getConstructor(Throwable.class);
            ex = c.newInstance(EXC);
            assertNotNull(ex);
            logException(ex);
        } catch (InstantiationException e) {
            log.error("{} cannot be instantiated with a throwable", clazz);
            fail(e.toString());
        } catch (IllegalAccessException e) {
            log.error("Throwable constructor of {} is private", clazz);
            fail(e.toString());
        } catch (NoSuchMethodException e) {
            log.error("{} has no throwable constructor", clazz);
            fail(e.toString());
        } catch (InvocationTargetException e) {
            log.error("Throwable constructor of {} threw an exception", clazz);
            fail(e.toString());
        }
    }

    /**
     * Tests the message and cause constructor.
     */
    public void testMessageExceptionConstructor() {
        T ex;
        try {
            Constructor<T> c = clazz.getConstructor(String.class,
                    Throwable.class);
            ex = c.newInstance(MSG, EXC);
            assertNotNull(ex);
            logException(ex);
        } catch (InstantiationException e) {
            log.error("{} cannot be instantiated with a message and a"
                    + " throwable", clazz);
            fail(e.toString());
        } catch (IllegalAccessException e) {
            log.error("Message and throwable constructor of {} is private",
                    clazz);
            fail(e.toString());
        } catch (NoSuchMethodException e) {
            log.error("{} has no message and throwable constructor", clazz);
            fail(e.toString());
        } catch (InvocationTargetException e) {
            log.error("Message and throwable constructor of {} threw an"
                    + " exception", clazz);
            fail(e.toString());
        }
    }

    /**
     * Tests the 4Param Constructor.
     */
    public void test4ParamConstructor() {
        T ex;
        try {
            Constructor<T> c = clazz.getConstructor(String.class,
                    Throwable.class, SeverityLevel.class, Marker.class);
            ex = c.newInstance(MSG, EXC, SeverityLevel.ERROR,
                    MarkerFactory.getMarker("TestMarker"));
            assertNotNull(ex);
            logException(ex);
        } catch (InstantiationException e) {
            log.error("{} cannot be instantiated with 4 params", clazz);
            fail(e.toString());
        } catch (IllegalAccessException e) {
            log.error("4 params constructor of {} is private",
                    clazz);
            fail(e.toString());
        } catch (NoSuchMethodException e) {
            log.error("{} has no 4 params constructor", clazz);
            fail(e.toString());
        } catch (InvocationTargetException e) {
            log.error("4 params constructor of {} threw an exception", clazz);
            fail(e.toString());
        }
    }
}
