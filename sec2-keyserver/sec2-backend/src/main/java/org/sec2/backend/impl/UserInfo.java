package org.sec2.backend.impl;

import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.sec2.backend.IUserInfo;

public class UserInfo implements IUserInfo {

    private X509Certificate encryptionPKC;
    private X509Certificate signaturePKC;
    private String emailAddress;
    private byte[] id;
    private boolean confirmed;
    
    @Override
    public int hashCode() {
        return emailAddress.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof UserInfo)) {
            return false;
        }
        UserInfo otherUser = (UserInfo) other;
        if (!otherUser.getEmailAddress().equalsIgnoreCase(
                    this.getEmailAddress())
                || !Arrays.equals(otherUser.getId(), this.getId())) {
            return false;
        }
        return true;
    }
    
    @Override
    public String getEmailAddress() {
        return this.emailAddress;
    }

    @Override
    public X509Certificate getEncryptionPKC() {
        return this.encryptionPKC;
    }

    @Override
    public byte[] getId() {
        return this.id;
    }
    
    @Override
    public X509Certificate getSignaturePKC() {
        return this.signaturePKC;
    }

    public void setEncryptionPKC(X509Certificate encryptionPKC) {
        this.encryptionPKC = encryptionPKC;
    }

    public void setSignaturePKC(X509Certificate signaturePKC) {
        this.signaturePKC = signaturePKC;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setId(byte[] id) {
        this.id = id;
    }

    public String toString() {
        String ret = "User " + DatatypeConverter.printHexBinary(id);
        ret += "\nSignPKC: " + signaturePKC.toString();
        ret += "\nEncPKC: " + encryptionPKC.toString();
        ret += "eMail: " + emailAddress;
        return ret;
    }

    @Override
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @param confirmed the confirmed to set
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
    
}
