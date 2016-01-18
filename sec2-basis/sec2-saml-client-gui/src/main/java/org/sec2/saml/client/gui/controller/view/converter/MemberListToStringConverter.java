/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.saml.client.gui.controller.view.converter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.beansbinding.Converter;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.User;

/**
 *
 * @author dev
 */
public class MemberListToStringConverter extends Converter<List<byte[]>, String> {

    @Override
    public String convertForward(List<byte[]> s) {
        IUserManager um = ManagerProvider.getInstance().getUserManager();
        StringBuilder sb = new StringBuilder();
        for (byte[] userId : s) {
            User u;
            try {
                u = um.getUser(userId);
            } catch (ExMiddlewareException ex) {
                Logger.getLogger(MemberListToStringConverter.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            } catch (IOException ex) {
                Logger.getLogger(MemberListToStringConverter.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            sb.append(u.getEmailAddress());
            sb.append(", ");
        }
        String result = sb.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    @Override
    public List<byte[]> convertReverse(String t) {
        throw new UnsupportedOperationException("Read only.");
    }
}
