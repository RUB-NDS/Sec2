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
package org.sec2.frontend.exceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic tests for keyserver exceptions.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @param <T> The exception to test
 * @version 0.1
 *
 * November 05, 2012
 */
public abstract class AbstractKeyserverExceptionTests<T extends
                AbstractKeyserverException> extends TestCase {

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
            AbstractKeyserverExceptionTests.class);

    /**
     * The concrete class to test.
     */
    private Class<T> clazz = getClassLiteral();

    /**
     * @return A concrete literal for the generic type
     */
    protected abstract Class<T> getClassLiteral();

    /**
     * Tests the default constructor.
     */
    public void testDefaultConstructor() {
        T ex = null;
        try {
            ex = clazz.newInstance();
            ex.log();
            assertNotNull(ex);
        } catch (InstantiationException e) {
            log.debug("{} has no default constructor", clazz);
            assertNull(ex);
        } catch (IllegalAccessException e) {
            log.debug("Default constructor of {} is private", clazz);
            assertNull(ex);
        }
    }

    /**
     * Tests the message only constructor.
     */
    public void testMessageConstructor() {
        T ex = null;
        try {
            Constructor<T> c = clazz.getConstructor(String.class);
            ex = c.newInstance(MSG);
            ex.log();
            assertNotNull(ex);
        } catch (InstantiationException e) {
            log.debug("{} cannot be instantiated with a message", clazz);
            assertNull(ex);
        } catch (IllegalAccessException e) {
            log.debug("Message constructor of {} is private", clazz);
            assertNull(ex);
        } catch (NoSuchMethodException e) {
            log.debug("{} has no message constructor", clazz);
            assertNull(ex);
        } catch (InvocationTargetException e) {
            log.debug("Message constructor of {} threw an exception", clazz);
            assertNull(ex);
        }
    }

    /**
     * Tests the cause only constructor.
     */
    public void testExceptionConstructor() {
        T ex = null;
        try {
            Constructor<T> c = clazz.getConstructor(Throwable.class);
            ex = c.newInstance(EXC);
            ex.log();
            assertNotNull(ex);
        } catch (InstantiationException e) {
            log.debug("{} cannot be instantiated with a message and a throwable"
                    , clazz);
            assertNull(ex);
        } catch (IllegalAccessException e) {
            log.debug("Message and throwable constructor of {} is private",
                    clazz);
            assertNull(ex);
        } catch (NoSuchMethodException e) {
            log.debug("{} has no message and throwable constructor", clazz);
            assertNull(ex);
        } catch (InvocationTargetException e) {
            log.debug("Message and throwable constructor of {} threw an "
                    + "exception", clazz);
            assertNull(ex);
        }
    }

    /**
     * Tests the message and cause constructor.
     */
    public void testMessageExceptionConstructor() {
        T ex = null;
        try {
            Constructor<T> c = clazz.getConstructor(String.class,
                    Throwable.class);
            ex = c.newInstance(MSG, EXC);
            ex.log();
            assertNotNull(ex);
        } catch (InstantiationException e) {
            log.debug("{} cannot be instantiated with a throwable", clazz);
            assertNull(ex);
        } catch (IllegalAccessException e) {
            log.debug("Throwable constructor of {} is private", clazz);
            assertNull(ex);
        } catch (NoSuchMethodException e) {
            log.debug("{} has no throwable constructor", clazz);
            assertNull(ex);
        } catch (InvocationTargetException e) {
            log.debug("Throwable constructor of {} threw an exception", clazz);
            assertNull(ex);
        }
    }
}
