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
package org.sec2.frontend.processors;

import junit.framework.TestCase;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.Sec2RequestMessage;

/**
 * Tests for Sec2MessageProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 29, 2012
 */
public class Sec2MessageProcessorTests extends TestCase {

    /**
     * The processor to test.
     */
    private Sec2MessageProcessor proc;

    /**
     * Create processor.
     */
    @Override
    public void setUp() {
        proc = new Sec2MessageProcessor();
    }

    /**
     * Delete references.
     */
    @Override
    public void tearDown() {
        proc = null;
    }

    /**
     * Tests Sec2MessageProcessor.process(null).
     */
    public void testProcessNull() {
        try {
            proc.process(null);
            fail("Should have failed with an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        } catch (BackendProcessException e) {
            fail(e.toString());
        }
    }

    /**
     * Tests Sec2MessageProcessor.getProcessor(null).
     */
    public void testGetProcessorNull() {
        try {
            proc.getProcessor(null);
            fail("Should have failed with an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests that an unknown interface cannot be resolved.
     */
    public void testGetProcessorForUnknownInterface() {
        assertNull(proc.getProcessor(
                (new Sec2RequestMessage() { }).getClass()));
    }

    /**
     * Tests that an unknown interface cannot be processed.
     */
    public void testProcessForUnknownInterface() {
        BackendJob job = new BackendJob(new Sec2RequestMessage() { }, "AQI=",
                "testRequest");
        try {
            proc.process(job);
            fail("Should have failed with an BackendProcessException");
        } catch (BackendProcessException e) {
            assertNotNull(e);
        }
    }
}
