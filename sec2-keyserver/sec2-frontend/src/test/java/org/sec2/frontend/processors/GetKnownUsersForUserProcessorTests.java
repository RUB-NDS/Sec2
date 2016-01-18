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

import static junit.framework.Assert.assertNotNull;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GetKnownUsersForUser;
import org.sec2.saml.xml.UserList;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for GetKnownUsersForUserProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 18, 2013
 */
public class GetKnownUsersForUserProcessorTests
                extends AbstractGroupRequiringTests {
    /**
     * The processor to test.
     */
    private GetKnownUsersForUserProcessor proc;

    /**
     * Some test message.
     */
    private GetKnownUsersForUser content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        super.setUp();
        proc = new GetKnownUsersForUserProcessor();
        try {
            content = SAMLEngine.getXMLObject(GetKnownUsersForUser.class);
            content.setValue(TestKeyProvider.getInstance().getUserIDBase64());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests getting an empty list of known users.
     */
    public void testCorrectGetKnownUsersForUserRequestEmpty() {
        UserList list = fetchKnownUsersList();
        assertNotNull(list);
        assertTrue(list.getUserIDs().isEmpty());
    }

    /**
     * Tests getting a list of known users with an entry in it.
     */
    public void testCorrectGetKnownUsersForUserRequestWithEntry() {
        this.addSecondUserToTestGroup();
        UserList list = fetchKnownUsersList();
        assertNotNull(list);
        assertEquals(1, list.getUserIDs().size());
    }

    /**
     * @return a list of users known by the test user
     */
    private UserList fetchKnownUsersList() {
        UserList list = null;
        try {
            list = (UserList) proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }
        return list;
    }

    /**
     * Delete references.
     */
    @Override
    public void tearDown() {
        super.tearDown();
        proc = null;
        content = null;
    }
}
