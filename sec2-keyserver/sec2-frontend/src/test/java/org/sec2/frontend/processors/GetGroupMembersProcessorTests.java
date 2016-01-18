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

import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GetGroupMembers;
import org.sec2.saml.xml.UserList;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for GetGroupMembersProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 16, 2013
 */
public class GetGroupMembersProcessorTests extends AbstractGroupRequiringTests {

    /**
     * The processor to test.
     */
    private GetGroupMembersProcessor proc;

    /**
     * Some test message.
     */
    private GetGroupMembers content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        super.setUp();
        proc = new GetGroupMembersProcessor();
        try {
            content = SAMLEngine.getXMLObject(GetGroupMembers.class);
            content.setValue(GROUPNAME);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests getting a group's member list.
     */
    public void testCorrectGetGroupMembersRequest() {
        UserList list = null;
        try {
            list = (UserList) proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }

        assertNotNull(list);
        assertEquals(1, list.getUserIDs().size());
        assertEquals(TestKeyProvider.getInstance().getUserIDBase64(),
                list.getUserIDs().get(0).getValue());
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
