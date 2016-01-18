/*
 * Copyright 2013 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.securityprovider.exceptions;

import java.security.PublicKey;
import java.security.cert.Certificate;
import org.sec2.logging.LogLevel;

/**
 *  Indicates that a key signature does not match the certificate.
 *  As the key is already in the system, the certificate must be changed.
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de> 
 * @date 08.01.2013
*/
public class IllegalPostInstantinationModificationException extends SecurityProviderException{
  /**
     * Default message for the exception.
     */
    public static final String DEFAULT_MESSAGE = 
            "Attemp of Variable modification that is only "
            + "allowed before instantination ";
    /**
     * Default log level for the exception.
     */
    public static final LogLevel DEFAULT_LOGLEVEL = LogLevel.PROBLEM;

    /**
     * Constructor for a new, wrapped/unwrapped exception.
     *
     * @param message Reason for this exception or
     * <code>null</code> if the default message should be used.
     * @param exception Wrapped exception which caused the problem, if any or
     * <code>null</code> if there is no exception to wrap.
     * @param loglevel Log level for the generated message or
     * <code>null</code> if this issue should not be logged.
     */
    public IllegalPostInstantinationModificationException(String message) {
                     super(message, null, DEFAULT_LOGLEVEL);
       
        
    /*    
        super(DEFAULT_MESSAGE, exception, DEFAULT_LOGLEVEL);
        if (message != null) {
            this.setMessage(message);
        }
        if (loglevel != null) {
            this.setLogLevel(loglevel);
        } */
    }
    
    public IllegalPostInstantinationModificationException(){
        super(DEFAULT_MESSAGE, null, DEFAULT_LOGLEVEL);
    }

    
}
