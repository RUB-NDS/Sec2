package org.sec2.backend.impl;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.sec2.backend.exceptions.InvalidUserPKCException;

/**
 * CertificateUtil is a utility class that converts byte arrays 
 * to X509Certificate objects.
 * 
 * @author Utimaco Safeware
 *
 */
public class CertificateUtil {

    /**
     * Converts a byte representation of a X.509 certificate to a 
     * X509Certificate object.
     * 
     * @param userPKC byte representation of a X.509 certificate
     * @return A X509Ceritifcate object representing users' certificate.
     * @throws InvalidUserPKCException 
     *         If the byte array is no valid X509 certificate.
     */
    public static X509Certificate convertToX509Certificate(byte[] userPKC)
            throws InvalidUserPKCException {
        X509Certificate certificate = null;
        CertificateFactory factory;
        try {
            factory = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) factory
                    .generateCertificate(new ByteArrayInputStream(userPKC));
        }
        catch (CertificateException e) {
            throw new InvalidUserPKCException(e);
        }
        return certificate;
    }
}
