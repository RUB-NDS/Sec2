/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GetUserInfoByMail;
import org.sec2.saml.xml.UserInfo;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for GetUserInfoByMailProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 24, 2013
 */
public class GetUserInfoByMailProcessorTests extends TestCase {
    /**
     * The processor to test.
     */
    private GetUserInfoByMailProcessor proc;

    /**
     * Some test message.
     */
    private GetUserInfoByMail content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        proc = new GetUserInfoByMailProcessor();
        try {
            content = SAMLEngine.getXMLObject(GetUserInfoByMail.class);
            content.setValue("anotheruser@sec2.org");
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests getting information about user2.
     */
    public void testCorrectGetUserInfoByMailRequest() {
        UserInfo user = null;
        try {
            user = (UserInfo) proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }

        assertNotNull(user);
        assertEquals(user.getEmailAddress().getValue(), "anotheruser@sec2.org");
    }

    /**
     * Delete references.
     */
    @Override
    public void tearDown() {
        proc = null;
        content = null;
    }
}
