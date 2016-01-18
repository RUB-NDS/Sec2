/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class RestException extends Exception {

    /**
     * Creates a new instance of
     * <code>RestException</code> without detail message.
     */
    public RestException() {
    }

    /**
     * Constructs an instance of
     * <code>RestException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RestException(String msg) {
        super(msg);
    }

    public RestException(Throwable cause) {
        super(cause);
    }

    public RestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    
}
