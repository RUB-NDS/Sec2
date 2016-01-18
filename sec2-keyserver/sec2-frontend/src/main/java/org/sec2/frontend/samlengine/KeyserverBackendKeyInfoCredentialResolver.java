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
package org.sec2.frontend.samlengine;

//CHECKSTYLE.OFF: LineLengthCheck - Cannot make this line shorter
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.AbstractCriteriaFilteringCredentialResolver;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.criteria.KeyNameCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCriteria;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.util.Base64;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.frontend.BackendHolder;
import org.sec2.frontend.KeyserverFrontendConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//CHECKSTYLE.ON: LineLengthCheck

/**
 * An implementation of {@link KeyInfoCredentialResolver}
 * which uses the sec2 backend as the underlying credential source.
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * July 29, 2013
 */
public final class KeyserverBackendKeyInfoCredentialResolver
                extends AbstractCriteriaFilteringCredentialResolver
                implements KeyInfoCredentialResolver {

    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(
            KeyserverBackendKeyInfoCredentialResolver.class);

    /**
     * The collection of credentials which is the underlying store for
     * the resolver.
     */
    private final LoadingCache<String, Iterable<Credential>> credentials;

    /**
     * The length of a user's ID in bytes, e.g. 32 for SHA-256.
     */
    private static final int USER_ID_LENGTH;

    static {
        int temp = 0;
        try {
            temp = MessageDigest.getInstance(
                    KeyserverFrontendConfig.DIGEST_ALGORITHM).getDigestLength();
        } catch (final NoSuchAlgorithmException e) {
            LOG.error("DigestAlgorithm {} could not be found. Please file a "
                    + "bug report, this error should be impossible",
                    KeyserverFrontendConfig.DIGEST_ALGORITHM);
        }
        USER_ID_LENGTH = temp;
    }

    /**
     * Constructor.
     * A Guava cache is used as the underlying collection implementation.
     */
    private KeyserverBackendKeyInfoCredentialResolver() {
        super();
        credentials = CacheBuilder.from(
                KeyserverFrontendConfig.KEY_CACHE_CONFIG).build(
                new CacheLoader<String, Iterable<Credential>>() {
                    /**
                     * Retrieves a key from the backend.
                     */
                    @Override
                    public Iterable<Credential> load(final String userIDBase64)
                            throws UserNotFoundException {
                        LOG.debug("Fetching certificates for user '{}' "
                                + "from backend", userIDBase64);
                        return fetchCredentialsFromBackend(userIDBase64);
                    }
                });
    }

    /** {@inheritDoc} */
    @Override
    protected Iterable<Credential> resolveFromSource(
            final CriteriaSet criteriaSet) throws SecurityException {
        String userIDBase64 = null;
        if (criteriaSet.contains(KeyInfoCriteria.class)) {
            LOG.trace("CriteriaSet contains a KeyInfoCriteria");
            userIDBase64 = getUserIDFromKeyInfoCriteria(
                    criteriaSet.get(KeyInfoCriteria.class));
        }
        if (userIDBase64 == null
                && criteriaSet.contains(KeyNameCriteria.class)) {
            LOG.trace("CriteriaSet contains a KeyNameCriteria");
            userIDBase64 = criteriaSet.get(KeyNameCriteria.class).getKeyName();
        }
        if (userIDBase64 == null) {
            LOG.warn("CriteriaSet contained neither a valid KeyInfoCriteria "
                    + "nor a valid KeyNameCriteria, could not resolve "
                    + "credentials");
            return Collections.EMPTY_LIST;
        }

        LOG.trace("Resolving certificates for user '{}'", userIDBase64);
        Iterable<Credential> keys;
        try {
            keys = credentials.get(userIDBase64);
        } catch (ExecutionException e) {
            LOG.warn("No user with ID '{}' found", userIDBase64);
            keys = Collections.EMPTY_LIST;
        }
        return keys;
    }

    /**
     * Extracts a user's ID from a KeyInfo element.
     * @param keyInfoCriterion The KeyInfoCriterion that wraps the KeyInfo
     *          element
     * @return the user's ID in Base64 encoding or null if the KeyInfoCriterion
     *          contained no valid user ID
     */
    private String getUserIDFromKeyInfoCriteria(
            final KeyInfoCriteria keyInfoCriterion) {
        String userIDBase64 = null;
        try {
            KeyInfo ki = keyInfoCriterion.getKeyInfo();
            if (ki == null) {
                throw new IllegalArgumentException(
                        "KeyInfoCriteria contains no KeyInfo");
            }
            if (ki.getKeyNames().isEmpty()) {
                throw new IllegalArgumentException(
                        "KeyInfo contains no KeyName");
            }
            for (KeyName kn : ki.getKeyNames()) {
                userIDBase64 = kn.getValue();
                byte[] userID = Base64.decode(userIDBase64);
                if (userID.length == USER_ID_LENGTH) {
                    break;
                } else {
                    LOG.warn("'{}' is no valid user ID, ignoring it",
                            userIDBase64);
                    userIDBase64 = null;
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage());
        }
        return userIDBase64;
    }

    /**
     * Fetches the two certificates of a user from the keyserver's backend.
     * @param userIDBase64 the ID of the user in Base64 encoding
     * @return a collection containing 2 credentials with certificates in them
     * @throws UserNotFoundException if the user does not exist
     */
    private Collection<Credential> fetchCredentialsFromBackend(
            final String userIDBase64) throws UserNotFoundException {
        IUserInfo user = BackendHolder.getBackend().getUserInfo(
                Base64.decode(userIDBase64));
        BasicX509Credential signCert = new BasicX509Credential();
        signCert.setEntityCertificate(user.getSignaturePKC());
        signCert.getKeyNames().add(userIDBase64);
        BasicX509Credential encCert  = new BasicX509Credential();
        encCert.setEntityCertificate(user.getEncryptionPKC());
        encCert.getKeyNames().add(userIDBase64);
        ArrayList<Credential> certs = new ArrayList<Credential>(2);
        certs.add(signCert);
        certs.add(encCert);
        return certs;
    }

    /**
     * @return the keyserver's backend
     */
    public static KeyserverBackendKeyInfoCredentialResolver getInstance() {
        return KeyserverBackendKeyInfoCredentialResolverHolder.INSTANCE;
    }

    /**
     * Nested class holding instance.
     */
    private static class KeyserverBackendKeyInfoCredentialResolverHolder {
        /**
         * The singleton instance.
         */
        private static final KeyserverBackendKeyInfoCredentialResolver
                INSTANCE = new KeyserverBackendKeyInfoCredentialResolver();
    }
}
