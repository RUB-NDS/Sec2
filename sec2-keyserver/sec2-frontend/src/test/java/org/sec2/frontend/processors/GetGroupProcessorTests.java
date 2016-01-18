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

import static junit.framework.Assert.fail;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GetGroup;
import org.sec2.saml.xml.GroupInfo;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for GetKnownUsersForUserProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 18, 2013
 */
public class GetGroupProcessorTests extends AbstractGroupRequiringTests {

    /**
     * The processor to test.
     */
    private GetGroupProcessor proc;

    /**
     * Some test message.
     */
    private GetGroup content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        super.setUp();
        proc = new GetGroupProcessor();
        try {
            content = SAMLEngine.getXMLObject(GetGroup.class);
            content.setValue(GROUPNAME);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests getting a group.
     */
    public void testCorrectGetGroupRequest() {
        GroupInfo group = null;
        try {
            group = (GroupInfo) proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }

        assertNotNull(group);
        assertEquals(GROUPNAME, group.getGroupName().getValue());
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
