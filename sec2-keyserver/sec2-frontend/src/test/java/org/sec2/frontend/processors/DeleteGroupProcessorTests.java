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
import org.sec2.saml.xml.DeleteGroup;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for DeleteGroupProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * June 11, 2013
 */
public class DeleteGroupProcessorTests extends AbstractGroupRequiringTests {

    /**
     * The processor to test.
     */
    private DeleteGroupProcessor proc;

    /**
     * Some test message.
     */
    private DeleteGroup content;

    /**
     * Create processor.
     */
    @Override
    public void setUp() {
        super.setUp();
        proc = new DeleteGroupProcessor();
        try {
            content = SAMLEngine.getXMLObject(DeleteGroup.class);
            content.setValue(GROUPNAME);
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Delete references. Method does not call 'super.tearDown()'. This is
     * intentional since the super class' method would try to delete the group
     * that was deleted by the test.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(
                    value = "IJU_TEARDOWN_NO_SUPER",
                    justification = "The super class' tearDown method would try"
                        + " to delete the group that was deleted by the test.")
    public void tearDown() {
        proc = null;
        content = null;
    }

    /**
     * Tests deleting a group.
     */
    public void testCorrectDeleteGroupRequest() {
        try {
            proc.process(content, TestKeyProvider.getInstance().getUserID(),
                    "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }
    }
}
