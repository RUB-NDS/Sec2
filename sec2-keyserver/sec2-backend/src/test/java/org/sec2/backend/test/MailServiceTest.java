package org.sec2.backend.test;


import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.MailService;

import junit.framework.TestCase;

public class MailServiceTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    
    public void testSendMail() {
        MailService ms = new MailService(ConfigurationFactory.createDefault());
        ms.send("sec2.keyserver@googlemail.com", "Geheime Nachricht", "ARGH", null);
    }
}
