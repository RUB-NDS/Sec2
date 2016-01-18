package org.sec2.android.app;

import org.sec2.android.ISec2MiddlewareService;
import org.sec2.android.exceptions.Sec2MwServiceRemoteException;

import android.os.RemoteException;

/**
 * This class provides method of the Sec2-middleware-service, represented by
 * the interface ISec2MiddlewareService, to activities of the
 * Sec2-Administration-App. In order to work a valid instance of the service
 * implementation has to be provided to this class first. The instance should
 * only be provided by the activity, which handles the whole service. If the
 * service-instance should not longer be used by other activites, the activity
 * may call method invalidService().
 *
 * @author nike
 */
final class ServiceMethodProvider
{
    private static ISec2MiddlewareService service = null;
    private static final String ERR_SERVICE_NOT_AVAILABLE =
            "Service is not available yet!";
    private static final String ERR_SERVICE_NOT_RUNNING =
            "Service is not running!";

    /**
     * Sets the service. The method has to be called before any other method of
     * this class is called; otherwise a RemoteException is thrown. The passed
     * service must not be NULL, otherwise a NPE is thrown. The method should
     * <br>only</br> be called by the activity, which handles the whole
     * service.
     *
     * @param service - The service, which must not be NULL.
     *
     * @throws NullPointerException if the passed service is NULL.
     */
    static void setService(final ISec2MiddlewareService service)
    {
        if (service != null)
        {
            ServiceMethodProvider.service = service;
        }
        else
        {
            throw new NullPointerException("Service must not be NULL!");
        }
    }

    /**
     * Invalidates the service, so that other activities can't access the
     * methods of the service anymore. This method should <br>only</br> be
     * called by the activity, which handles the whole service.
     */
    static void invalidService()
    {
        service = null;
    }

    /**
     * Adds the user with the passed user-ID to the groups with the passed
     * group-IDs as a member. The method returns TRUE, if the method could have
     * been completed without any notification, warning or exception. This
     * means, the user could have been added to all groups with the passed IDs.
     * Otherwise it returns FALSE.
     *
     * @param userId - The user's ID, who is to be added to groups.
     * @param groupIds - The IDs of the groups, where the user is to be added.
     *
     * @return TRUE, if the method could have been completed without any
     *  notification, warning or exception. Otherwise it returns FALSE.
     *
     * @throws RemoteException
     */
    boolean addUserToGroups(final String userId, final String[] groupIds)
            throws RemoteException
            {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.addUserToGroups(userId, groupIds);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
            }

    /**
     * Removes the user with the passed user-ID from the groups with the passed
     * group-IDs as a member. The method returns TRUE, if the method could have
     * been completed without any notification, warning or exception. This
     * means, the user could have been removed from all groups with the passed
     * IDs. Otherwise it returns FALSE.
     *
     * @param userId - The user's ID, who is to be removed from groups.
     * @param groupIds - - The IDs of the groups, where the user is to be
     *  removed.
     *
     * @return TRUE, if the method could have been completed without any
     *  notification, warning or exception. Otherwise it returns FALSE.
     *
     * @throws RemoteException
     */
    boolean removeUserFromGroups(final String userId, final String[] groupIds)
            throws RemoteException
            {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.removeUserFromGroups(userId, groupIds);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
            }

    /**
     * Adds all users with the passed user-IDs to the group with the passed
     * group-ID as a member. The method returns TRUE, if the method could have
     * been completed without any notification, warning or exception. This
     * means, all users with the passed IDs could have been added to the group
     * with the passed ID. Otherwise it returns FALSE.
     *
     * @param userIds - The users' IDs, who are to be added to the group.
     * @param groupId - The ID of the group, where the users are to be added.
     *
     * @return TRUE, if the method could have been completed without any
     *  notification, warning or exception. Otherwise it returns FALSE.
     *
     * @throws RemoteException
     */
    boolean addUsersToGroup(final String[] userIds, final String groupId)
            throws RemoteException
            {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.addUsersToGroup(userIds, groupId);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
            }

