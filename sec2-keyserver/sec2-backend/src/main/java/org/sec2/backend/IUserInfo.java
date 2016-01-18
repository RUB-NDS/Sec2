package org.sec2.backend;

import java.security.cert.X509Certificate;

public interface IUserInfo {
    /**
     * @return The user's id (SHA-256 hash of user's signing public key certificate)
     */
    public byte[] getId();

    /**
     * @return The user's email address
     */
    public String getEmailAddress();

    /**
     * @return The user's signing certificate
     */
    public X509Certificate getSignaturePKC();

    /**
     * @return The user's encryption certificate
     */
    public X509Certificate getEncryptionPKC();
    
    /**
     * @return the confirmation status
     */
    public boolean isConfirmed();
}
