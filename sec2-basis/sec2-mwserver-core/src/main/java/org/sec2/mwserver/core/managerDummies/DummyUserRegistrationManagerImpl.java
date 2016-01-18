/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.mwserver.core.managerDummies;

import java.io.IOException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IUserManager;
import org.sec2.managers.IUserRegistrationManager;
import org.sec2.managers.ManagerProvider;

/**
 *
 * @author dev
 */
public class DummyUserRegistrationManagerImpl implements IUserRegistrationManager {
    
    private static DummyUserRegistrationManagerImpl instance = null;

    private DummyUserRegistrationManagerImpl() {
    }
    
    public static DummyUserRegistrationManagerImpl getInstance() {
        if (instance == null) {
            instance = new DummyUserRegistrationManagerImpl();
        }
        return instance;
    }

    @Override
    public void registerUser(String emailAddress) throws ExMiddlewareException, IOException {
        IUserManager ium = ManagerProvider.getInstance().getUserManager();
        if (ium instanceof DummyUserManagerImpl) {
            DummyUserManagerImpl um = (DummyUserManagerImpl) ium;
            um.addUser_Dummy(emailAddress);
        }
    }

    @Override
    public void confirmUser(String challenge) throws IOException, ExMiddlewareException {
        // nothing to do
    } 
}
