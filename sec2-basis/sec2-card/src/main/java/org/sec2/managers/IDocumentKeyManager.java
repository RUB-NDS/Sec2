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
package org.sec2.managers;

import java.util.List;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;

/**
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public interface IDocumentKeyManager {
    
    public ExtendedDocumentKey getDecryptedDocumentKeyForGroups(String keyId) throws
            KeyManagerException;
    
    public ExtendedDocumentKey getDecryptedDocumentKey(List<DocumentKey> 
            encryptedDocumentKeys) throws KeyManagerException;
}
