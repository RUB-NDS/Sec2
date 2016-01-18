/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.managers.impl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class TSManager extends TestCase{

    public TSManager(String name) {
        super(name);
    }
    
    public static Test suite() {
        System.out.println("\n==== Starting Manager Tests ====");

        TestSuite suite = new TestSuite();

        
        suite.addTestSuite(DocumentKeyManagerSoftwareToken.class);   
        suite.addTestSuite(DocumentKeyManagerHardwareToken.class);
       return suite;
    }            
            
}
