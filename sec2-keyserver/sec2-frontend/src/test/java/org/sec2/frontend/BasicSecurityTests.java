/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.frontend;

import org.apache.commons.httpclient.HttpStatus;

/**
 * Tests the responses of the servlet to security related requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 16, 2012
 */
public class BasicSecurityTests extends AbstractServletTests {

    /**
     * Tests that the keyserver denies a message without a signature.
     */
    public void testNoSignature() {
        getRequest().setContent(this.getTestXML("noSignature.xml"));
        try {
            getResponse().parse(getTester().getResponses(
                    getRequest().generate()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(HttpStatus.SC_FORBIDDEN, getResponse().getStatus());
        assertTrue(getResponse().getContent().contains("<saml2p:Response"));
        assertTrue(getResponse().getContent().contains("<saml2p:StatusCode "
                + "Value=\"urn:oasis:names:tc:SAML:2.0:status:"
                + "Requester"));
    }

    /**
     * Tests that the keyserver processes a request only once.
     */
    public void testReplayAttack() {
        getRequest().setContent(this.getTestXML("correctRequest.xml"));
        // first request works
        try {
            getResponse().parse(getTester().getResponses(
                    getRequest().generate()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(HttpStatus.SC_OK, getResponse().getStatus());
        assertTrue(getResponse().getContent().contains("<saml2p:Response"));
        assertTrue(getResponse().getContent().contains("<saml2p:StatusCode "
                + "Value=\"urn:oasis:names:tc:SAML:2.0:status:"
                + "Success"));

        // second request fails
        try {
            getResponse().parse(getTester().getResponses(
                    getRequest().generate()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(HttpStatus.SC_FORBIDDEN, getResponse().getStatus());
        assertTrue(getResponse().getContent().contains("<saml2p:Response"));
        assertTrue(getResponse().getContent().contains("<saml2p:StatusCode "
                + "Value=\"urn:oasis:names:tc:SAML:2.0:status:"
                + "Requester"));
    }
}
