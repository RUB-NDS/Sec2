package org.sec2.android;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.SecretKey;

import org.sec2.android.app.ServicePreferenceActivity;
import org.sec2.android.exceptions.Sec2MwServiceRemoteException;
import org.sec2.android.exceptions.Sec2MwServiceRuntimeException;
import org.sec2.android.model.SessionKey;
import org.sec2.android.persistence.DbPersistenceManager;
import org.sec2.android.util.Constants;
import org.sec2.android.util.CryptoUtils;
import org.sec2.android.util.Sec2MiddlewarePreferenceManager;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IAppKeyManager;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.middleware.R;
import org.sec2.mwserver.core.util.NonceCleaner;
import org.sec2.persistence.PersistenceManagerContainer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sec2.configuration.ConfigurationManager;
import org.sec2.configuration.exceptions.ExConfigurationInitializationFailure;
import org.sec2.exceptions.BootstrapException;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.managers.IUserRegistrationManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.mwserver.core.MWServerCoreBootstrap;
import org.sec2.mwserver.core.managerDummies.DummyGroupManagerImpl;
import org.sec2.mwserver.core.managerDummies.DummyUserManagerImpl;
import org.sec2.mwserver.core.managerDummies.DummyUserRegistrationManagerImpl;

/**
 * This class implements the server service of the Sec2-middleware.
 *
 * @author nike
 */
public final class Sec2MiddlewareServiceImpl extends Service {

    private ISec2MiddlewareService.Stub serviceStub = null;

    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(final Intent arg0) {
        if (serviceStub == null) {
            serviceStub = new StubImpl(this);
        }

        return serviceStub;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
            final int startId) {
        String dbKey = intent.getStringExtra(Constants.INTENT_EXTRA_DB_KEY);

        if (dbKey == null) {
            dbKey = "";
        }
        try {
            serviceStub.startServer(dbKey);
        } catch (final RemoteException re) {
            throw new Sec2MwServiceRuntimeException(re);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            serviceStub.stopServer();
        } catch (final RemoteException re) {
            throw new Sec2MwServiceRuntimeException(re);
        }
    }