    /**
     * Removes the users with the passed user-IDs from the group with the
     * passed group-ID as a member. The user-IDs must be valid user-IDs encoded
     * in base64. The method returns TRUE, if the method could have been
     * completed without any notification, warning or exception. This means,
     * all users with the passed IDs could have been removed from the group.
     * Otherwise it returns FALSE.
     *
     * @param userIds - The IDs of the users, who are to be added to the group,
     *  encoded in base64.
     * @param groupId - The ID of the group, where the users are to be added.
     *
     * @return TRUE, if the method could have been completed without any
     *  notification, warning or exception. Otherwise it returns FALSE.
     *
     * @throws RemoteException
     */
    boolean removeUsersFromGroup(final String[] userIds, final String groupId)
            throws RemoteException
            {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.removeUsersFromGroup(userIds, groupId);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
            }

    /**
     * Returns the number of group-members for the group with the passed ID. If
     * no group was found for the passed ID, -1 is returned.
     *
     * @param groupId - The group's ID for which the member-count is to be
     *  returned
     *
     * @return The number of group-members; -1 if no group with the passed ID
     *  was found
     *
     * @throws RemoteException
     */
    int getMemberCount(final String groupId) throws RemoteException
    {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.getMemberCount(groupId);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
    }

    /**
     * Creates a new group from the passed groupName. The group's name must not
     * be NULL or empty. Otherwise a RemoteException is thrown. The method
     * returns the ID of the newly created method as the string-representation
     * of the ID. If the returned ID is NULL, creation of the group was aborted
     * due to error(s).
     *
     * @param groupName - The name of the group to be created
     *
     * @return The ID of the newly created group. The ID is NULL if
     *  group-creation was aborted due to error(s)
     *
     * @throws RemoteException
     */
    String createNewGroup(final String groupName) throws RemoteException
    {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.createNewGroup(groupName);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
    }

    /**
     * Deletes the group with the passed group-ID, if and only if the user, who
     * is registered at the Sec2-middleware is the owner of the group.
     * Otherwise a RemoteException is thrown. Same applies if groupId is NULL.
     * So groupId must not be NULL.
     *
     * @param groupId - The ID of the group to be deleted.
     *
     * @return TRUE, if the group with the passed ID could have been
     *  successfully deleted; otherwise FALSE
     *
     * @throws RemoteException
     */
    boolean deleteGroup(final String groupId) throws RemoteException
    {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.deleteGroup(groupId);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
    }

    /**
     * Returns an array with the IDs of all apps, which have been registered at
     * the Sec2-middleware. Passing the session-token prevents, that the recent
     * session-token is seen as app-ID and returned in the array together with
     * the IDs of the registered apps.
     *
     * @param sessionToken - The recent session-token. May be NULL.
     *
     * @return An array with the IDs of all apps, which have been registered at
     *  the middleware.
     *
     * @throws RemoteException
     */
    String[] getRegisteredAppIds(final String sessionToken)
            throws RemoteException
            {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.getRegisteredAppIds(sessionToken);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
            }

    /**
     * Unregister the app wih the passed ID from the Sec2-middleware. If the ID
     * is null, nothing is done and FALSE is returned. FALSE is also returned,
     * if an error during the unregistration-process has occured. In all other
     * cases TRUE is returned.
     *
     * @param appId - The ID of the app to be unregistered
     *
     * @return FALSE, if the ID is null or if an error has occured; otherwise
     *  TRUE.
     *
     * @throws RemoteException
     */
    boolean unregisterApp(final String appId) throws RemoteException
    {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            return service.unregisterApp(appId);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
    }

    /**
     * Registers a user at the Sec2 keyserver.
     *
     * @param emailAddress - The email address of the user to register
     *
     * @throws RemoteException if an error occured during the registration
     */
    void registerUser(final String emailAddress) throws RemoteException
    {
        if (service == null)
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_AVAILABLE);
        }
        if (service.isRunning())
        {
            service.registerUser(emailAddress);
        }
        else
        {
            throw new Sec2MwServiceRemoteException(ERR_SERVICE_NOT_RUNNING);
        }
    }
}
