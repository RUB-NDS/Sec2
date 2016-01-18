/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sec2.managers.impl;

import java.io.IOException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.beans.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy group manager for testing purposes. 
 * 
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @version 0.1
 */
public class DummyGroupManager implements IGroupManager {
    
    /**
     * logger
     */
    private static Logger logger =
            LoggerFactory.getLogger(DummyGroupManager.class);

    @Override
    public void createGroup(Group group) throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Group getGroup(String groupName) throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateGroup(Group group) throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteGroup(String groupName) throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getGroupsForUser(byte[] userId) throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void ensureGroupKeyIsAvailable(String groupName) throws ExMiddlewareException, IOException {
        logger.debug("Ensure that Group Key is obtained");
    }

    @Override
    public void revertGroupMembers(Group group) throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
