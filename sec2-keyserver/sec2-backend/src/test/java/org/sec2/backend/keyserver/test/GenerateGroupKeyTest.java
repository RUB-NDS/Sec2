package org.sec2.backend.keyserver.test;

import java.util.Properties;

import junit.framework.TestCase;

import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.backend.impl.KeyServer;
import org.sec2.statictestdata.TestKeyProvider;

public class GenerateGroupKeyTest extends TestCase {

    private KeyServer keyserver;
    private TestKeyProvider keyProvider;
    
    public GenerateGroupKeyTest(String name) {
        super(name);
    }
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.keyProvider = TestKeyProvider.getInstance();
        Properties configuration = ConfigurationFactory.createDefault();
        DatabaseServer databaseServer = new DatabaseServer(configuration);
        this.keyserver = new KeyServer(databaseServer, configuration);
    }





    public void testGenerateGroupKey() {
        System.out.println("begin testGenerateGroupKey:");
        String keyName = "";
        keyserver.generateGroupKey(keyName);
        System.out.println("end testGenerateGroupKey:");
    }


}
