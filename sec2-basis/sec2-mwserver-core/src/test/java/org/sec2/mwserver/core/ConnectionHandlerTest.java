package org.sec2.mwserver.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import junit.framework.TestCase;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;
import org.sec2.exceptions.BootstrapException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IDocumentKeyManager;
import org.sec2.managers.exceptions.KeyManagerException;
import org.sec2.managers.impl.DocumentKeyManagerImpl;
import org.sec2.mwserver.core.exceptions.ExServerConnectionException;
import org.sec2.mwserver.core.exceptions.HandleRequestException;
import org.sec2.mwserver.core.rest.AbstractRestFunctionExecutor;
import org.sec2.mwserver.core.util.ICryptoUtils;
import org.sec2.persistence.PersistenceManagerContainer;
import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ExtendedDocumentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This JUnit-Class tests the methods of class ConnectionHandler.
 *
 * @author nike
 */
public final class ConnectionHandlerTest extends TestCase {
    
    private TestSocketFactory factory = new TestSocketFactory();
    private TestRestFunctionExecutor trfe = new TestRestFunctionExecutor();
    private TestCryptoUtils cryptoUtils = new TestCryptoUtils();
    private TestDbManager dbManager = new TestDbManager();
    private static boolean initialized = false;
    static final String GROUP111_ID = "group111";
    /**
     * logger
     */
    private static Logger logger =
            LoggerFactory.getLogger(ConnectionHandlerTest.class);
    
