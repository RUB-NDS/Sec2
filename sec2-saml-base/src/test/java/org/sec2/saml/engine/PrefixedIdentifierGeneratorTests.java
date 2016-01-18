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
package org.sec2.saml.engine;

import java.security.NoSuchAlgorithmException;
import junit.framework.TestCase;

/**
 * Tests generating valid XML IDs.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 02, 2012
 */
public class PrefixedIdentifierGeneratorTests extends TestCase {

    /**
     * The regex for an ID of an XML element.
     */
    public static final String ID_REGEX = "_[0-9a-f\\-]{32}";

    /**
     * The generator which is tested.
     */
    private PrefixedIdentifierGenerator generator;

    /**
     * Set up: Get PrefixedIdentifierGenerator.
     * (Called before every test case method.)
     */
    @Override
    public void setUp() {
        try {
            generator = PrefixedIdentifierGenerator.getInstance();
        } catch (NoSuchAlgorithmException e) {
            fail(e.toString());
        }
    }

    /**
     * Tear down: Delete reference to PrefixedIdentifierGenerator.
     * (Called after every test case method.)
     */
    @Override
    public void tearDown() {
        generator = null;
    }

    /**
     * Tests generating an ID with null.
     */
    public void testNullID() {
        String testID = generator.generatePrefixedIdentifier(null);
        assertNotNull(testID);
        assertTrue(testID.matches(ID_REGEX));
    }

    /**
     * Tests generating an ID with a string prefix.
     */
    public void testStringID() {
        String testID = generator.generatePrefixedIdentifier("test");
        assertNotNull(testID);
        assertTrue(testID.matches("test" + ID_REGEX));
    }

    /**
     * Tests generating an ID with a number in front.
     */
    public void testNumberID() {
        String testID = generator.generatePrefixedIdentifier("123");
        assertNotNull(testID);
        assertTrue(testID.matches("_123" + ID_REGEX));
    }
}
