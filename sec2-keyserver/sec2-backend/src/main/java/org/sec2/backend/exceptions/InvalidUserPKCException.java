package org.sec2.backend.exceptions;

import java.security.cert.CertificateException;

/**
 * 
 * @author Utimaco Safeware
 *
 */
public class InvalidUserPKCException extends Exception {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 8048158201746634951L;
    
    /**
     * Detailed message for this exception
     */
    private String message;
    
    /**
     *   
     * @param e
     */
    public InvalidUserPKCException(CertificateException e) {
        this.initCause(e);
    }

    /**
     * Default constructor
     */
    public InvalidUserPKCException() {}

    /**
     * 
     * @param message
     */
    public InvalidUserPKCException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }

}
