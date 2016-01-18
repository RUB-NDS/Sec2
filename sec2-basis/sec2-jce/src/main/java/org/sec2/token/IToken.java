package org.sec2.token;

import org.sec2.token.keys.ServerKey;
import org.sec2.token.keys.UserKey;
import org.sec2.token.keys.ClusterKey;
import org.sec2.token.keys.DocumentKey;
import org.sec2.token.keys.ClusterKeyId;
import org.sec2.token.exceptions.TokenException;

public interface IToken {

    public static class TokenStatus {

        public final boolean pukIsChanged;
        public final boolean pinIsChanged;
        public final boolean skeyIsSet;
        public final boolean ukeysAreGenerated;

        public TokenStatus(boolean puk, boolean pin,
                boolean ukey, boolean skey) {
            this.pinIsChanged = pin;
            this.pukIsChanged = puk;
            this.skeyIsSet = skey;
            this.ukeysAreGenerated = ukey;
        }
    }

    /**
     * Connect to the Token. Has to be followed by a disconnect after the token
     * is no longer used.
     *
     * @throws TokenException
     */
    public void connect() throws TokenException;

    /**
     * Disconnects the connection to the Token. Use only after successful
     * connection.
     *
     * @throws TokenException
     */
    public void disconnect() throws TokenException;

    /**
     * Authenticate user with PIN. Must be executed before any of the "core"
     * SEC2-functionality can be used. Should be succeeded with a call to
     * logout(), before the connection the the card is closed.
     *
     * @param pin User's PIN
     * @throws TokenException
     */
    public void loginPIN(byte[] pin) throws TokenException;

    /**
     * Logout an already logged in user. Do not call when not logged in.
     *
     * @throws TokenException
     */
    public void logoutPIN() throws TokenException;

    /**
     * Change the PIN of the user. Will work only if authentication with PUK
     * happend prior to this function.
     *
     * @param pin New PIN
     * @throws TokenException
     */
    public void setPIN(byte[] pin) throws TokenException;

    /**
     * Login with PUK. The PUK allows to administer critical functions of the
     * token, i.e., generate a new user-key, import a server key, etc.
     *
     * @param puk Admin's PUK
     * @throws TokenException
     */
    public void loginPUK(byte[] puk) throws TokenException;

    /**
     * Logout admin. Only call this after successful login with PUK.
     *
     * @throws TokenException
     */
    public void logoutPUK() throws TokenException;

    /**
     * Change PUK of token. PUK may be changed only ONCE, i.e. after the card
     * was programmed and the PUK is still set to DEFUALT_PUK.
     *
     * @param puk New PUK
     * @throws TokenException
     */
    public void setPUK(byte[] puk) throws TokenException;

    /**
     * Generate a new set user-key pairs. Generates a signature-pair and an 
     * encryption keypair. Takes a while and requires authentication with PUK.
     *
     * @throws TokenException
     */
    public void generateUserKeys() throws TokenException;

    /**
     * Read the user's public key for signatures from token. Requires
     * authentication with PIN.
     *
     * @return Public key of user
     * @throws TokenException
     */
    public UserKey getUserKeySig() throws TokenException;

    /**
     * Read the user's public key for encryption from token. Requires
     * authentication with PIN.
     *
     * @return Public key of user
     * @throws TokenException
     */
    public UserKey getUserKeyEnc() throws TokenException;

    /**
     * Sign a hash value (SHA1, MD5, SHA256) with the user's private key.
     * Requires authentication with PIN.
     *
     * @param hash Padded SHA1, MD5 or SHA256 hash
     * @return Signature
     * @throws TokenException
     */
    public byte[] sign(byte[] hash) throws TokenException;

    /**
     * Retrieve a new document-key. This function basically asks the token to
     * generate some random data (as much data as a document-key is long). The
     * result will be an unencrypted key. Requires authentication with PIN.
     *
     * @return Unencrypted document-key
     * @throws TokenException
     */
    public DocumentKey createDocumentKey() throws TokenException;

