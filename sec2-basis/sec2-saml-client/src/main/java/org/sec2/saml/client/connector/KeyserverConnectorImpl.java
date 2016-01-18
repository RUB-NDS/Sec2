/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.client.connector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.httpclient.HttpStatus;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.Pair;
import org.opensaml.xml.validation.ValidationException;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.client.engine.ClientSAMLEngine;
import org.sec2.saml.client.engine.SecurityProviderConnectorFactory;
import org.sec2.saml.client.exceptions.ClientSecurityException;
import org.sec2.saml.engine.PrefixedIdentifierGenerator;
import org.sec2.saml.engine.SAMLEngine;
import org.sec2.saml.engine.XMLHelper;
import org.sec2.saml.exceptions.CipherEngineException;
import org.sec2.saml.exceptions.KeyserverException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.exceptions.SignatureEngineException;
import org.sec2.saml.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class does the high level work of generating SAML messages. It calls the
 * SAML engine and puts the things together.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.3
 *
 * May 18, 2013
 */
public final class KeyserverConnectorImpl implements IKeyserverConnector {

    /**
     * Class logger.
     */
    private final Logger log =
            LoggerFactory.getLogger(KeyserverConnectorImpl.class);
    /**
     * The SAML engine used for all SAML operations.
     */
    private final ClientSAMLEngine engine;
    /**
     * URL of the keyserver.
     */
    private final String keyserverURL;
    /**
     * Cache of recently used IDs. It would suffice to have only the keys, but
     * since this is a map, store the IssueInstant timestamp wo know when the
     * original request was sent (useful for logging).
     */
    private final Cache<String, DateTime> recentIDs;
    /**
     * Executes network requests.
     */
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    /**
     * Constructor.
     *
     * @param pKeyserverURL URL of the keyserver
     * @throws SAMLEngineException if SAMLEngine instance cannot be retrived
     */
    KeyserverConnectorImpl(final String pKeyserverURL)
            throws SAMLEngineException {
        engine = ClientSAMLEngine.getInstance();
        keyserverURL = pKeyserverURL;
        recentIDs = CacheBuilder.from(
                SAMLClientConfig.RECENT_IDS_CACHE_CONFIG).build();
    }

    /**
     * Retrieves the keyserver's response to a request message. Top level entry
     * point to the keyserver communication.
     *
     * @param content The request message to send to the keyserver
     * @return the keyserver's response
     * @throws SAMLEngineException if something went wrong processing either
     * request or response
     * @throws IOException if the communication with the keyserver failed
     */
    private Sec2ResponseMessage queryResponse(final Sec2RequestMessage content)
            throws SAMLEngineException, IOException {
        if (log.isTraceEnabled()) { // saves performance if logging is disabled
            try {
                log.trace("Unencrypted request content: {}",
                        XMLHelper.getXMLString((XMLObject) content));
            } catch (MarshallingException e) {
                // Can't fix this here. But the exception will occur again when
                // the payload is encrypted und will be handled there
                log.error("Generated request payload could not be marshalled");
            }
        }

        // create request
        Pair<AttributeQuery, Credential> xmlKeyPair =
                this.engine.createAttributeQuery(content);
        String xml;
        try {
            xml = XMLHelper.getXMLString(xmlKeyPair.getFirst());
        } catch (MarshallingException e) {
            throw new SAMLEngineException(e);
        }

        String responseBody = this.queryKeyserver(xml);
        Response response = engine.parseResponse(responseBody);

        // TODO: check if keyserver responded an error and process it

        // check response
        validateResponse(response, xmlKeyPair.getFirst().getID());

        // decrypt the request
        XSAny attributeValue = (XSAny) response.getAssertions().get(0).
                getAttributeStatements().get(0).getAttributes().get(0).
                getAttributeValues().get(0);
        EncryptedAttribute encContent = (EncryptedAttribute) attributeValue.getUnknownXMLObjects().get(0);
        Attribute responseAttribute;
        try {
            responseAttribute = engine.getCipherEngine().
                    decrypt(encContent, xmlKeyPair.getSecond());
        } catch (CipherEngineException e) {
            e.log();
            throw new ClientSecurityException(e);
        }
        if (log.isTraceEnabled()) { // saves performance if logging is disabled
            try {
                log.trace("Decrypted response content: {}",
                        XMLHelper.getXMLString(responseAttribute));
            } catch (MarshallingException e) {
                // Strange, but no real problem...
                log.error("Decrypted response content could not be marshalled");
            }
        }

        // validate content
        try {
            engine.getSec2samlValidator().validate(responseAttribute);
        } catch (ValidationException e) {
            throw new ClientSecurityException(e);
        }

        if (responseAttribute.getAttributeValues().size() != 1) {
            throw new ClientSecurityException("Response does not contain "
                    + "exactly one AttributeValue in its EncryptedAttribute");
        }
        attributeValue = (XSAny) responseAttribute.getAttributeValues().get(0);
        if (attributeValue.getUnknownXMLObjects().size() > 0) {
            // this is the case for all messages except <deleteGroup>
            if (attributeValue.getUnknownXMLObjects().size() != 1) {
                throw new ClientSecurityException("Response does not contain "
                        + "exactly one child in its EncryptedAttribute's "
                        + "AttributeValue");
            }
            if (!(attributeValue.getUnknownXMLObjects().get(0) instanceof Sec2ResponseMessage)) {
                throw new ClientSecurityException("Response does not contain "
                        + "a Sec2 message in its EncryptedAttribute's "
                        + "AttributeValue");
            }
            return (Sec2ResponseMessage) attributeValue.getUnknownXMLObjects().
                    get(0);
        } else {
            // <deleteGroup>
            return null;
        }
    }

