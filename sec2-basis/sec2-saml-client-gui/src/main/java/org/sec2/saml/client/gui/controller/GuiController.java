/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.saml.client.gui.controller;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdesktop.observablecollections.ObservableCollections;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.User;
import org.sec2.saml.client.SAMLClientBootstrap;
import org.sec2.saml.client.gui.model.UserBean;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.jdesktop.observablecollections.ObservableList;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.managers.beans.Group;
import org.sec2.saml.client.exceptions.UnregisteredUserException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.securityprovider.exceptions.IllegalPostInstantinationModificationException;

/**
 *
 * @author dev
 */
public class GuiController {

    private static IGroupManager gm;
    private static IUserManager um;
    private UserBean registeredUser;
    private List<UserBean> knownUsers;
    private List<Group> knownGroups;
    private static GuiController INSTANCE = new GuiController();

    public static GuiController getInstance() {
        return INSTANCE;
    }

    public static void init() throws EntityUnknownException, IllegalPostInstantinationModificationException, SAMLEngineException, UnregisteredUserException, IOException {
            try {
                Security.insertProviderAt(MobileClientProvider.getInstance(), 1);
                Security.addProvider(new BouncyCastleProvider()); //for AES-GCM
            } catch (IllegalStateException e) {
                MobileClientProvider.setType(TokenType.SOFTWARE_TEST_TOKEN_USER_1);
                MobileClientProvider.getInstance(TokenConstants.DEFAULT_PIN);
                init();
            }
            SAMLClientBootstrap.bootstrap();
            final ManagerProvider managerProvider = ManagerProvider.getInstance();
            um = managerProvider.getUserManager();
            gm = managerProvider.getGroupManager();
    }

    public GuiController() {
        knownUsers = ObservableCollections.observableList(new ArrayList<UserBean>());
        knownGroups = ObservableCollections.observableList(new ArrayList<Group>());
        registeredUser = new UserBean();
    }

    public void loginUser() throws EntityUnknownException, IllegalPostInstantinationModificationException, SAMLEngineException, UnregisteredUserException, IOException {
        init();
        registeredUser.setUser(um.getRegisteredUser());
    }

    public UserBean getRegisteredUser() {
        return registeredUser;
    }

    public List<UserBean> getKnownUsers() {
        return knownUsers;
    }

    public List<Group> getKnownGroups() {
        return knownGroups;
    }

    public void updateKnownUsers() {
        knownUsers.clear();
        try {
            final List<byte[]> result = um.getKnownUsers();
            for (byte[] id : result) {
                UserBean toAdd = new UserBean(um.getUser(id));
                knownUsers.add(toAdd);
            }
        } catch (ExMiddlewareException ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void updateGroups() {
        knownGroups.clear();
        try {
            final byte[] userID = registeredUser.getUser().getUserID();
            String [] groupnames = gm.getGroupsForUser(userID);
            for(String groupname : groupnames) {
                Group group = gm.getGroup(groupname);
                knownGroups.add(group);
            }
        } catch (ExMiddlewareException ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(GuiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
