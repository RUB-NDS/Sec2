/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.frontend.processors;

import java.security.cert.CertificateEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;
import org.owasp.esapi.codecs.Base64;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.AbstractUserManagementFactory;
import org.sec2.backend.impl.ConfigurationFactory;
import org.sec2.backend.impl.DatabaseServer;
import org.sec2.frontend.exceptions.BackendProcessException;
import org.sec2.frontend.samlengine.KeyserverSAMLEngine;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.EncryptionCertificate;
import org.sec2.saml.xml.RegisterUser;
import org.sec2.saml.xml.SignatureCertificate;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Tests for RegisterUserProcessor.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 25, 2013
 */
public class RegisterUserProcessorTests extends TestCase {

    /**
     * The processor to test.
     */
    private RegisterUserProcessor proc;

    /**
     * The SAMLEngine used.
     */
    private KeyserverSAMLEngine engine;

    /**
     * Some test message.
     */
    private RegisterUser content;

    /**
     * Create processor and SAMLEngine.
     */
    @Override
    public void setUp() {
        this.tearDown(); //makes sure, that user3 does not exist in the DB
        proc = new RegisterUserProcessor();
        try {
            engine = KeyserverSAMLEngine.getInstance();
            content = SAMLEngine.getXMLObject(RegisterUser.class);
            content.setSignatureCertificate(engine.getXSElementGenerator().
                    buildXSBase64Binary(
                    SignatureCertificate.DEFAULT_ELEMENT_NAME));
            content.getSignatureCertificate().setValue(Base64.encodeBytes(
                TestKeyProvider.getInstance().getUser3SignCert().getEncoded()));
            content.setEncryptionCertificate(engine.getXSElementGenerator().
                    buildXSBase64Binary(
                    EncryptionCertificate.DEFAULT_ELEMENT_NAME));
            content.getEncryptionCertificate().setValue(Base64.encodeBytes(
                TestKeyProvider.getInstance().getUser3EncCert().getEncoded()));
        } catch (CertificateEncodingException e) {
            fail(e.getMessage());
        } catch (SAMLEngineException e) {
            e.log();
            fail(e.toString());
        }
    }

    /**
     * Test a wrong clientID.
     */
    public void testWrongClientID() {
        try {
            proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
            fail("Should have thrown a BackendProcessException because the "
                    + "IDs of user1 and user3 do not match");
        } catch (BackendProcessException e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests registering a new user.
     */
    public void testCorrectRegisterUserRequest() {
        try {
            proc.process(content,
                    TestKeyProvider.getInstance().getUser3ID(), "testRequest");
        } catch (BackendProcessException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests registering a user that already exists.
     */
    public void testDuplicateUser() {
        try {
            content.getSignatureCertificate().setValue(Base64.encodeBytes(
                TestKeyProvider.getInstance().getUserSignCert().getEncoded()));
            content.getEncryptionCertificate().setValue(Base64.encodeBytes(
                TestKeyProvider.getInstance().getUserEncCert().getEncoded()));
        } catch (CertificateEncodingException  e) {
            fail(e.getMessage());
        }

        try {
            proc.process(content,
                    TestKeyProvider.getInstance().getUserID(), "testRequest");
            fail("Should have thrown a UserAlreadyExistsException because "
                    + "user1 already exists");
        } catch (BackendProcessException e) {
            if (!(e.getCause() instanceof UserAlreadyExistsException)) {
                fail("Should have thrown a UserAlreadyExistsException because "
                    + "user1 already exists");
            }
        }
    }

    /**
     * Delete references and the new user.
     */
    @Override
    public void tearDown() {
        proc = null;
        engine = null;
        content = null;
        try {
            //Test if user3 exists, will execute the catch block if not
            AbstractUserManagementFactory.createDefault().getUserInfo(
                    TestKeyProvider.getInstance().getUser3ID());

            Connection db = new DatabaseServer(
                    ConfigurationFactory.createDefault()).createConnection();
            PreparedStatement query = null;
            try {
                //Delete all groups user3 created
                query = db.prepareStatement(
                        "DELETE groups, group_members FROM groups INNER JOIN "
                        + "group_members ON groups.id = group_members.group_ref"
                        + " WHERE operator IN "
                        + "(SELECT email FROM users WHERE pk_hash = ?);");
                query.setBytes(1, TestKeyProvider.getInstance().getUser3ID());
                query.execute();
                query.close();
                //Delete user3
                query = db.prepareStatement(
                        "DELETE FROM users WHERE pk_hash = ?;");
                query.setBytes(1, TestKeyProvider.getInstance().getUser3ID());
                query.execute();
                query.close();
            } catch (SQLException e) {
                fail(e.getMessage());
            } finally {
                try {
                    query.close();
                    db.close();
                } catch (SQLException e) {
                    fail(e.getMessage());
                }
            }
        } catch (UserNotFoundException e) {
            assertNotNull(e);
        }
    }
}
