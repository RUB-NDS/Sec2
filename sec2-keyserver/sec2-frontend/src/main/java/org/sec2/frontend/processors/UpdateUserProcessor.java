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

import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.saml.xml.UpdateUser;
import org.sec2.saml.xml.UserInfo;

/**
 * A processor for UpdateUser requests.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 28, 2012
 */
class UpdateUserProcessor extends
        AbstractUserInfoRespondingProcessor<UpdateUser> {

    /** {@inheritDoc} */
    @Override
    public final UserInfo process(final UpdateUser sec2message,
            final byte[] clientID, final String requestID)
            throws BackendProcessException {
        throw new UnsupportedOperationException();
    }
}
