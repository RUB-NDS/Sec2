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

import java.util.List;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IUserInfo;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.xml.Sec2RequestMessage;
import org.sec2.saml.xml.UserID;
import org.sec2.saml.xml.UserList;

/**
 * Abstract prototype of a processor that returns a UserList object.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @param <T> The type of Sec2RequestMessage to process
 * @version 0.1
 *
 * July 26, 2013
 */
abstract class AbstractUserListRespondingProcessor<T extends
        Sec2RequestMessage> extends AbstractSec2MessageProcessor<T, UserList> {

    /**
     * Creates an XML UserList from a List&lt;IUserInfo&gt; object.
     *
     * @param backendObject The List&lt;IUserInfo&gt;
     * @return an XML UserList
     */
    protected final UserList createUserList(
            final List<IUserInfo> backendObject) {
        // create response payload
        UserList payload = SAMLEngine.getXMLBuilder(UserList.class,
                UserList.DEFAULT_ELEMENT_NAME).buildObject(
                UserList.DEFAULT_ELEMENT_NAME);

        // set user IDs
        for (IUserInfo user : backendObject) {
            XSBase64Binary userXML = this.getXsGenerator().buildXSBase64Binary(
                UserID.DEFAULT_ELEMENT_NAME);
            userXML.setValue(Base64.encodeBytes(user.getId()));
            payload.getUserIDs().add(userXML);
        }

        // saves performance if logging is disabled
        if (this.getLogger().isTraceEnabled()) {
            try {
                this.getLogger().trace("UserList content: {}",
                        XMLHelper.getXMLString(payload));
            } catch (MarshallingException e) {
                // Can't fix this here. But the exception will occur again when
                // the payload is encrypted und will be handled there
                this.getLogger().error(
                        "Generated response payload could not be marshalled");
            }
        }
        return payload;
    }
}
