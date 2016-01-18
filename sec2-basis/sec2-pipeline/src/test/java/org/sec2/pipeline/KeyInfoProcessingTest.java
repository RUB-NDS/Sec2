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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.sec2.extern.org.apache.commons.codec.binary.Base64;
import org.custommonkey.xmlunit.XMLUnit;
import org.sec2.pipeline.handlers.KeyInfoProcessingHandler;
import org.sec2.pipeline.handlers.SerializationHandler;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyInfo parsing tests
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date Aug 20, 2012
 * @version 0.1
 *
 */
public class KeyInfoProcessingTest extends TestCase {

    /**
     * SLF4J Logger.
     *
     */
    Logger log = LoggerFactory.getLogger(KeyInfoProcessingTest.class);
    /**
     * Group Key Name
     */
    private final static String GROUP_KEY_ID_1 = "GK1";
    /**
     * Group Key Name
     */
    private final static String GROUP_KEY_ID_2 = "GK2";
    /**
     * Cipher Value
     */
    private final static String CIPHER_VALUE =
            Base64.encodeBase64String("some bytes".getBytes());
    /**
     * Dokument Key Name
     */
    private final static String DOCUMENT_KEY_ID_1 = "GK1;nonce124";
    /**
     * Dokument Key Name
     */
    private final static String DOCUMENT_KEY_ID_2 = "GK1;GK2;nonce124";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public KeyInfoProcessingTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(KeyInfoProcessingTest.class);
    }

    /**
     * Test the KeyInfo element parsing with the KeyInfoProcessingHandler
     *
     * @throws Exception
     */
    public void testEncryptedKey1() throws Exception {
        log.debug("testing the KeyInfoProcessingHandler");
        String encryptedKey = getEncryptedKey(GROUP_KEY_ID_1, CIPHER_VALUE);
        String[] keys = {encryptedKey};
        String keyInfo = getKeyInfo(DOCUMENT_KEY_ID_1, keys);
        StringReader sr = new StringReader(keyInfo);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        XMLProcessor p = new XMLProcessor(sr, pw) {
            @Override
            void createHandlerChain() {
                KeyInfoProcessingHandler kiph = new KeyInfoProcessingHandler();
                SerializationHandler sh = new SerializationHandler(eventWriter);
                firstHandler = kiph;
                kiph.insertNextHandler(sh);
            }
        };

        p.createHandlerChain();
        p.processXMLStream();

        KeyInfoProcessingHandler kiph = ((KeyInfoProcessingHandler) p.firstHandler);
        List<DocumentKey> eks = kiph.getEncryptedDocumentKeys();
        assertEquals(1, eks.size());

        assertEquals(DOCUMENT_KEY_ID_1, kiph.getDocumentKeyName());

        String cipherValue = Base64.encodeBase64String(
                kiph.getEncryptedDocumentKeys().get(0).getKey().getBytes());
        assertEquals(CIPHER_VALUE, cipherValue);

        assertEquals(GROUP_KEY_ID_1 + ExtendedDocumentKey.GROUP_SEPARATOR
                + DOCUMENT_KEY_ID_1, kiph.getEncryptedDocumentKeys().
                get(0).getKeyId());

        assertEquals(17, kiph.getEventCacheList().size());


        // compare the KeyInfoProcessingHandler output XML with the original XML
        List<DocumentKey> documentKeys = kiph.getEncryptedDocumentKeys();
        ExtendedDocumentKey edk = new ExtendedDocumentKey(documentKeys);
        byte[] testKey = {0, 0, 0, 0, 0, 0};
        DocumentKey testDK = new DocumentKey(testKey, false, DOCUMENT_KEY_ID_1);
        edk.setDecryptedDocumentKey(testDK);

        kiph.generateKeyInfoEvents(edk);
        pw.flush();
        
        assertTrue(XMLUnit.compareXML(keyInfo, new String(baos.toByteArray())).identical());
    }

    /**
     * Test the KeyInfo element parsing with the KeyInfoProcessingHandler
     *
     * @throws Exception
     */
    public void testEncryptedKey2() throws Exception {
        log.debug("testing the KeyInfoProcessingHandler with two EncryptedKey elements");
        String encryptedKey1 = getEncryptedKey(GROUP_KEY_ID_1, CIPHER_VALUE);
        String encryptedKey2 = getEncryptedKey(GROUP_KEY_ID_2, CIPHER_VALUE);
        String[] keys = {encryptedKey1, encryptedKey2};
        String keyInfo = getKeyInfo(DOCUMENT_KEY_ID_2, keys);
        StringReader sr = new StringReader(keyInfo);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        XMLProcessor p = new XMLProcessor(sr, pw) {
            @Override
            void createHandlerChain() {
                KeyInfoProcessingHandler kiph = new KeyInfoProcessingHandler();
                SerializationHandler sh = new SerializationHandler(eventWriter);
                firstHandler = kiph;
                kiph.insertNextHandler(sh);
            }
        };

        p.createHandlerChain();
        p.processXMLStream();

        KeyInfoProcessingHandler kiph = ((KeyInfoProcessingHandler) p.firstHandler);
        List<DocumentKey> eks = kiph.getEncryptedDocumentKeys();
        assertEquals(2, eks.size());

        assertEquals(DOCUMENT_KEY_ID_2, kiph.getDocumentKeyName());

        String cipherValue = Base64.encodeBase64String(
                kiph.getEncryptedDocumentKeys().get(1).getKey().getBytes());
        assertEquals(CIPHER_VALUE, cipherValue);

        assertEquals(GROUP_KEY_ID_1 + ExtendedDocumentKey.GROUP_SEPARATOR
                + DOCUMENT_KEY_ID_2, kiph.getEncryptedDocumentKeys().
                get(0).getKeyId());


        // compare the KeyInfoProcessingHandler output XML with the original XML
        List<DocumentKey> documentKeys = kiph.getEncryptedDocumentKeys();
        ExtendedDocumentKey edk = new ExtendedDocumentKey(documentKeys);
        byte[] testKey = {0, 0, 0, 0, 0, 0};
        DocumentKey testDK = new DocumentKey(testKey, false, DOCUMENT_KEY_ID_2);
        edk.setDecryptedDocumentKey(testDK);

        kiph.generateKeyInfoEvents(edk);
        pw.flush();
        
        assertTrue(XMLUnit.compareXML(keyInfo, new String(baos.toByteArray())).identical());
    }

    /**
     * Construct EncryptedKey element
     *
     * @param groupKey
     * @param cipherValue
     * @return
     */
    public static final String getEncryptedKey(String groupKey,
            String cipherValue) {

        return "<xenc11:EncryptedKey xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\" >"
                + "<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">"
                + "<ds:KeyName>" + groupKey + "</ds:KeyName>"
                + "</ds:KeyInfo>"
                + "<xenc11:CipherData>"
                + "<xenc11:CipherValue>" + cipherValue + "</xenc11:CipherValue>"
                + "</xenc11:CipherData>"
                + "</xenc11:EncryptedKey>";
    }

    /**
     * Construct KeyInfo
     *
     * @param documentKeyName
     * @param encryptedKeys
     * @return
     */
    public static final String getKeyInfo(String documentKeyName,
            String[] encryptedKeys) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">");
        sb.append("<ds:KeyName>").append(documentKeyName).append("</ds:KeyName>");
        for (String ek : encryptedKeys) {
            sb.append(ek);
        }
        sb.append("</ds:KeyInfo>");
        return sb.toString();
    }
}