    /**
     * Class for handling the connection to the service.
     *
     * @author nike
     */
    public static final class Sec2MiddlewareServiceConnection
            implements ServiceConnection {

        private ISec2MiddlewareService service = null;
        private final IServiceConnectionListener listener;

        /**
         * Constructor for class Sec2MiddlewareServiceConnection. A listener
         * implementing interface IServiceConnectionListener may be passed, so
         * that the listener will be notified, when the service gets connected
         * or disconnected.
         *
         * @param listener A listener-object, implementing
         * IServiceConnectionListener. Object may be NULL.
         */
        public Sec2MiddlewareServiceConnection(
                final IServiceConnectionListener listener) {
            this.listener = listener;
        }

        @Override
        public void onServiceConnected(final ComponentName name,
                final IBinder binder) {
            service = StubImpl.asInterface(binder);
            if (listener != null) {
                listener.onServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            service = null;
            if (listener != null) {
                listener.onServiceDisconnected();
            }
        }

        /**
         * Returns the service, to which was connected.
         *
         * @return The service
         */
        public ISec2MiddlewareService getService() {
            return service;
        }
    }

    private final class StubImpl extends ISec2MiddlewareService.Stub {

        private boolean running = false;
        private NotificationManager notificationManager = null;
        private PendingIntent pendingIntent = null;
        private User registeredUser = null;
        private IUserManager userManager = null;
        private IGroupManager groupManager = null;
        private final Sec2Middleware middleware =
                Sec2Middleware.getSec2Middleware();
        private final Sec2MiddlewareServiceImpl service;
        private final Context context;
        private final Class<?> clazz = StubImpl.class;
        private static final int NOTIFICATION_SERVICE_INFO = 1000;

        private StubImpl(final Sec2MiddlewareServiceImpl service) {
            this.service = service;
            context = service.getApplicationContext();
            notificationManager =
                    (NotificationManager) (service.getSystemService(
                    Context.NOTIFICATION_SERVICE));
            pendingIntent = PendingIntent.getActivity(service, 0,
                    new Intent(service, ServicePreferenceActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        /**
         * Stops the Sec2-Middleware-Server for the REST-interface.
         *
         * @throws RemoteException
         */
        @Override
        public void stopServer() throws RemoteException {
            String message = null;

            if (running) {
                running = false;
                middleware.stopMiddlewareServer();
                message = service.getString(R.string.server_stopping);
                notificationManager.notify(NOTIFICATION_SERVICE_INFO,
                        createNotification(message));
                NonceCleaner.stopNonceCleanerTask();
                service.stopForeground(true);
            }
        }

        /**
         * Starts the Sec2-Middleware-Server for the REST-interface.
         *
         * @param dbKey - The key to open the encrypted DB, where all
         * app-authentication-keys are stored.
         *
         * @throws RemoteException
         */
        @Override
        public void startServer(final String dbKey) throws RemoteException {

            File cryptoFile = new File(this.context.getFilesDir(), "crypto-data");
            try {
                ConfigurationManager.getInstance().setConfigurationProperty_INTERNAL("org.sec2.token.swtoken.filename", cryptoFile.getPath());
            } catch (ExConfigurationInitializationFailure ex) {
                ex.printStackTrace();
                throw new RemoteException(ex.toString());
            }
            if (!cryptoFile.exists()) {
                BufferedInputStream bis = null;
                OutputStream os = null;
                final int BUF_SIZE = 8 * 1024;


                try {
                    bis = new BufferedInputStream(getAssets().open("crypto-data"));
                    os = new BufferedOutputStream(
                            new FileOutputStream(cryptoFile));
                    byte[] buf = new byte[BUF_SIZE];
                    int len;
                    while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                        os.write(buf, 0, len);
                    }
                    os.close();
                    bis.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String message = null;
            final int middlewarePort;
            final Sec2MiddlewarePreferenceManager prefManager =
                    new Sec2MiddlewarePreferenceManager(
                    getApplicationContext());

            //Create a new DbPersistenceManager-object in any case, because it
            //may be, that the key for the encrypted DB was changed
            PersistenceManagerContainer.setPersistenceManager(
                    new DbPersistenceManager(context, dbKey));

            //If the server isn't running yet, start the server
            if (!running) {

                message = service.getString(R.string.server_stop);
                service.startForeground(NOTIFICATION_SERVICE_INFO,
                        createNotification(message));

                try {
                    // run bootstraps
                    MWServerCoreBootstrap.bootstrap();

                    ManagerProvider.getInstance().setUserManager(DummyUserManagerImpl.getInstance(new File(this.context.getFilesDir(), "DummyUserManagerImpl.ser")));
                    ManagerProvider.getInstance().setGroupManager(DummyGroupManagerImpl.getInstance(new File(this.context.getFilesDir(), "DummyGroupManagerImpl.ser")));
                    ManagerProvider.getInstance().setUserRegistrationManager(DummyUserRegistrationManagerImpl.getInstance());

                    middlewarePort = prefManager.getServerListenPort();
                    middleware.startMiddlewareServer(context, "*",
                            middlewarePort);
                    running = true;
                    message = MessageFormat.format(service.getString(
                            R.string.server_start), middlewarePort);
                    notificationManager.notify(NOTIFICATION_SERVICE_INFO,
                            createNotification(message));
                    NonceCleaner.startNonceCleanerTask();
                } catch (BootstrapException ex) {
                    LogHelper.logE(clazz, ex.getMessage());
                    throw new Sec2MwServiceRemoteException(ex.getMessage());
                } catch (EntityUnknownException ex) {
                    LogHelper.logE(clazz, ex.getMessage());
                    throw new Sec2MwServiceRemoteException(ex.getMessage());
//                } catch (SAMLEngineException ex) {
//                    LogHelper.logE(clazz, ex.getMessage());
//                    throw new Sec2MwServiceRemoteException(ex.getMessage());
//                } catch (IOException ex) {
//                    LogHelper.logE(clazz, ex.getMessage());
//                    throw new Sec2MwServiceRemoteException(ex.getMessage());
//                } catch (UnregisteredUserException ex) {
//                    LogHelper.logE(clazz, ex.getMessage());
//                    throw new Sec2MwServiceRemoteException(ex.getMessage());
                } catch (ExMiddlewareException ex) {
                    LogHelper.logE(clazz, ex.getMessage());
                    throw new Sec2MwServiceRemoteException(ex.getMessage());
                }
            }
        }

        @Override
        public boolean isRunning() throws RemoteException {
            return running;
        }

        /**
         * Requests a new session-key. A session key includes a session-token.
         *
         * @param oldSessionToken - The old session token, which was used
         * previously
         *
         * @throws RemoteException
         */
        @Override
        public SessionKey getSessionKey(final String oldSessionToken)
                throws RemoteException {
            final IAppKeyManager keyManager = ManagerProvider.getInstance().getAppKeyManager();
            final CryptoUtils cryptoUtils = new CryptoUtils();
            //bytes is used as base for a seed
            byte[] bytes = Long.toString((new Date()).getTime()).getBytes();
            SecureRandom rng = null;
            String sessionToken = null;
            SecretKey keyTemp = null;

            try {
                /*Create session token*/
                //use only the last two bytes from the array
                if (bytes.length >= 2) {
                    bytes = Arrays.copyOfRange(bytes, bytes.length - 2,
                            bytes.length);
                }
                //Instantiate SecureRandom and get 2 random bytes
                rng = new SecureRandom(bytes);
                rng.nextBytes(bytes);
                //Encode the bytes hexadecimal.
                sessionToken = cryptoUtils.encodeHex(bytes, true);

                /*Create a new key for the session token*/
                keyTemp = keyManager.createRequestValidationKey(sessionToken);
                /*Delete the key for the old session token*/
                if (oldSessionToken != null && !oldSessionToken.isEmpty()) {
                    if (!keyManager.deleteKeyForApp(oldSessionToken)) {
                        LogHelper.logW(clazz, getString(
                                R.string.server_error_session_key_deletion));
                    }
                }

                /*Return the new session key*/
                return new SessionKey(cryptoUtils.encodeSecretKeyAsHex(
                        keyTemp, true), keyTemp.getAlgorithm(), sessionToken);
            } catch (final Exception e) {
                LogHelper.logW(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }
        }

        /**
         * Tells the Sec2-middleware to use the passed key for the encrypted
         * database.
         *
         * @param dbKey - The key to be used
         */
        @Override
        public void useDbKey(final String dbKey) {
            PersistenceManagerContainer.setPersistenceManager(
                    new DbPersistenceManager(context, dbKey));
        }

        /**
         * Adds the user with the passed user-ID to the groups with the passed
         * group-IDs as a member. The user-ID must be a valid user ID encoded in
         * base64. The method returns TRUE, if the method could have been
         * completed without any notification, warning or exception. This means,
         * the user could have been added to all groups with the passed IDs.
         * Otherwise it returns FALSE.
         *
         * @param userId - The user's ID, who is to be added to groups, encoded
         * in base64.
         * @param groupIds - The IDs of the groups, where the user is to be
         * added.
         *
         * @return TRUE, if the method could have been completed without any
         * notification, warning or exception. Otherwise it returns FALSE.
         *
         * @throws RemoteException
         */
        @Override
        public boolean addUserToGroups(final String userId,
                final String[] groupIds) throws RemoteException {
            return addOrRemoveUserInGroups(userId, groupIds, true);
        }

        /**
         * Removes the user with the passed user-ID from the groups with the
         * passed group-IDs as a member. The user-ID must be a valid user ID
         * encoded in base64. The method returns TRUE, if the method could have
         * been completed without any notification, warning or exception. This
         * means, the user could have been removed from all groups with the
         * passed IDs. Otherwise it returns FALSE.
         *
         * @param userId - The user's ID, who is to be added to groups, encoded
         * in base64.
         * @param groupIds - The IDs of the groups, where the user is to be
         * added.
         *
         * @return TRUE, if the method could have been completed without any
         * notification, warning or exception. Otherwise it returns FALSE.
         *
         * @throws RemoteException
         */
        @Override
        public boolean removeUserFromGroups(final String userId,
                final String[] groupIds) throws RemoteException {
            return addOrRemoveUserInGroups(userId, groupIds, false);
        }

        /**
         * Adds the users with the passed user-IDs to the group with the passed
         * group-ID as a member. The user-IDs must be valid user-IDs encoded in
         * base64. The method returns TRUE, if the method could have been
         * completed without any notification, warning or exception. This means,
         * all users with the passed Ids could have been added to the group.
         * Otherwise it returns FALSE.
         *
         * @param userIds - The IDs of the users, who are to be added to the
         * group, encoded in base64.
         * @param groupId - The ID of the group, where the users are to be
         * added.
         *
         * @return TRUE, if the method could have been completed without any
         * notification, warning or exception. Otherwise it returns FALSE.
         *
         * @throws RemoteException
         */
        @Override
        public boolean addUsersToGroup(final String[] userIds,
                final String groupId) throws RemoteException {
            return addOrRemoveUsersInGroup(userIds, groupId, true);
        }

        /**
         * Removes the users with the passed user-IDs from the group with the
         * passed group-ID as a member. The user-IDs must be valid user-IDs
         * encoded in base64. The method returns TRUE, if the method could have
         * been completed without any notification, warning or exception. This
         * means, all users with the passed IDs could have been removed from the
         * group. Otherwise it returns FALSE.
         *
         * @param userIds - The IDs of the users, who are to be added to the
         * group, encoded in base64.
         * @param groupId - The ID of the group, where the users are to be
         * added.
         *
         * @return TRUE, if the method could have been completed without any
         * notification, warning or exception. Otherwise it returns FALSE.
         *
         * @throws RemoteException
         */
        @Override
        public boolean removeUsersFromGroup(final String[] userIds,
                final String groupId) throws RemoteException {
            return addOrRemoveUsersInGroup(userIds, groupId, false);
        }

        /**
         * Returns the number of group-members for the group with the passed ID.
         * If no group was found for the passed ID, -1 is returned.
         *
         * @param groupId - The group's ID for which the member-count is to be
         * returned
         *
         * @return The number of group-members; -1 if no group with the passed
         * ID was found
         *
         * @throws RemoteException
         */
        @Override
        public int getMemberCount(final String groupId) throws RemoteException {
            Group group = null;
            int count = -1;

            if (groupId != null) {
                try {
                    initGroupManager();
                    group = groupManager.getGroup(groupId);
                    if (group != null) {
                        count = group.getMembers().size();
                    }
                } catch (final Exception e) {
                    LogHelper.logE(clazz, e.getMessage());
                    throw new Sec2MwServiceRemoteException(e.getMessage());
                }
            } else {
                LogHelper.logW(clazz, "\"groupId\" war NULL.");
            }

            return count;
        }

        /**
         * Creates a new group from the passed groupName. The group's name must
         * not be NULL or empty. Otherwise a RemoteException is thrown. The
         * method returns the ID of the newly created method as the
         * string-representation of the ID. If the returned ID is NULL, creation
         * of the group was aborted due to error(s).
         *
         * @param groupName - The name of the group to be created
         *
         * @return The ID of the newly created group. The ID is NULL if
         * group-creation was aborted due to error(s)
         *
         * @throws RemoteException
         */
        @Override
        public String createNewGroup(final String groupName)
                throws RemoteException {
            Group group = null;

            if (groupName == null || groupName.isEmpty()) {
                throw new Sec2MwServiceRemoteException(
                        "The name of the group to be created must not be NULL "
                        + "or empty!");
            }
            try {
                initUserManager();
                initGroupManager();
                group = new Group(groupName, userManager.getRegisteredUser());
                groupManager.createGroup(group);

                return group.getGroupName();
            } catch (final Exception e) {
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }
        }

        /**
         * Deletes the group with the passed group-ID, if and only if the user,
         * who is registered at the Sec2-middleware is the owner of the group.
         * Otherwise a RemoteException is thrown. Same applies if groupId is
         * NULL. So groupId must not be NULL.
         *
         * @param groupId - The ID of the group to be deleted.
         *
         * @return TRUE, if the group with the passed ID could have been
         * successfully deleted; otherwise FALSE
         *
         * @throws RemoteException
         */
        @Override
        public boolean deleteGroup(final String groupId) throws RemoteException {
            Group group = null;
            boolean success = false;

            if (groupId == null || groupId.isEmpty()) {
                throw new Sec2MwServiceRemoteException(
                        "The ID of the group must not be NULL or empty!");
            }
            try {
                if (registeredUser == null) {
                    initUserManager();
                    registeredUser = userManager.getRegisteredUser();
                }
                initGroupManager();
                group = groupManager.getGroup(groupId);
                if (group != null) {
                    if (Arrays.equals(registeredUser.getUserID(),
                            group.getOwner().getUserID())) {
                        groupManager.deleteGroup(group.getGroupName());
                        success = true;
                    } else {
                        LogHelper.logE(clazz, MessageFormat.format(
                                "Registrierter Benutzer ist nicht "
                                + "Besitzer der Gruppe mit der ID {0}",
                                group.getGroupName()));
                        notificationManager.notify(NOTIFICATION_SERVICE_INFO,
                                createNotification(MessageFormat.format(
                                getString(R.string.server_error_add_no_access),
                                group.getGroupName())));
                    }
                } else {
                    LogHelper.logE(clazz, MessageFormat.format(
                            "Es konnte keine Gruppe für die ID {0} gefunden "
                            + "werden", groupId));
                    throw new Sec2MwServiceRemoteException("Es konnte keine "
                            + "Gruppe für die übergebene ID gefunden werden");
                }

                return success;
            } catch (final Exception e) {
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }
        }

        /**
         * Returns an array with the IDs of all apps, which have been registered
         * at the Sec2-middleware. Passing the session-token prevents, that the
         * recent session-token is seen as app-ID and returned in the array
         * together with the IDs of the registered apps.
         *
         * @param sessionToken - The recent session-token. May be NULL.
         *
         * @return An array with the IDs of all apps, which have been registered
         * at the middleware.
         *
         * @throws RemoteException
         */
        @Override
        public String[] getRegisteredAppIds(final String sessionToken) {
            final IAppKeyManager keyManager =
                    ManagerProvider.getInstance().getAppKeyManager();
            String[] appIds = keyManager.getRegisteredAppIds();
            ArrayList<String> appIdsAsList = null;

            if (sessionToken != null && appIds.length > 0) {
                appIdsAsList = new ArrayList<String>(Arrays.asList(appIds));
                if (appIdsAsList.remove(sessionToken)) {
                    return appIdsAsList.toArray(
                            new String[appIdsAsList.size()]);
                } else {
                    return appIds;
                }
            } else {
                return appIds;
            }
        }

        /**
         * Registers a user at the Sec2 keyserver.
         *
         * @param emailAddress - The email address of the user to register
         *
         * @throws RemoteException if an error occured during the registration
         */
        @Override
        public void registerUser(final String emailAddress) throws RemoteException {
            final IUserRegistrationManager registerManager =
                    ManagerProvider.getInstance().getUserRegistrationManager();
            try {
                registerManager.registerUser(emailAddress);
            } catch (ExMiddlewareException e) {
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            } catch (IOException e) {
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }
        }

        /**
         * Confirms the current user's account at the keyserver.
         *
         * @param challenge the challenge the user provided
         *
         * @throws RemoteException if an error occured during the confirmation
         */
        @Override
        public void confirmUser(final String challenge) throws RemoteException {
            final IUserRegistrationManager registerManager =
                    ManagerProvider.getInstance().getUserRegistrationManager();
            try {
                registerManager.confirmUser(challenge);
            } catch (ExMiddlewareException e) {
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            } catch (IOException e) {
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }
        }

        /**
         * Unregister the app wih the passed ID from the Sec2-middleware. If the
         * ID is null, nothing is done and FALSE is returned. FALSE is also
         * returned, if an error during the unregistration-process has occured.
         * In all other cases TRUE is returned.
         *
         * @param appId - The ID of the app to be unregistered
         *
         * @return FALSE, if the ID is null or if an error has occured;
         * otherwise TRUE.
         */
        @Override
        public boolean unregisterApp(final String appId) {
            final IAppKeyManager keyManager = ManagerProvider.getInstance().getAppKeyManager();

            if (appId != null) {
                return keyManager.deleteKeyForApp(appId);
            } else {
                return false;
            }
        }

        private void initUserManager() throws ExMiddlewareException,
                IOException {
            if (userManager == null) {
                userManager = ManagerProvider.getInstance().getUserManager();
            }
        }

        private void initGroupManager() throws ExMiddlewareException,
                IOException {
            if (groupManager == null) {
                groupManager = ManagerProvider.getInstance().getGroupManager();
            }
        }

        private boolean addOrRemoveUserInGroups(final String userId,
                final String[] groupIds, final boolean add)
                throws RemoteException {
            User user = null;
            Group group = null;
            CryptoUtils cryptoUtils = null;
            boolean success = false;
            boolean overallSuccess = true;
            boolean notificationSent = false;

            if (userId == null) {
                overallSuccess = false;
                LogHelper.logE(clazz, "\"userId\" war NULL.");
                throw new Sec2MwServiceRemoteException("\"userId\" war NULL.");
            }
            if (groupIds == null) {
                overallSuccess = false;
                LogHelper.logE(clazz, "\"groupIds\" war NULL.");
                throw new Sec2MwServiceRemoteException(
                        "\"groupIds\" war NULL.");
            }
            cryptoUtils = new CryptoUtils();
            try {
                initUserManager();
                user = userManager.getUser(cryptoUtils.decodeBase64(userId));
                if (user != null) {
                    if (registeredUser == null) {
                        registeredUser = userManager.getRegisteredUser();
                    }
                    initGroupManager();
                    //Add user to groups or remove user from groups
                    for (int i = 0; i < groupIds.length; i++) {
                        group = groupManager.getGroup(groupIds[i]);
                        //If an error occur, log a warning and proceed with
                        //next group
                        if (group != null) {
                            //If and only if the registered user is the
                            //group-owner, group-modifications are allowed
                            if (Arrays.equals(registeredUser.getUserID(),
                                    group.getOwner().getUserID())) {
                                if (add) {
                                    success = group.addMember(user);
                                } else {
                                    //Don't remove a user from group if he's
                                    //the owner
                                    if (!Arrays.equals(user.getUserID(),
                                            group.getOwner().getUserID())) {
                                        success = group.removeMember(user);
                                    } else {
                                        success = false;
                                        overallSuccess = false;
                                        LogHelper.logW(clazz,
                                                MessageFormat.format(
                                                "Entfernen nicht "
                                                + "möglich. "
                                                + "Benutzer "
                                                + "ist "
                                                + "Besitzer "
                                                + "von Gruppe "
                                                + "{0}.",
                                                group
                                                .getGroupName()));
                                        notificationManager.notify(
                                                NOTIFICATION_SERVICE_INFO,
                                                createNotification(
                                                MessageFormat.format(
                                                getString(
                                                R.string.server_error_remove_owner),
                                                group
                                                .getGroupName())));
                                        notificationSent = true;
                                    }
                                }
                                if (success) {
                                    groupManager.updateGroup(group);
                                } else {
                                    overallSuccess = false;
                                    if (!notificationSent) {
                                        if (add) {
                                            LogHelper.logW(clazz,
                                                    MessageFormat.format(
                                                    "Benutzer "
                                                    + "konnte "
                                                    + "nicht "
                                                    + "zur "
                                                    + "Gruppe "
                                                    + "{0} "
                                                    + "hinzugefügt "
                                                    + "werden",
                                                    group
                                                    .getGroupName()));
                                            notificationManager.notify(
                                                    NOTIFICATION_SERVICE_INFO,
                                                    createNotification(
                                                    MessageFormat
                                                    .format(getString(
                                                    R.string.server_error_add_no_group),
                                                    group
                                                    .getGroupName())));
                                        } else {
                                            LogHelper.logW(clazz, MessageFormat
                                                    .format("Benutzer konnte "
                                                    + "nicht aus "
                                                    + "Gruppe {0} "
                                                    + "entfernt "
                                                    + "werden",
                                                    group
                                                    .getGroupName()));
                                            notificationManager.notify(
                                                    NOTIFICATION_SERVICE_INFO,
                                                    createNotification(
                                                    MessageFormat
                                                    .format(getString(
                                                    R.string.server_error_remove_no_group),
                                                    group
                                                    .getGroupName())));
                                        }
                                    }
                                }
                            } else {
                                overallSuccess = false;
                                LogHelper.logW(clazz,
                                        MessageFormat.format("Registrierter "
                                        + "Benutzer ist nicht Besitzer"
                                        + " der Gruppe mit der ID {0}",
                                        groupIds[i]));
                                if (add) {
                                    notificationManager.notify(
                                            NOTIFICATION_SERVICE_INFO,
                                            createNotification(
                                            MessageFormat.format(
                                            getString(R.string.server_error_add_no_access),
                                            group
                                            .getGroupName())));
                                } else {
                                    notificationManager.notify(
                                            NOTIFICATION_SERVICE_INFO,
                                            createNotification(
                                            MessageFormat.format(
                                            getString(R.string.server_error_remove_no_access),
                                            group
                                            .getGroupName())));
                                }
                            }
                        } else {
                            overallSuccess = false;
                            LogHelper.logW(clazz, MessageFormat.format("Es "
                                    + "konnte keine Gruppe für die ID {0} "
                                    + "gefunden werden", groupIds[i]));
                            if (add) {
                                notificationManager.notify(
                                        NOTIFICATION_SERVICE_INFO,
                                        createNotification(MessageFormat
                                        .format(getString(R.string.server_error_add_no_group),
                                        i + ".")));
                            } else {
                                notificationManager.notify(
                                        NOTIFICATION_SERVICE_INFO,
                                        createNotification(MessageFormat
                                        .format(getString(R.string.server_error_remove_no_group),
                                        i + ".")));
                            }
                        }
                    }
                } else {
                    LogHelper.logE(clazz, MessageFormat.format("Es konnte kein"
                            + " User für die ID {0} gefunden werden", userId));
                    throw new Sec2MwServiceRemoteException("Es konnte kein "
                            + "User für die übergebene ID gefunden werden");
                }
            } catch (final Exception e) {
                overallSuccess = false;
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }

            return overallSuccess;
        }

        private boolean addOrRemoveUsersInGroup(final String[] userIds,
                final String groupId, final boolean add) throws RemoteException {
            User user = null;
            Group group = null;
            CryptoUtils cryptoUtils = null;
            boolean success = false;
            boolean overallSuccess = true;
            boolean notificationSent = false;

            if (userIds == null) {
                overallSuccess = false;
                LogHelper.logE(clazz, "\"userIds\" war NULL.");
                throw new Sec2MwServiceRemoteException(
                        "\"userIds\" war NULL.");
            }
            if (groupId == null) {
                overallSuccess = false;
                LogHelper.logE(clazz, "\"groupId\" war NULL.");
                throw new Sec2MwServiceRemoteException(
                        "\"groupId\" war NULL.");
            }
            try {
                initGroupManager();
                group = groupManager.getGroup(groupId);
                if (group != null) {
                    initUserManager();
                    if (registeredUser == null) {
                        registeredUser = userManager.getRegisteredUser();
                    }
                    //If and only if the registered user is the group owner,
                    //group-modifications are allowed
                    if (Arrays.equals(registeredUser.getUserID(),
                            group.getOwner().getUserID())) {
                        cryptoUtils = new CryptoUtils();
                        //Add users to group or remove users from group
                        for (int i = 0; i < userIds.length; i++) {
                            user = userManager.getUser(
                                    cryptoUtils.decodeBase64(userIds[i]));
                            //If an error occurs, log a warning and proceed
                            //with next user
                            if (user != null) {
                                if (add) {
                                    success = group.addMember(user);
                                } else {
                                    //Don't remove a user from group if he's
                                    //the owner
                                    if (!Arrays.equals(user.getUserID(),
                                            group.getOwner().getUserID())) {
                                        success = group.removeMember(user);
                                    } else {
                                        success = false;
                                        overallSuccess = false;
                                        LogHelper.logW(clazz, MessageFormat
                                                .format("Entfernen nicht "
                                                + "möglich. Benutzer "
                                                + "ist Besitzer von "
                                                + "Gruppe {0}.",
                                                group.getGroupName()));
                                        notificationManager.notify(
                                                NOTIFICATION_SERVICE_INFO,
                                                createNotification(
                                                MessageFormat.format(
                                                getString(R.string.server_error_remove_owner),
                                                group
                                                .getGroupName())));
                                        notificationSent = true;
                                    }
                                }
                                if (success) {
                                    groupManager.updateGroup(group);
                                } else {
                                    overallSuccess = false;
                                    if (!notificationSent) {
                                        if (add) {
                                            LogHelper.logW(clazz, MessageFormat
                                                    .format("{0} Benutzer "
                                                    + "konnte nicht "
                                                    + "zur Gruppe {1} "
                                                    + "hinzugefügt "
                                                    + "werden",
                                                    i + ".", group
                                                    .getGroupName()));
                                            notificationManager.notify(
                                                    NOTIFICATION_SERVICE_INFO,
                                                    createNotification(
                                                    MessageFormat
                                                    .format(getString(
                                                    R.string.server_error_add),
                                                    i + ".",
                                                    group
                                                    .getGroupName())));
                                        } else {
                                            LogHelper.logW(clazz, MessageFormat
                                                    .format("{0} Benutzer "
                                                    + "konnte nicht "
                                                    + "aus der Gruppe "
                                                    + "{1} entfernt "
                                                    + "werden",
                                                    i + ".",
                                                    group
                                                    .getGroupName()));
                                            notificationManager.notify(
                                                    NOTIFICATION_SERVICE_INFO,
                                                    createNotification(
                                                    MessageFormat
                                                    .format(getString(
                                                    R.string.server_error_remove),
                                                    i + ".",
                                                    group
                                                    .getGroupName())));
                                        }
                                    }
                                }
                            } else {
                                overallSuccess = false;
                                LogHelper.logW(clazz, MessageFormat.format("Es"
                                        + " konnte kein Benutzer für die ID "
                                        + "{0} gefunden werden", userIds[i]));
                                if (add) {
                                    notificationManager.notify(
                                            NOTIFICATION_SERVICE_INFO,
                                            createNotification(MessageFormat
                                            .format(getString(R.string.server_error_add_no_user),
                                            i + ".")));
                                } else {
                                    notificationManager.notify(
                                            NOTIFICATION_SERVICE_INFO,
                                            createNotification(MessageFormat
                                            .format(getString(R.string.server_error_remove_no_user),
                                            i + ".")));
                                }
                            }
                        }
                    } else {
                        overallSuccess = false;
                        LogHelper.logW(clazz,
                                MessageFormat.format("Registrierter Benutzer "
                                + "ist nicht Besitzer der Gruppe mit "
                                + "der ID {0}", group.getGroupName()));
                        if (add) {
                            notificationManager.notify(
                                    NOTIFICATION_SERVICE_INFO,
                                    createNotification(
                                    MessageFormat.format(getString(
                                    R.string.server_error_add_no_access),
                                    group.getGroupName())));
                        } else {
                            notificationManager.notify(
                                    NOTIFICATION_SERVICE_INFO,
                                    createNotification(
                                    MessageFormat.format(getString(
                                    R.string.server_error_remove_no_access),
                                    group.getGroupName())));
                        }
                    }
                } else {
                    LogHelper.logE(clazz,
                            MessageFormat.format("Es konnte keine Gruppe für "
                            + "die ID {0} gefunden werden", groupId));
                    throw new Sec2MwServiceRemoteException("Es konnte keine "
                            + "Gruppe für die übergebene ID gefunden "
                            + "werden");
                }
            } catch (final Exception e) {
                overallSuccess = false;
                LogHelper.logE(clazz, e.getMessage());
                throw new Sec2MwServiceRemoteException(e.getMessage());
            }

            return overallSuccess;
        }

        private Notification createNotification(final String text) {
            final Notification notification =
                    new Notification(R.drawable.alert, text,
                    System.currentTimeMillis());

            notification.setLatestEventInfo(context,
                    getText(R.string.server_title), text, pendingIntent);

            return notification;
        }
    }
}
