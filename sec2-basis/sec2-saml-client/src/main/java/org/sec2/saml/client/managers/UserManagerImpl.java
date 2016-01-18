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
package org.sec2.saml.client.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.util.Base64;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.managers.IUserManager;
import org.sec2.managers.beans.User;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.client.connector.IKeyserverConnector;
import org.sec2.saml.client.connector.KeyserverConnectorFactory;
import org.sec2.saml.client.exceptions.SAML2MiddlewareProxyException;
import org.sec2.saml.client.exceptions.UnregisteredUserException;
import org.sec2.saml.exceptions.KeyserverException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.UserResponseType;

/**
 * Manager used to manage users: view, get and modify current user.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.4
 *
 * August 21, 2013
 */
public final class UserManagerImpl implements IUserManager {

    /**
     * Singleton instance.
     */
    private static UserManagerImpl instance;
    /**
     * Collection of users, mapped by ID. Makes sure that a user is reflected by
     * exactly one object at a time.
     */
    private final HashMap<ByteArrayAsList, WeakReference<User>> usersByID;
    /**
     * Collection of users, mapped by email address. Makes sure that a user is
     * reflected by exactly one object at a time.
     */
    private final HashMap<String, WeakReference<User>> usersByMail;
    /**
     * Cache of users, mapped by ID. Makes sure that a user is updated recently.
     * Fetches missing users automatically.
     */
    private final LoadingCache<ByteArrayAsList, User> usersByIDCache;
    /**
     * Cache of users, mapped by email address. Makes sure that a user is
     * updated recently. Fetches missing users automatically.
     */
    private final LoadingCache<String, User> usersByMailCache;
    /**
     * User who is logged into the middleware (and whose smartcard is inserted
     * etc.).
     */
    private final User registeredUser;
    /**
     * The connector to communicate with the keyserver.
     */
    private final IKeyserverConnector connector;

    /**
     * @return The singleton instance of the UserManager
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if the current user cannot be fetched from the
     * keyserver
     * @throws UnregisteredUserException if the user is not yet registered at
     * the keyserver
     */
    public static synchronized UserManagerImpl getInstance()
            throws EntityUnknownException, SAMLEngineException, IOException,
            UnregisteredUserException {
        if (instance == null) {
            instance = new UserManagerImpl();
        }
        return instance;
    }

