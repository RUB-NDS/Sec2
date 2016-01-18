/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.backend;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.backend.impl.GroupInfo;
import org.sec2.rest.RestException;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class Backend {

    public static IGroupInfo createGroup(byte[] userID, String groupName) throws
            RestException {
        try {
            return BackendHolder.getBackend().createGroup(userID,
                    groupName);
        } catch (GroupAlreadyExistsException ex) {
            throw new RestException(ex);
        } catch (UserNotFoundException ex) {
            throw new RestException(ex);
        } catch (InvalidGroupNameException ex) {
            throw new RestException(ex);
        } catch (PermissionException ex) {
            throw new RestException(ex);
        }
    }

    public static void deleteGroup(byte[] userID, String groupName) throws
            RestException {
        try {
            BackendHolder.getBackend().deleteGroup(userID,
                    groupName);
        } catch (PermissionException ex) {
            throw new RestException(ex);
        }
    }

    public static IGroupInfo modifyGroup(byte[] userID, IGroupInfo g)
            throws RestException {
        try {
            return BackendHolder.getBackend().modifyGroup(userID, g);
        } catch (PermissionException ex) {
            throw new RestException(ex);
        }

    }

    public static IGroupInfo getGroupInfo(byte[] userID, String groupName)
            throws RestException {
        try {
            return BackendHolder.getBackend().getGroupInfo(userID,
                    groupName);
        } catch (UserNotFoundException ex) {
            throw new RestException(ex);
        } catch (PermissionException ex) {
            throw new RestException(ex);
        }
    }

}
