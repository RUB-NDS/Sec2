/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.pipeline;

import junit.framework.TestCase;
import org.sec2.pipeline.datatypes.XMLSecurityConstants;
import org.sec2.pipeline.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyInfo parsing tests
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date Aug 20, 2013
 * @version 0.1
 *
 */
public class UtilsTest extends TestCase {

    /**
     * SLF4J Logger.
     *
     */
    Logger log = LoggerFactory.getLogger(UtilsTest.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UtilsTest(String testName) {
        super(testName);
    }

    public void testRandomGeneration() throws IllegalArgumentException {
        RandomUtils ru = RandomUtils.getInstance();
        
        byte[] iv;
        
        // test cbc iv length 
        iv = ru.generateIV(XMLSecurityConstants.Algorithm.AES128CBC.javaURI);
        assertEquals(16, iv.length);
        
        // test gcm iv length
        iv = ru.generateIV(XMLSecurityConstants.Algorithm.AES128GCM.javaURI);
        assertEquals(12, iv.length);
        
        // test unknown iv length
        Exception e = null;
        try {
            iv = ru.generateIV("none-algorithm");
        } catch (IllegalArgumentException ex) {
            e = ex;
        }
        assertNotNull(e);        
    }
}
