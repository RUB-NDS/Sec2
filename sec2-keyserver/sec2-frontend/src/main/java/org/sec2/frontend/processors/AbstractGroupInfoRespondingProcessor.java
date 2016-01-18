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

import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.encryption.CipherValue;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.encryption.EncryptionMethod;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.signature.DigestMethod;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IGroupInfo;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.xml.GroupInfo;
import org.sec2.saml.xml.GroupKey;
import org.sec2.saml.xml.GroupName;
import org.sec2.saml.xml.GroupOwnerID;
import org.sec2.saml.xml.Sec2RequestMessage;

/**
 * Abstract prototype of a processor that returns a GroupInfo object.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @param <T> The type of Sec2RequestMessage to process
 * @version 0.1
 *
 * July 26, 2013
 */
abstract class AbstractGroupInfoRespondingProcessor<T extends
        Sec2RequestMessage> extends AbstractSec2MessageProcessor<T, GroupInfo> {

    /**
     * Creates an XML GroupInfo from a backend IGroupInfo object.
     *
     * @param backendObject The backend IGroupInfo
     * @return an XML GroupInfo
     */
    protected final GroupInfo createGroupInfo(final IGroupInfo backendObject) {
        // create response payload
        GroupInfo payload = SAMLEngine.getXMLBuilder(GroupInfo.class,
                GroupInfo.DEFAULT_ELEMENT_NAME).buildObject(
                GroupInfo.DEFAULT_ELEMENT_NAME);

        // set group name
        payload.setGroupName(this.getXsGenerator().buildXSString(
                GroupName.DEFAULT_ELEMENT_NAME));
        payload.getGroupName().setValue(backendObject.getGroupName());

        // set group owner id
        payload.setGroupOwnerID(this.getXsGenerator().buildXSBase64Binary(
                GroupOwnerID.DEFAULT_ELEMENT_NAME));
        payload.getGroupOwnerID().setValue(Base64.encodeBytes(
                backendObject.getOperator().getId()));

        //set group key
        payload.setGroupKey(SAMLEngine.getXMLBuilder(KeyInfo.class,
                KeyInfo.DEFAULT_ELEMENT_NAME).buildObject(
                GroupKey.DEFAULT_ELEMENT_NAME));
        payload.getGroupKey().getEncryptedKeys().add(SAMLEngine.getXMLBuilder(
                EncryptedKey.class, EncryptedKey.DEFAULT_ELEMENT_NAME).
                buildObject(EncryptedKey.DEFAULT_ELEMENT_NAME));
        EncryptedKey encKey = payload.getGroupKey().getEncryptedKeys().get(0);

        // set encryption method
        encKey.setEncryptionMethod(SAMLEngine.getXMLBuilder(
                EncryptionMethod.class, EncryptionMethod.DEFAULT_ELEMENT_NAME).
                buildObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));
        encKey.getEncryptionMethod().setAlgorithm(
                KeyserverFrontendConfig.XML_ENCRYPTION_KEYTRANSPORT_NS);
        DigestMethod digestMethod = SAMLEngine.getXMLBuilder(DigestMethod.class,
                DigestMethod.DEFAULT_ELEMENT_NAME).buildObject(
                DigestMethod.DEFAULT_ELEMENT_NAME);
        // using SHA-1 here is mandatory according to xml encryption standard
        digestMethod.setAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
        encKey.getEncryptionMethod().getUnknownXMLObjects().add(digestMethod);

        // set cipher data and cipher value
        encKey.setCipherData(SAMLEngine.getXMLBuilder(CipherData.class,
                CipherData.DEFAULT_ELEMENT_NAME).buildObject(
                CipherData.DEFAULT_ELEMENT_NAME));
        encKey.getCipherData().setCipherValue(SAMLEngine.getXMLBuilder(
                CipherValue.class, CipherValue.DEFAULT_ELEMENT_NAME).
                buildObject(CipherValue.DEFAULT_ELEMENT_NAME));
        encKey.getCipherData().getCipherValue().setValue(Base64.encodeBytes(
                backendObject.getWrappedKey()));

        // saves performance if logging is disabled
        if (this.getLogger().isTraceEnabled()) {
            try {
                this.getLogger().trace("GroupInfo content: {}",
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
