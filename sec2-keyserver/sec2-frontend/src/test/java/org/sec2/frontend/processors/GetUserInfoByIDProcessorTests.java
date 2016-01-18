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
import org.sec2.saml.xml.GetUserInfoByID;
import org.sec2.saml.xml.UserInfo;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for GetUserInfoByIDProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 24, 2013
 */
public class GetUserInfoByIDProcessorTests extends TestCase {
    /**
     * The processor to test.
     */
    private GetUserInfoByIDProcessor proc;

    /**
     * Some test message.
     */
    private GetUserInfoByID content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        proc = new GetUserInfoByIDProcessor();
        try {
            content = SAMLEngine.getXMLObject(GetUserInfoByID.class);
            content.setValue(TestKeyProvider.getInstance().getUser2IDBase64());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests getting information about user2.
     */
    public void testCorrectGetUserInfoByIDRequest() {
        UserInfo user = null;
        try {
            user = (UserInfo) proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }

        assertNotNull(user);
        assertEquals(user.getUserID().getValue(),
                TestKeyProvider.getInstance().getUser2IDBase64());
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
