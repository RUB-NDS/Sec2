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
 * Tests the responses of the servlet to invalid xml or non-SAML requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * Oktober 20, 2012
 */
public class XMLProcessingServletTests extends AbstractServletTests {

    /**
     * Tests that the server denies processing invalid XML.
     */
    public void testInvalidXML() {
        getRequest().setContent("<xml></xlm>");
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
                + "Requester"));
    }

    /**
     * Tests that the server denies processing non-SAML requests.
     */
    public void testNonSAML() {
        getRequest().setContent("<xml></xml>");
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
                + "Requester"));
    }

    /**
     * Tests that the server accepts only SAML-AttributeQueries as request.
     */
    public void testNonAttributeQuery() {
        getRequest().setContent("<saml2p:Response xmlns:saml2p=\""
                + "urn:oasis:names:tc:SAML:2.0:protocol\"></saml2p:Response>");
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
                + "Requester"));
    }
}
