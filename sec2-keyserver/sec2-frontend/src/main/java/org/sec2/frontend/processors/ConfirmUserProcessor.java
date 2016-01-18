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

import org.opensaml.xml.util.Base64;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.frontend.samlengine.KeyserverSAMLEngine;
import org.sec2.frontend.samlengine.KeyserverSignatureEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.exceptions.SignatureEngineException;
import org.sec2.saml.xml.ConfirmUser;
import org.sec2.saml.xml.GroupInfo;

/**
 * A processor for ConfirmUser requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 26, 2013
 */
class ConfirmUserProcessor
            extends AbstractGroupInfoRespondingProcessor<ConfirmUser> {
    /**
     * The engine used to validate the signature.
     */
    private KeyserverSignatureEngine engine;

    /**
     * Constructor.
     */
    ConfirmUserProcessor() {
        try {
            engine = KeyserverSAMLEngine.getInstance().getSignatureEngine();
        } catch (SAMLEngineException e) {
            this.getLogger().error("KeyserverSAMLEngine could not be "
                    + "instantiated. Please file a bug report, "
                    + "this error should be impossible", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final GroupInfo process(final ConfirmUser sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        // check the signature
        try {
            engine.validate(sec2message);
        } catch (SignatureEngineException e) {
            e.log();
            throw new BackendProcessException(e,
                    BackendProcessException.Impact.PROCESSING_ERROR,
                    Base64.encodeBytes(clientID), requestID);
        }

        // check the challenge
        //BackendHolder.getBackend().confirmEmail()
        throw new UnsupportedOperationException();
    }
}
