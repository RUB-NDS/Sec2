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
package org.sec2.frontend.processors;

import org.sec2.saml.xml.Sec2RequestMessage;

/**
 * A container that encapsulates a Sec2RequestMessage,
 * the corresponding client's ID and the request's ID.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * March 12, 2012
 */
public final class BackendJob {

    /**
     * The request of the client.
     */
    private Sec2RequestMessage sec2Object;

    /**
     * The client's ID.
     */
    private String clientID;

    /**
     * The request's ID.
     */
    private String requestID;

    /**
     * Constructor.
     *
     * @param pSec2Object The request of the client
     * @param pClientID The client's ID
     * @param pRequestID The request's ID
     */
    public BackendJob(final Sec2RequestMessage pSec2Object,
            final String pClientID, final String pRequestID) {
        if (pSec2Object == null) {
            throw new IllegalArgumentException(
                    "Parameter pSec2Object must not be null");
        }
        if (pClientID == null) {
            throw new IllegalArgumentException(
                    "Parameter pClientID must not be null");
        }
        if (pRequestID == null) {
            throw new IllegalArgumentException(
                    "Parameter pRequestID must not be null");
        }
        this.sec2Object = pSec2Object;
        this.clientID   = pClientID;
        this.requestID  = pRequestID;
    }

    /**
     * @return the sec2Object
     */
    public Sec2RequestMessage getSec2Object() {
        return sec2Object;
    }

    /**
     * @return the clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * @return the requestID
     */
    public String getRequestID() {
        return requestID;
    }

}
