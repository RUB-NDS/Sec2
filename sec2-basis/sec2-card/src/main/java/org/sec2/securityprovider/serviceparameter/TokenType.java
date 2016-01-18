/*
 * Copyright 2012 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */
package org.sec2.securityprovider.serviceparameter;

/**
 *
 * @author Jan Temme 09.08.2012
 */
public enum TokenType implements IServiceParameter {

    /**
     * HARDWARE_TOKEN aka Java Smartcard
     */
    HARDWARE_TOKEN,
    
    /**
     * SOFTWARE_TOKEN aka Java Smartcard Simulator
     */
    SOFTWARE_TOKEN,
    
    /** Test User from STatic TestData
     * 
     */
    SOFTWARE_TEST_TOKEN_USER_1,
    
    SOFTWARE_TEST_TOKEN_USER_2,
    
    UNSPECIFIED;
    
    public static final String TOKEN_TYPE_IDENTIFIER = "TokenType";
}