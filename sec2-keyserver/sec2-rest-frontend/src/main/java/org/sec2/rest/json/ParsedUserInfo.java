/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.json;

import java.security.cert.X509Certificate;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.impl.UserInfo;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
class ParsedUserInfo implements IUserInfo {

    private X509Certificate sig;
    private X509Certificate enc;
    private byte[] id;
    private String mail;
    private boolean confirmed;

    public ParsedUserInfo(UserInfo userInfo) {
    }

    @Override
    public byte[] getId() {
        return id;
    }

    @Override
    public String getEmailAddress() {
        return mail;
    }

    @Override
    public X509Certificate getSignaturePKC() {
        return sig;
    }

    @Override
    public X509Certificate getEncryptionPKC() {
        return enc;
    }

    @Override
    public boolean isConfirmed() {
        return confirmed;
    }
}
