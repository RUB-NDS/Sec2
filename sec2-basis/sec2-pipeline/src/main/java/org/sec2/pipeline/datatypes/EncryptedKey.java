/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 * 
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.pipeline.datatypes;

import org.sec2.extern.org.apache.commons.codec.binary.Base64;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;

/**
 * Class representing the EncryptedKey 
 * 
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date    Aug 20, 2012
 * @version 0.1
 *
 */
public class EncryptedKey {
    
    private String keyId;
    
    private String cipherValue;
    
    private String groupKeyId;
    
    /**
     * A default constructor 
     */
    public EncryptedKey() {
    }
    
    public EncryptedKey(DocumentKey dk) {
        this.keyId = dk.getKeyId();
        String base64Key = Base64.encodeBase64String(dk.getKey().getBytes());
        this.cipherValue = base64Key;
        this.groupKeyId = keyId.split(ExtendedDocumentKey.GROUP_SEPARATOR)[0];
    }
    
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyName) {
        this.keyId = keyName;
    }

    public String getCipherValue() {
        return cipherValue;
    }

    public void setCipherValue(String cipherValue) {
        this.cipherValue = cipherValue;
    }

    public String getGroupKeyId() {
        return groupKeyId;
    }

    public void setGroupKeyId(String groupKeyId) {
        this.groupKeyId = groupKeyId;
    }
}
