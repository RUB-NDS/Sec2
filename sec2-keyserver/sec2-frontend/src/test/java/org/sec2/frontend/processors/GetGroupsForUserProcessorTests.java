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
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GetGroupsForUser;
import org.sec2.saml.xml.GroupList;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for GetGroupsForUserProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 22, 2013
 */
public class GetGroupsForUserProcessorTests
                extends AbstractGroupRequiringTests {
    /**
     * The processor to test.
     */
    private GetGroupsForUserProcessor proc;

    /**
     * Some test message.
     */
    private GetGroupsForUser content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        super.setUp();
        proc = new GetGroupsForUserProcessor();
        try {
            content = SAMLEngine.getXMLObject(GetGroupsForUser.class);
            content.setValue(TestKeyProvider.getInstance().getUserIDBase64());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests getting a list of known groups.
     */
    public void testCorrectGetGroupsForUserRequest() {
        GroupList list = fetchGroupsList();
        assertNotNull(list);
        assertEquals(2, list.getGroups().size()); //2= private group & testgroup
    }

    /**
     * @return a list of groups the test user is member of
     */
    private GroupList fetchGroupsList() {
        GroupList list = null;
        try {
            list = (GroupList) proc.process(content,
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
