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
package org.sec2.managers.exceptions;

/**
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class KeyManagerException extends Exception {

    public KeyManagerException(String message) {
        super(message);
    }
    
    public KeyManagerException(Exception e) {
        super(e);
    }
    
    public KeyManagerException(String message, Exception e) {
        super(message, e);
    }
}
