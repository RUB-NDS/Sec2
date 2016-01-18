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
package org.sec2.pipeline;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.custommonkey.xmlunit.XMLUnit;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.core.XMLConstants;
import org.sec2.managers.factories.KeyManagerFactory;
import org.sec2.managers.impl.DocumentKeyManagerImpl;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.sec2.pipeline.handlers.AbstractHandler;
import org.sec2.pipeline.handlers.EncryptedDataDecryptionHandler;
import org.sec2.pipeline.handlers.EncryptedDataEncryptionHandler;
import org.sec2.pipeline.handlers.SerializationHandler;
import org.w3c.dom.Document;

/**
 * Encryption processing tests
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date Aug 20, 2013
 * @version 0.1
 *
 */
public class EncryptionTest extends AbstractEncryptionDecryptionTest {

    private static String UNKNOWN_GROUP = "groupxxx";
    
    /**
     * Create the test case
     *
     */
    public EncryptionTest() throws Exception {
        super();
    }

    public void testEncryptDecryptDocument1() throws Exception {
        String document = getDocument(GROUP111_ID);
        logger.debug("Encryption test using the pipeline");
        String encrypted = encryptDocument(document);
        
        String decrypted = decryptDocument(encrypted);
        assertTrue(XMLUnit.compareXML(document, decrypted).identical());
    }

    public void testEncryptDecryptDocumentWithNewDocID() throws Exception {
        String document = getDocument(GROUP333_ID);
        logger.debug("Encryption test a group333, which forces to create a new"
                + "document key");
        String encrypted = encryptDocument(document);
        String decrypted = decryptDocument(encrypted);
        assertTrue(XMLUnit.compareXML(document, decrypted).identical());
    }

    public void testEncryptDocumentWithUnkownGroup() throws Exception {
        String document = getDocument(UNKNOWN_GROUP);
        logger.debug("Encryption test with an unknown group key leads to a "
                + "ExXMLProcessingException. The cause of this exception is the "
                + "KeyManagerException");
        ExXMLProcessingException ex = null;
        try {
            encryptDocument(document);
        } catch (ExXMLProcessingException e) {
            ex = e;
        }
        assertNotNull(ex);
        assertEquals(KeyManagerException.class, ex.getCause().getClass());
    }

    public void testDecryptDocumentWithUnkownGroup() throws Exception {
        String document = getDocument(GROUP333_ID);
        logger.debug("Decryption test with an unknown group key. EncryptedData will"
                + "not be decrypted");
        String encrypted = encryptDocument(document);
        String encryptedUnknown = encrypted.replace(GROUP333_ID, UNKNOWN_GROUP);

        String decrypted = decryptDocument(encryptedUnknown);
        assertTrue(XMLUnit.compareXML(encryptedUnknown, decrypted).identical());
    }

    public void testEncryptDecryptDoubleEncryptedDocument() throws Exception {
        String document = getDoubledDocument(GROUP222_ID, GROUP111_ID);
        logger.debug("Encryption test with a double encrypted document");
        String encrypted = encryptDocument(document);

        String decrypted = decryptDocument(encrypted);
        assertTrue(XMLUnit.compareXML(document, decrypted).identical());
    }

    public void testEncryptDecryptLongDocument() throws Exception {
        int length = 100000;
        String document = getLongDocument(GROUP111_ID, length);
        logger.debug("Encryption test using a long document, document length: cca."
                + length + " bytes.");
        
        long time = System.currentTimeMillis();
        String encrypted = encryptDocument(document);
        String decrypted = decryptDocument(encrypted);
        System.out.println("Streaming-based Encryption - Decryption lasted: " + (System.currentTimeMillis() - time));
        
        time = System.currentTimeMillis();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(new ByteArrayInputStream(document.getBytes("utf-8")));
        doc.getElementById("1");
        System.out.println("DOM document parsing lasted: " + (System.currentTimeMillis() - time));
        
        assertTrue(XMLUnit.compareXML(document, decrypted).identical());
        
    }

    private String encryptDocument(String document) throws Exception {
        StringReader sr = new StringReader(document);
        StringWriter sw = new StringWriter();

        final EncryptedDataEncryptionHandler eh = new EncryptedDataEncryptionHandler();
        eh.setDocumentKeyManager(dkm);

        XMLProcessor oxp = new XMLProcessor(sr, sw) {
            @Override
            void createHandlerChain() {
                AbstractHandler ah = new AbstractHandler() {
                };
                SerializationHandler sh = new SerializationHandler(eventWriter);
                eh.insertNextHandler(sh);
                ah.insertNextHandler(eh);

                firstHandler = ah;
            }
        };
        oxp.createHandlerChain();
        oxp.processXMLStream();
        sw.flush();
        sw.close();
        sr.close();

        return sw.toString();
    }

    private String decryptDocument(String document) throws Exception {
        StringReader sr = new StringReader(document);
        StringWriter sw = new StringWriter();

        final EncryptedDataDecryptionHandler dh = new EncryptedDataDecryptionHandler();
        dh.setDocumentKeyManager(dkm);

        XMLProcessor oxp = new XMLProcessor(sr, sw) {
            @Override
            void createHandlerChain() {
                AbstractHandler ah = new AbstractHandler() {
                };
                SerializationHandler sh = new SerializationHandler(eventWriter);
                dh.insertNextHandler(sh);
                ah.insertNextHandler(dh);

                firstHandler = ah;
            }
        };
        oxp.createHandlerChain();
        oxp.processXMLStream();
        sw.flush();
        sr.close();
        sw.close();

        return sw.toString();
    }

    @Override
    public void setUp() {
        super.setUp();
    }

    private static String getDocument(String groups) {
        return "<doc xmlns:xx=\"abc\">"
                + "<enc xmlns:sec2=\"" + XMLConstants.SEC2_NS + "\" sec2:groups=\""
                + groups + "\">"
                + "<test><a>test</a></test>"
                + "</enc>"
                + "</doc>";
    }

    private static String getDoubledDocument(String groups1, String groups2) {
        return "<doc xmlns:xx=\"abc\">"
                + "<enc xmlns:sec2=\"" + XMLConstants.SEC2_NS + "\" sec2:groups=\""
                + groups1 + "\">"
                + "<test><a>test</a></test>"
                + "<enc xmlns:sec2=\"" + XMLConstants.SEC2_NS + "\" sec2:groups=\""
                + groups2 + "\">"
                + "<test><a>test</a></test>"
                + "</enc>"
                + "</enc>"
                + "</doc>";
    }

    private static String getLongDocument(String groups, int byteLength) {
        String fill = "<abc>a</abc>";
        StringBuilder document = new StringBuilder(byteLength);
        document.append("<doc>");
        document.append("<enc xmlns:sec2=\"").append(XMLConstants.SEC2_NS).
                append("\" sec2:groups=\"").append(groups).append("\">");

        for (int i = 0; i < byteLength / fill.length(); i++) {
            document.append(fill); 
        }
        document.append("</enc>");
        document.append("</doc>");

        return document.toString();
    }
}
