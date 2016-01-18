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
package org.sec2.managers.factories;

import org.sec2.managers.IDocumentKeyManager;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.managers.impl.DocumentKeyManagerImpl;
import org.sec2.managers.impl.DummyDocumentKeyManagerImpl;

/**
 * KeyManagerFactory for maintaining GroupKey and DocumentKey managers
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class KeyManagerFactory {

    /**
     * This class cannot be instantiated
     */
    private KeyManagerFactory() {
        
    }
    
    /**
     * 
     * @return instance of a DocumentKeyManager
     */
    public static IDocumentKeyManager getDocumentKeyManager() throws 
            KeyManagerException {
        return DocumentKeyManagerImpl.getInstance();
//        return DummyDocumentKeyManagerImpl.getInstance();
    }
}
