/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 * 
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited. For details on "Sec2" and its contributors visit
 * 
 * http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.backend;

import java.security.cert.X509Certificate;

/**
 * Insert description here.
 * 
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 * 
 *          January 11, 2013
 */
public interface IServerInfo {

    /**
     * @return The keyserver's URL
     */
    public String getEndpointURL();

    /**
     * @return The server's signing certificate
     */
    public X509Certificate getSignaturePKC();

    /**
     * @return The server's encryption certificate
     */
    public X509Certificate getEncryptionPKC();
}
