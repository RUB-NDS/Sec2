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

import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.frontend.samlengine.KeyserverSAMLEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.AddUsersToGroup;
import org.sec2.saml.xml.GroupName;
import org.sec2.saml.xml.UserID;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for AddUsersToGroupProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 13, 2013
 */
public class AddUsersToGroupProcessorTests extends AbstractGroupRequiringTests {

    /**
     * The processor to test.
     */
    private AddUsersToGroupProcessor proc;

    /**
     * The SAMLEngine used.
     */
    private KeyserverSAMLEngine engine;

    /**
     * Some test message.
     */
    private AddUsersToGroup content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        super.setUp();
        proc = new AddUsersToGroupProcessor();
        try {
            engine = KeyserverSAMLEngine.getInstance();
            content = SAMLEngine.getXMLObject(AddUsersToGroup.class);
            content.setGroupName(engine.getXSElementGenerator().buildXSString(
                GroupName.DEFAULT_ELEMENT_NAME));
            content.getGroupName().setValue(GROUPNAME);
            content.getUsers().add(engine.getXSElementGenerator().
                    buildXSBase64Binary(UserID.DEFAULT_ELEMENT_NAME));
            content.getUsers().get(0).setValue(
                    TestKeyProvider.getInstance().getUser2IDBase64());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Tests adding a user to a group.
     */
    public void testCorrectAddUsersToGroupRequest() {
        try {
            assertEquals(1, AbstractUserManagementFactory.createDefault().
                    getGroupInfo(TestKeyProvider.getInstance().getUserID(),
                    GROUPNAME).getMembers().size());
        } catch (UserNotFoundException e) {
            fail("Test user does not exist in backend");
        } catch (PermissionException e) {
            fail("Test group is not accessible");
        }

        try {
            proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }

        try {
            assertEquals(2, AbstractUserManagementFactory.createDefault().
                    getGroupInfo(TestKeyProvider.getInstance().getUserID(),
                    GROUPNAME).getMembers().size());
        } catch (UserNotFoundException e) {
            fail("Test user does not exist in backend");
        } catch (PermissionException e) {
            fail("Test group is not accessible");
        } // removing user from group is not necessary since tearDown() deletes
          // the whole group
    }

    /**
     * Delete references.
     */
    @Override
    public void tearDown() {
        super.tearDown();
        proc = null;
        engine = null;
        content = null;
    }
}