    /**
     * Sends a HTTP request to the keyserver and returns the response.
     *
     * @param request The data to be sent in the POST-body of the request
     * @return The response body from the keyserver
     * @throws IOException if something with the network connection breaks
     * @throws KeyserverException if the keyserver responded an error
     */
    private String queryKeyserver(final String request)
            throws IOException, KeyserverException {
        Callable<String> c = new Callable<String>() {
            @Override
            public String call() throws Exception {
                // Send data
                URL url = new URL(getKeyserverURL());
                URLConnection connexion = url.openConnection();
                if (!(connexion instanceof HttpURLConnection)) {
                    throw new IOException("You can only use HTTP-URLs to "
                            + "connect to a "
                            + "keyserver! " + getKeyserverURL()
                            + "is an invalid URL!");
                }
                HttpURLConnection conn = (HttpURLConnection) connexion;
                conn.setDoOutput(true); // sets HTTP-method to POST
                conn.setRequestProperty("Accept", "text/xml");
                conn.setRequestProperty("Content-type", "text/xml");
                OutputStreamWriter writer = new OutputStreamWriter(
                        conn.getOutputStream(),
                        SAMLClientConfig.DEFAULT_ENCODING);

                log.trace("Sending request: {}", request);

                try {
                    writer.write(request);
                    writer.flush();
                } finally {
                    writer.close();
                }

                if (conn.getResponseCode() == HttpStatus.SC_OK) {
                    String response = readStream(conn.getInputStream());
                    log.trace("Received response: {}", response);
                    return response;
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("Received error response: {}",
                                readStream(conn.getErrorStream()));
                    }
                    throw new KeyserverException(
                            "Keyserver responded with HTTP "
                            + conn.getResponseCode()
                            + " " + conn.getResponseMessage());
                }
            }
        };
        Future<String> result = executor.submit(c);
        try {
            return result.get();
        } catch (InterruptedException ex) {
            log.error("Thread was interrupted", ex);
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            Throwable t = ex.getCause();
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            if (t instanceof KeyserverException) {
                throw (KeyserverException) t;
            }
            log.error("Undeclared exception thrown", t);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Validates XML, checks the signature (and if it's trustworthy) and
     * verifies that the response meets all requirements needed for processing.
     *
     * @param response The Response to check
     * @param requestID the ID of request that the keyserver responded to
     * @throws ClientSecurityException if further processing of the response is
     * denied.
     */
    private void validateResponse(final Response response,
            final String requestID) throws ClientSecurityException {
        // validate XML
        try {
            engine.getSamlCoreValidator().validate(response);
        } catch (ValidationException e) {
            throw new ClientSecurityException(e);
        }

        // check the signature
        try {
            engine.getSignatureEngine().validate(response);
        } catch (SignatureEngineException e) {
            e.log();
            throw new ClientSecurityException(e);
        }

        // verify non cryptographic requirements
        engine.getResponseVerifier().verify(response, requestID);

        // check that the response is fresh
        if (recentIDs.getIfPresent(response.getID()) != null) {
            log.error("Response-ID '{}' has been used before ({}), "
                    + "assuming replay attack", response.getID(),
                    recentIDs.getIfPresent(response.getID()));
            throw new ClientSecurityException("Response-ID has been used "
                    + "before, assuming replay attack");
        } else {
            recentIDs.put(response.getID(), response.getIssueInstant());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupInfo createGroup(final Group group) throws SAMLEngineException,
            IOException {
        CreateGroup content = SAMLEngine.getXMLObject(CreateGroup.class);
        prepareGroupInfo(content, group);
        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupInfo.class, response);
        return (GroupInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupInfo getGroup(final String groupName)
            throws SAMLEngineException, IOException {
        GetGroup content = SAMLEngine.getXMLObject(GetGroup.class);
        content.setValue(groupName);

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupInfo.class, response);
        return (GroupInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupInfo updateGroup(final Group group)
            throws SAMLEngineException, IOException {
        UpdateGroup content = SAMLEngine.getXMLObject(UpdateGroup.class);
        prepareGroupInfo(content, group);

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupInfo.class, response);
        return (GroupInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteGroup(final String groupName)
            throws SAMLEngineException, IOException {
        DeleteGroup content = SAMLEngine.getXMLObject(DeleteGroup.class);
        content.setValue(groupName);

        Sec2ResponseMessage response = this.queryResponse(content);
        if (response != null) {
            log.warn("Keyserver responded a {} even though no response was "
                    + "expected", response.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyInfo getGroupKey(final String groupName)
            throws SAMLEngineException, IOException {
        return this.getGroup(groupName).getGroupKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserList getGroupMembers(final String groupName)
            throws SAMLEngineException, IOException {
        GetGroupMembers content = SAMLEngine.getXMLObject(
                GetGroupMembers.class);
        content.setValue(groupName);

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(UserList.class, response);
        return (UserList) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupList getGroupsForUser(final byte[] userID)
            throws SAMLEngineException, IOException {
        GetGroupsForUser content = SAMLEngine.getXMLObject(
                GetGroupsForUser.class);
        content.setValue(Base64.encodeBytes(userID));

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupList.class, response);
        return (GroupList) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfo getUser(final byte[] userID)
            throws SAMLEngineException, IOException {
        GetUserInfoByID content = SAMLEngine.getXMLObject(
                GetUserInfoByID.class);
        content.setValue(Base64.encodeBytes(userID));

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(UserInfo.class, response);
        return (UserInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfo getUser(final String emailAddress)
            throws SAMLEngineException, IOException {
        GetUserInfoByMail content = SAMLEngine.getXMLObject(
                GetUserInfoByMail.class);
        content.setValue(emailAddress);

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(UserInfo.class, response);
        return (UserInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupInfo addUsersToGroup(final String groupName,
            final Collection<byte[]> users) throws SAMLEngineException,
            IOException {
        AddUsersToGroup content = SAMLEngine.getXMLObject(
                AddUsersToGroup.class);
        prepareGroupUsersRelation(content, groupName, users);

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupInfo.class, response);
        return (GroupInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupInfo removeUsersFromGroup(final String groupName,
            final Collection<byte[]> users) throws SAMLEngineException,
            IOException {
        RemoveUsersFromGroup content = SAMLEngine.getXMLObject(
                RemoveUsersFromGroup.class);
        prepareGroupUsersRelation(content, groupName, users);

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupInfo.class, response);
        return (GroupInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfo updateUser(final User user)
            throws SAMLEngineException, IOException {
        throw new UnsupportedOperationException("Currently not supported by "
                + "Sec2");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserList getKnownUsers(final byte[] userID)
            throws SAMLEngineException, IOException {
        GetKnownUsersForUser content = SAMLEngine.getXMLObject(
                GetKnownUsersForUser.class);
        content.setValue(Base64.encodeBytes(userID));

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(UserList.class, response);
        return (UserList) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfo registerUser(final String emailAddress)
            throws SAMLEngineException, IOException {

        RegisterUser content = SAMLEngine.getXMLObject(RegisterUser.class);
        Pair<X509Certificate, X509Certificate> certs;
        try {
            certs = SecurityProviderConnectorFactory.
                    getSecurityProviderConnector().
                    generateClientCertificates(emailAddress);
        } catch (EntityUnknownException ex) {
            throw new SAMLEngineException(ex);
        }
        try {
            content.setSignatureCertificate(SAMLEngine.getXMLObject(
                    SignatureCertificate.class));
            content.getSignatureCertificate().setValue(
                    Base64.encodeBytes(certs.getFirst().getEncoded()));
            content.setEncryptionCertificate(SAMLEngine.getXMLObject(
                    EncryptionCertificate.class));
            content.getEncryptionCertificate().setValue(
                    Base64.encodeBytes(certs.getSecond().getEncoded()));
        } catch (CertificateEncodingException ex) {
            throw new SAMLEngineException(ex);
        }

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(UserInfo.class, response);
        return (UserInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupInfo confirmUser(final String challenge)
            throws SAMLEngineException, IOException {
        ConfirmUser content = SAMLEngine.getXMLObject(ConfirmUser.class);
        content.setChallenge(SAMLEngine.getXMLObject(Challenge.class));
        content.getChallenge().setValue(challenge);
        try {
            content.setID(PrefixedIdentifierGenerator.getInstance().
                    generatePrefixedIdentifier("challenge"));
        } catch (NoSuchAlgorithmException ex) {
            throw new SAMLEngineException(ex);
        }
        try {
            engine.getSignatureEngine().signXMLObject(content);
        } catch (MarshallingException ex) {
            throw new SAMLEngineException(ex);
        }

        Sec2ResponseMessage response = this.queryResponse(content);
        checkResponseType(GroupInfo.class, response);
        return (GroupInfo) response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRegisteredUserID() throws EntityUnknownException {
        return engine.getEntityID();
    }

    /**
     * @return the keyserverURL
     */
    protected String getKeyserverURL() {
        return keyserverURL;
    }

    /**
     * Sets data in a GroupRequestType.
     *
     * @param content the GroupRequestType
     * @param group the group with the data
     */
    private void prepareGroupInfo(final GroupRequestType content,
            final Group group) {
        content.setGroupName(engine.getXSElementGenerator().buildXSString(
                GroupName.DEFAULT_ELEMENT_NAME));
        content.getGroupName().setValue(group.getGroupName());
        content.setGroupOwnerID(engine.getXSElementGenerator().
                buildXSBase64Binary(GroupOwnerID.DEFAULT_ELEMENT_NAME));
        content.getGroupOwnerID().setValue(Base64.encodeBytes(
                group.getOwner().getUserID()));
    }

    /**
     * Sets data in a GroupUsersRelationType.
     *
     * @param content the GroupUsersRelationType
     * @param groupName the name of the group
     * @param users a collection of user IDs
     * @throws SAMLEngineException if XML objects cannot be created
     */
    private static void prepareGroupUsersRelation(
            final GroupUsersRelationType content,
            final String groupName, final Collection<byte[]> users)
            throws SAMLEngineException {
        content.setGroupName(SAMLEngine.getXMLObject(GroupName.class));
        content.getGroupName().setValue(groupName);
        for (byte[] id : users) {
            XSBase64Binary element = SAMLEngine.getXMLObject(UserID.class);
            element.setValue(Base64.encodeBytes(id));
            content.getUsers().add(element);
        }
    }

    /**
     * Checks whether a response is of a certain type.
     *
     * @param <T> The type constraint for the response
     * @param type The type that is expected for the response
     * @param response The response to check
     * @throws KeyserverException if the types don't match
     */
    private static <T extends Sec2ResponseMessage> void checkResponseType(
            final Class<T> type, final Sec2ResponseMessage response)
            throws KeyserverException {
        if (!type.isInstance(response)) {
            throw new KeyserverException("A " + type.getName()
                    + " was expected, but the keyserver responded with a "
                    + response.getClass().getName());
        }
    }

    /**
     * Reads the body of an HTTP response.
     *
     * @param stream The stream to read from
     * @return The HTTP body as String
     * @throws IOException if the connection cannot be read from
     */
    private String readStream(final InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, SAMLClientConfig.DEFAULT_ENCODING));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }
}
