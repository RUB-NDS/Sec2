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

import junit.framework.TestCase;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.frontend.samlengine.KeyserverSAMLEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.CreateGroup;
import org.sec2.saml.xml.GroupName;
import org.sec2.saml.xml.GroupOwnerID;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for CreateGroupProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 11, 2013
 */
public class CreateGroupProcessorTests extends TestCase {

    /**
     * The test groups identifier.
     */
    private static final String GROUPNAME = "testGroup";

    /**
     * The processor to test.
     */
    private CreateGroupProcessor proc;

    /**
     * The SAMLEngine used.
     */
    private KeyserverSAMLEngine engine;

    /**
     * Some test message.
     */
    private CreateGroup content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        proc = new CreateGroupProcessor();
        try {
            engine = KeyserverSAMLEngine.getInstance();
            content = SAMLEngine.getXMLObject(CreateGroup.class);
            content.setGroupName(engine.getXSElementGenerator().buildXSString(
                GroupName.DEFAULT_ELEMENT_NAME));
            content.getGroupName().setValue(GROUPNAME);
            content.setGroupOwnerID(engine.getXSElementGenerator().
                    buildXSBase64Binary(GroupOwnerID.DEFAULT_ELEMENT_NAME));
            content.getGroupOwnerID().setValue(
                    TestKeyProvider.getInstance().getUserIDBase64());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Delete references.
     */
    @Override
    public void tearDown() {
        proc = null;
        engine = null;
        content = null;
    }

    /**
     * Tests creating a group.
     */
    public void testCorrectCreateGroupRequest() {
        try {
            AbstractUserManagementFactory.createDefault().getGroupInfo(
                    TestKeyProvider.getInstance().getUserID(), GROUPNAME);
            fail("Should have thrown a PermissionException because the group "
                    + "should not exist");
        } catch (UserNotFoundException e) {
            fail("Test user does not exist in backend");
        } catch (PermissionException e) {
            assertNotNull(e); //All good, indicates that the group doesn't exist
        }

        try {
            proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }

        try {
            AbstractUserManagementFactory.createDefault().getGroupInfo(
                    TestKeyProvider.getInstance().getUserID(), GROUPNAME);
        } catch (UserNotFoundException e) {
            fail("Creating a group deleted the test user?!");
        } catch (PermissionException e) {
            fail("No permission to create group");
        } finally {
            // remove test group created by test
            try {
                AbstractUserManagementFactory.createDefault().deleteGroup(
                        TestKeyProvider.getInstance().getUserID(), GROUPNAME);
            } catch (PermissionException e) {
                fail(e.getMessage());
            }
        }
    }

    /**
     * Tests that client A cannot create a group with client B as owner.
     */
    public void testWrongGroupOwner() {
        try {
            proc.process(content, new byte[] {0x00, 0x01, 0x02}, "testRequest");
            fail("Should have failed with a BackendProcessException");
        } catch (BackendProcessException e) {
            assertTrue(e.getMessage().contains("A new group's owner has to be "
                    + "the client who issued the request to create "
                    + "the new group"));
        }
    }
}