    public ConnectionHandlerTest() throws BootstrapException, IOException, 
            IllegalPostInstantinationModificationException {
        // it would be cooler to use @BeforeClass, but this is not possible in 
        // junit 3 :)
        if (!initialized) {
            MobileClientProvider.setType(TokenType.SOFTWARE_TOKEN);
            MobileClientProvider.getInstance(TokenConstants.DEFAULT_PIN);
            PersistenceManagerContainer.setPersistenceManager(dbManager);
            MWServerCoreBootstrap.bootstrap();
            Socket.setSocketImplFactory(factory);
            initialized = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setUp() {
        
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * register()-method of the REST-interface when sending the appropriate
     * request.
     */
    public void testRunHandleRequestRegister() {
        TestClientSocket socket = null;
        ConnectionHandler connectionHandler = null;
        
        try {
            socket = new TestClientSocket(TestRequests.getRegisterRequest());
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(TestRestFunctionExecutor.REGISTER));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * getRegisteredUser()-method of the REST-interface when sending the
     * appropriate request.
     */
    public void testRunHandleRequestGetRegisteredUser() {
        Socket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getGetRegisteredUserRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(
                    TestRestFunctionExecutor.GET_REGISTERED_USER));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * getUsersInGroup()-method of the REST-interface when sending the
     * appropriate request.
     */
    public void testRunHandleRequestGetUsersInGroup() {
        Socket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getGetUsersInGroupRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(
                    TestRestFunctionExecutor.GET_USERS_IN_GROUP));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * getUser()-method of the REST-interface when sending the appropriate
     * request.
     */
    public void testRunHandleRequestGetUser() {
        Socket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getGetUserRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(
                    TestRestFunctionExecutor.GET_USER));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * getGroup()-method of the REST-interface when sending the appropriate
     * request.
     */
    public void testRunHandleRequestGetGroup() {
        Socket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getGetGroupRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(
                    TestRestFunctionExecutor.GET_GROUP));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * getGroupsForUser()-method of the REST-interface when sending the
     * appropriate request.
     */
    public void testRunHandleRequestGetGroupsForUser() {
        Socket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getGetGroupsForUserRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(
                    TestRestFunctionExecutor.GET_GROUPS_FOR_USER));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequest-method, if it calls the
     * getKnownUsers()-method of the REST-interface when sending the appropriate
     * request.
     */
    public void testRunHandleRequestGetKnownUsers() {
        Socket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getGetKnownUsersRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            assertTrue(trfe.isFunctionCalled(
                    TestRestFunctionExecutor.GET_KNOWN_USERS));
        } catch (IOException ioe) {
            fail();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
     * This method tests the handleRequestRequest-method.
     */
    public void testRunHandleRequestRedirect() {
        TestClientSocket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        try {
            request = TestRequests.getNoRestRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            String content = socket.getOutputStreamContent();
            assertTrue(content.startsWith("HTTP/1.1 200 OK"));
        } catch (IOException ioe) {
            fail(ioe.getLocalizedMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * This method tests the handleRequestRedirect-method.
     */
    public void testPutWithContent() throws KeyManagerException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        TestClientSocket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        // set ExtendedDocumentKey containing encrypted and decrypted document key
        // with a given group key
        ExtendedDocumentKey edk = createNiceMock(ExtendedDocumentKey.class);
        expect(edk.getKeyIdWithNonce()).andReturn("group111;group111;nonce123");
        byte[] key = new byte[16];
        DocumentKey encDK = new DocumentKey(key, true, "group111;group111;nonce123");
        expect(edk.getEncryptedDocumentKeys()).andReturn(Collections.singletonList(encDK));
        DocumentKey decDK = new DocumentKey(key, false, "group111;nonce123");
        expect(edk.getDecryptedDocumentKey()).andReturn(decDK);
        replay(edk);
        
        // create DocumentKeyManagerImpl mock
        DocumentKeyManagerImpl dkmi = createNiceMock(DocumentKeyManagerImpl.class);
        expect(dkmi.getDecryptedDocumentKeyForGroups("group111")).andReturn(edk);
        replay(dkmi);
        
        // set the singleton instance of the DocumentKeyManagerImpl to the created mock
        setDokumentKeyManger(dkmi);
        
        try {
            request = TestRequests.getPutRequestWithData(
                    dbManager.getAppAuthKey("test"), cryptoUtils, "localhost:50101");
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            String output = socket.getOutputStreamContent();
            assertTrue(output.startsWith("HTTP/1.1 200 OK"));
        } catch (IOException ioe) {
            fail();
        } finally {
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }
    
    
    /**
     * This method tests the handleRequestRedirect-method and the decryption module
     */
    public void testGetWithContentFromDB() throws KeyManagerException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        TestClientSocket socket = null;
        ConnectionHandler connectionHandler = null;
        String request = null;
        
        // set ExtendedDocumentKey containing encrypted and decrypted document key
        // with a given group key
        ExtendedDocumentKey edk = createNiceMock(ExtendedDocumentKey.class);
        expect(edk.getKeyIdWithNonce()).andReturn("group111;group111;nonce123");
        byte[] key = new byte[16];
        DocumentKey decDK = new DocumentKey(key, false, "group111;nonce123");
        expect(edk.getDecryptedDocumentKey()).andReturn(decDK);
        replay(edk);
        
        // create DocumentKeyManagerImpl mock
        DocumentKeyManagerImpl dkmi = createNiceMock(DocumentKeyManagerImpl.class);
        expect(dkmi.getDecryptedDocumentKey(anyObject(LinkedList.class))).andReturn(edk);
        replay(dkmi);
        
        // set the singleton instance of the DocumentKeyManagerImpl to the created mock
        setDokumentKeyManger(dkmi);
        
        TestClientSocketImpl.RESPONSE_CONTEXT = TestResponses.RESPONSE_CONTENT_ENCRYPTED_XML;
        
        try {
            request = TestRequests.getNoRestRequest(
                    dbManager.getAppAuthKey("test"), cryptoUtils);
            assertNotNull(request);
            socket = new TestClientSocket(request);
            connectionHandler = new ConnectionHandler(socket, trfe,
                    cryptoUtils);
            connectionHandler.run();
            String output = socket.getOutputStreamContent();
            System.out.println(output);
            assertTrue(output.startsWith("HTTP/1.1 200 OK"));
        } catch (IOException ioe) {
            fail();
        } finally {
            TestClientSocketImpl.RESPONSE_CONTEXT = TestResponses.RESPONSE_CONTENT_XML;
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }
    
    
    /**
     * Ugly reflection hack
     * 
     * @param dkmi
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    private void setDokumentKeyManger(IDocumentKeyManager dkmi) throws NoSuchFieldException, 
            IllegalArgumentException, IllegalAccessException {
        for (Field f : DocumentKeyManagerImpl.class.getDeclaredFields()) {
            if (f.getName().equalsIgnoreCase("instance")) {
                f.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(f, f.getModifiers() & ~Modifier.PRIVATE);
                f.set(null, dkmi);                
            }
        }
    }
    
    private final class TestRestFunctionExecutor
            extends AbstractRestFunctionExecutor {

        /**
         * Index of "register" in the array
         */
        public static final int REGISTER = 0;
        public static final int GET_REGISTERED_USER = 1;
        public static final int GET_USERS_IN_GROUP = 2;
        public static final int GET_USER = 3;
        public static final int GET_GROUP = 4;
        public static final int GET_GROUPS_FOR_USER = 5;
        public static final int GET_KNOWN_USERS = 6;
        private boolean[] functionCalled = new boolean[]{false, false, false,
            false, false, false, false};
        
        @Override
        public void register(final String appName, final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException, HandleRequestException {
            functionCalled[REGISTER] = true;
        }
        
        @Override
        public void getRegisteredUser(final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException {
            functionCalled[GET_REGISTERED_USER] = true;
        }
        
        @Override
        public void getUsersInGroup(final String groupId, final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException, HandleRequestException {
            functionCalled[GET_USERS_IN_GROUP] = true;
        }
        
        @Override
        public void getUser(final String userId, final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException {
            functionCalled[GET_USER] = true;
        }
        
        @Override
        public void getGroup(final String groupId, final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException {
            functionCalled[GET_GROUP] = true;
        }
        
        @Override
        public void getGroupsForUser(final String userId, final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException, HandleRequestException {
            functionCalled[GET_GROUPS_FOR_USER] = true;
        }
        
        @Override
        public void getKnownUsers(final String nonce,
                final BufferedWriter writer) throws ExMiddlewareException,
                ExServerConnectionException {
            functionCalled[GET_KNOWN_USERS] = true;
        }
        
        @Override
        protected boolean allowAppToRegister(String appName) {
            return true;
        }
        
        @Override
        protected ICryptoUtils getCryptoUtils() {
            return null;
        }

        /**
         * Returns, if the function with the passed index was called.
         *
         * @param functionIndex - The index of the function
         * @return
         */
        public boolean isFunctionCalled(int functionIndex) {
            return functionCalled[functionIndex];
        }
    }
}
