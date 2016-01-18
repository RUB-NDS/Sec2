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
 * Tests for the endpoint servlet of the keyserver.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 17, 2012
 */
public class EndpointServletTests extends AbstractServletTests {

    /**
     * Tests that the servlet denies the usage of the GET method.
     */
    public void testGET() {
        getRequest().setMethod("GET");
        try {
            getResponse().parse(getTester().getResponses(
                    getRequest().generate()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED,
                getResponse().getStatus());
        assertFalse(getResponse().getContent().contains("<samlp:Response"));
    }

    /**
     * Tests that the servlet responds with an error when the POST Body
     * is empty.
     */
    public void testEmptyBody() {
        getRequest().setContent("");
        try {
            getResponse().parse(getTester().getResponses(
                    getRequest().generate()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals(HttpStatus.SC_BAD_REQUEST, getResponse().getStatus());
        assertTrue(getResponse().getContent().contains("<saml2p:Response"));
        assertTrue(getResponse().getContent().contains("<saml2p:StatusCode "
                + "Value=\"urn:oasis:names:tc:SAML:2.0:status:"
                + "Requester\""));
    }

}
