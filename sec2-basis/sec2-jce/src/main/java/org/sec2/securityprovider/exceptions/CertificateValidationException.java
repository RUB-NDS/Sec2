/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.securityprovider.exceptions;

import java.security.KeyStoreException;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class CertificateValidationException extends KeyStoreException{
    public CertificateValidationException(final String message){
        super(message);
      
    }
}