    /**
     * Creates a new UserManager and intializes all fields.
     *
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if the current user cannot be fetched from the
     * keyserver
     * @throws UnregisteredUserException if the user is not yet registered at
     * the keyserver
     */
    private UserManagerImpl()
            throws EntityUnknownException, SAMLEngineException, IOException,
            UnregisteredUserException {
        try {
            this.connector = KeyserverConnectorFactory.getKeyserverConnector();
        } catch (SAMLEngineException e) {
            if (e.getCause() instanceof EntityUnknownException) {
                throw (EntityUnknownException) e.getCause();
            }
            throw e;
        }
        this.usersByID = new HashMap<ByteArrayAsList, WeakReference<User>>();
        this.usersByMail = new HashMap<String, WeakReference<User>>();
        this.usersByIDCache = CacheBuilder.from(
                SAMLClientConfig.MANAGERS_CACHE_CONFIG).build(
                new CacheLoader<ByteArrayAsList, User>() {
            /**
             * Retrieves a user and puts it in both caches.
             */
            @Override
            public User load(final ByteArrayAsList key)
                    throws SAMLEngineException, IOException {
                User user = updateUserByID(key);
                usersByMailCache.put(user.getEmailAddress(), user);
                return user;
            }
        });
        this.usersByMailCache = CacheBuilder.from(
                SAMLClientConfig.MANAGERS_CACHE_CONFIG).build(
                new CacheLoader<String, User>() {
            /**
             * Retrieves a user and puts it in both caches.
             */
            @Override
            public User load(final String key)
                    throws SAMLEngineException, IOException {
                User user = updateUserByMail(key);
                usersByIDCache.put(new ByteArrayAsList(user.getUserID()), user);
                return user;
            }
        });
        try {
            this.registeredUser = this.usersByIDCache.get(
                    new ByteArrayAsList(this.connector.getRegisteredUserID()));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SAMLEngineException) {
                SAMLEngineException ex = (SAMLEngineException) e.getCause();
                if (ex instanceof KeyserverException) {
                    throw new UnregisteredUserException();
                } else {
                    throw ex;
                }
            }
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IllegalStateException(e.getCause().getClass()
                    + " should not be thrown by load(byte[])");
        }
    }

    /**
     * Retrieves a user by ID and puts it in both maps.
     *
     * @param key The user's ID
     * @return The user
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if either no connection to the keyserver is available
     * or the keyserver detected an error.
     */
    private User updateUserByID(final ByteArrayAsList key)
            throws SAMLEngineException, IOException {
        User user;
        byte[] keyArray = key.toByteArray();
        UserResponseType response = connector.getUser(keyArray);
        if (!Arrays.equals(Base64.decode(response.getUserID().getValue()),
                keyArray)) {
            throw new IOException("Keyserver responded user with ID "
                    + response.getUserID().getValue() + " when ID "
                    + Base64.encodeBytes(keyArray) + " was requested");
        }
        if (usersByID.containsKey(key)) {
            user = usersByID.get(key).get();
            if (user != null) {
                // user already requested before and is still in memory
                if (!response.getEmailAddress().getValue().equals(user.
                        getEmailAddress())) {
                    usersByMail.remove(user.getEmailAddress());
                    user.setEmailAddress(response.getEmailAddress().getValue());
                    usersByMail.put(user.getEmailAddress(),
                            new WeakReference<User>(user));
                }
            } else {
                user = createNewUser(response);
            }
        } else {
            user = createNewUser(response);
        }
        return user;
    }

    /**
     * Retrieves a user by email address and puts it in both maps.
     *
     * @param key The user's email address
     * @return The user
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if either no connection to the keyserver is available
     * or the keyserver detected an error.
     */
    private User updateUserByMail(final String key) throws IOException,
            SAMLEngineException {
        User user;
        UserResponseType response = connector.getUser(key);
        if (!response.getEmailAddress().getValue().equals(key)) {
            throw new IOException("Keyserver responded user with email "
                    + response.getEmailAddress().getValue() + " when email "
                    + key + " was requested");
        }
        if (usersByMail.containsKey(key)) {
            user = usersByMail.get(key).get();
            if (user != null) {
                // user already requested before and is still in memory
                if (!Arrays.equals(Base64.decode(
                        response.getUserID().getValue()), user.getUserID())) {
                    throw new IOException("Keyserver responded user "
                            + "with ID " + response.getUserID().getValue()
                            + " when ID " + Base64.encodeBytes(user.getUserID())
                            + " was linked with the email " + key);
                }
            } else {
                user = createNewUser(response);
            }
        } else { // user never requested before
            user = createNewUser(response);
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getRegisteredUser() {
        return this.registeredUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(final byte[] userID)
            throws SAML2MiddlewareProxyException {
        try {
            return getUserInternal(userID);
        } catch (SAMLEngineException e) {
            e.log();
            throw new SAML2MiddlewareProxyException(e);
        }
    }

    /**
     * Returns the user with the passed user ID or null if no user was found.
     *
     * @param userID The user ID of the user to be returned
     * @return The user with the passed ID
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     */
    protected User getUserInternal(final byte[] userID)
            throws SAMLEngineException {
        User user;
        try {
            user = this.usersByIDCache.get(new ByteArrayAsList(userID));
        } catch (ExecutionException e) {
            throw (SAMLEngineException) e.getCause();
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(final String emailAddress)
            throws SAML2MiddlewareProxyException {
        User user;
        try {
            user = this.usersByMailCache.get(emailAddress);
        } catch (ExecutionException e) {
            SAMLEngineException cause = (SAMLEngineException) e.getCause();
            cause.log();
            throw new SAML2MiddlewareProxyException(cause);
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeEmailAddressOfRegisteredUser(final String newEmailAddress)
            throws SAML2MiddlewareProxyException {
        throw new UnsupportedOperationException(
                "Not supported by the current sec2 specs!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<byte[]> getKnownUsers()
            throws SAML2MiddlewareProxyException, IOException {
        List<XSBase64Binary> encIDs;
        try {
            encIDs = this.connector.getKnownUsers(
                    this.registeredUser.getUserID()).getUserIDs();
        } catch (SAMLEngineException e) {
            e.log();
            throw new SAML2MiddlewareProxyException(e);
        }
        List<byte[]> ids = new ArrayList<byte[]>(encIDs.size());
        for (XSBase64Binary element : encIDs) {
            ids.add(Base64.decode(element.getValue()));
        }
        return ids;
    }

    /**
     * Invalidates all caches, forces the manager to update all information from
     * the keyserver.
     */
    public void invalidateCaches() {
        this.usersByIDCache.invalidateAll();
        this.usersByMailCache.invalidateAll();
        usersByIDCache.put(new ByteArrayAsList(this.registeredUser.getUserID()),
                this.registeredUser);
        usersByMailCache.put(this.registeredUser.getEmailAddress(),
                this.registeredUser);
    }

    /**
     * Creates a new user object and adds it to both maps.
     *
     * @param response The response to create the user from
     * @return The user
     */
    private User createNewUser(final UserResponseType response) {
        // user never requested before
        User user = new User(Base64.decode(response.getUserID().getValue()),
                response.getEmailAddress().getValue());
        WeakReference<User> weakRef = new WeakReference<User>(user);
        usersByID.put(new ByteArrayAsList(user.getUserID()), weakRef);
        usersByMail.put(user.getEmailAddress(), weakRef);
        return user;
    }
}