    /**
     * Encrypts a document-key with a cluster-key, which has to be stored on the
     * card already. The cluster-key is referenced by an id. An all-zero IV is
     * used for the CBC mode. This function requires authentication with a PIN.
     *
     * @param keyId id of the cluster-key to use for encryption
     * @param documentKey a decrypted document-key
     * @return an encrypted document-key
     * @throws TokenException
     */
    public DocumentKey encryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey) throws TokenException;

    /**
     * Encrypts a document-key with a cluster-key, which has to be stored on the
     * card already. The cluster-key is referenced by an id. This function
     * requires authentication with a PIN.
     *
     * @param keyId id of the cluster-key to use for encryption
     * @param documentKey a decrypted document-key
     * @param IV a 16-byte array used as IV for encrypting the key in CBC mode
     * @return
     * @throws TokenException
     */
    public DocumentKey encryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException;

    /**
     * Decrypts a document-key with a cluster-key. Uses an all-zero IV for the
     * CBC mode. Requires PIN authentication.
     *
     * @param keyId id of the cluster-key to use, must exist on token
     * @param documentKey encrypted document-key
     * @return decrypted document-key
     * @throws TokenException
     */
    public DocumentKey decryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey) throws TokenException;

    /**
     * Decrypts a document-key with a cluster-key. Uses an all-zero IV for the
     * CBC mode. Requires PIN authentication.
     *
     * @param keyId id of the cluster-key to use, must exist on token
     * @param documentKey encrypted document-key
     * @param IV a 16-byte array used as IV for CBC mode
     * @return
     * @throws TokenException
     */
    public DocumentKey decryptDocumentKey(ClusterKeyId keyId,
            DocumentKey documentKey, byte[] IV) throws TokenException;

    /**
     * Remove a cluster-key from the card. Requires PIN authentication.
     *
     * @param keyId id of cluster-key to remove
     * @throws TokenException
     */
    public void removeClusterKey(ClusterKeyId keyId) throws TokenException;

    /**
     * Import a clusterKey into the token's keystore. The cluster-key is
     * encrypted and thus wrapped for the user's private key. Requires PIN
     * authentication.
     *
     * @param clusterKey cluster-key to be imported
     * @throws TokenException
     */
    public void importClusterKey(ClusterKey clusterKey) throws TokenException;

    /**
     * Check if a given cluster-key is already in the card. Requires PIN
     * authentication.
     *
     * @param keyId id of the cluster-key to find in the token's keystore.
     * @return true if key is found, false otherwise
     * @throws TokenException
     */
    public boolean isClusterKeyAvailable(ClusterKeyId keyId)
            throws TokenException;

    /**
     * Get a list of all available cluster-keys stored in the token's keystore.
     * Requires PIN authentication.
     *
     * @return list of cluster-key ids
     * @throws TokenException
     */
    public ClusterKeyId[] getAvailableClusterKeys() throws TokenException;

    /**
     * Clear the complete keystore of the token. This will make the token
     * "forget" all previously imported cluster-keys. Requires PIN
     * authentication.
     *
     * @throws TokenException
     */
    public void clearClusterKeys() throws TokenException;

    /**
     * Set the server-key on the token. Requires PUK authentication.
     *
     * @param serverKey key of the server/HSM/root of trust
     * @throws TokenException
     */
    public void setServerKey(ServerKey serverKey) throws TokenException;

    /**
     * Read the server-key from the card. Requires PIN authentication.
     *
     * @return public key of server/HSM/root of trust
     * @throws TokenException
     */
    public ServerKey getServerKey() throws TokenException;

    /**
     * Query token for status information, e.g., whether PIN or PUK was set,
     * etc.
     *
     * @return status of token
     * @throws TokenException
     */
    public TokenStatus getStatus() throws TokenException;

    /**
     * Ask token for a version string. Token will answer with the current
     * version string of the running applet code.
     *
     * @return version string
     * @throws TokenException
     */
    public String getVersion() throws TokenException;
}
