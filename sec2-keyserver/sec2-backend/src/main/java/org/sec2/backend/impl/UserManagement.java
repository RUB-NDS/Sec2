package org.sec2.backend.impl;

import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sec2.backend.IChallenge;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IServerInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.IUserManagement;
import org.sec2.backend.exceptions.ChallengeVerficationFailedException;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.InvalidUserPKCException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.backend.exceptions.UserNotFoundException;
import org.sec2.statictestdata.TestKeyProvider;

/**
 * Default implementation of the {@link IUserManagement} interface.
 */
public class UserManagement implements IUserManagement {

    private static final Logger LOGGER = Logger.getLogger(UserManagement.class.getName());
    
    /**
     * {@link DatabaseServer} used by this UserManagement
     */
    private DatabaseServer databaseServer;
    
    /**
     * {@link KeyServer} used by this UserManagement
     */
    private KeyServer keyServer;
    
    /**
     * {@link MailService} used by this UserManagement
     */
    private MailService mailService;

    /**
	 * Creates a new {@code UserManagement}. The passed in {@code configuration}
	 * is used to set up all needed properties.
	 * 
	 * @param configuration Configuration {@link Properties} object.
	 */
    public UserManagement(Properties configuration) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "UserManagement", 
                configuration
            );
        }
        
        this.databaseServer = new DatabaseServer(configuration);
        this.keyServer = new KeyServer(databaseServer, configuration);
        this.mailService = new MailService(configuration);
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "UserManagement"
            );
        }
    }

    /**
     * 
     * @param signaturePKC
     * @return
     */
    private byte[] calculateUserIdFromPKC(X509Certificate signaturePKC) 
            throws InvalidParameterException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "calculateUserIdFromPKC", 
                signaturePKC
            );
        }
        
        PublicKey publicKey = signaturePKC.getPublicKey();
        if (publicKey == null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Certificate has no public key");
            }
            throw new InvalidParameterException("Certificate has no public key.");
        }
        
        byte[] userId = keyServer.hash(publicKey.getEncoded());
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "calculateUserIdFromPKC", 
                userId
            );
        }
        return userId;
    }

    /**
     * Uses the keyserver to verify the challenge of the user.
     * @param challenge User challenge
     * @param signaturePKC User certificate
     * @throws ChallengeVerficationFailedException
     */
    private void checkChallenge(byte[] challenge, X509Certificate signaturePKC)
            throws ChallengeVerficationFailedException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "checkChallenge", 
                new Object[]{ challenge, signaturePKC }
            );
        }
        
        String emailAddress = MailHelper.extractEmailAddress(signaturePKC);
        if (emailAddress == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Could not find email address in certificate.");
            }
            throw new ChallengeVerficationFailedException();
        }
        IChallenge storedChallenge = databaseServer.fetchStoredChallenge(emailAddress);
        
        // new challegne verification
        if (Arrays.equals(challenge, storedChallenge.getChallenge())) {
            
        }
        // old challenge verification
        /* 
        try {
            boolean validChallenge = keyServer.verifyChallenge(
                storedChallenge, 
                challenge, 
                signaturePKC
            );
            if (!validChallenge) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Challenge verification failed!");
                }
                throw new ChallengeVerficationFailedException();
            }
        }
        catch (Exception e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(e.getMessage());
                e.printStackTrace();
            }
            throw new ChallengeVerficationFailedException();
        } finally {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.exiting(
                    UserManagement.class.getName(),
                    "checkChallenge"
                );
            }
        }*/
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "checkChallenge"
            );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IGroupInfo confirmEmail(byte[] signedChallenge, X509Certificate signaturePKC) 
        throws ChallengeVerficationFailedException,
            GroupAlreadyExistsException, 
            PermissionException,
            InvalidUserPKCException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "confirmEmail",
                new Object[]{ signedChallenge, signaturePKC }
            );
        }
        
        checkChallenge(signedChallenge, signaturePKC);
        byte[] userId = calculateUserIdFromPKC(signaturePKC);
        databaseServer.activateUser(userId);
        String emailAddress = MailHelper.extractEmailAddress(signaturePKC);
        if (emailAddress == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Could not find email address in certificate.");
            }
            throw new InvalidUserPKCException("Certificate has no email address.");
        }
        
        keyServer.generateGroupKey(emailAddress);
        databaseServer.createGroup(userId, emailAddress);
        IGroupInfo groupInfo = keyServer.getGroupInfo(userId,
                emailAddress, signaturePKC);
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "confirmEmail", 
                groupInfo
            );
        }
        return groupInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IGroupInfo createGroup(byte[] operatorId, String groupName)
            throws GroupAlreadyExistsException, 
            UserNotFoundException,
            InvalidGroupNameException, 
            PermissionException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "createGroup", 
                new Object[]{ operatorId, groupName }
            );
        }
        // TODO: group name validation for reserved group names
        if (groupName == null || groupName.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Groupname is empty or null!");
            }
            throw new InvalidGroupNameException();
        }
        if (databaseServer.groupExists(groupName)) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(String.format("Group '%s' already exists!", groupName));
            }
            throw new GroupAlreadyExistsException();
        }
        X509Certificate encryptionPKC = databaseServer.fetchEncryptionPKC(operatorId);
        keyServer.generateGroupKey(groupName);
        databaseServer.createGroup(operatorId, groupName);
        IGroupInfo encapsulatedKey = keyServer.getGroupInfo(
            operatorId,
            groupName, 
            encryptionPKC
        );
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "createGroup", 
                encapsulatedKey
            );
        }
        return encapsulatedKey;
    }

    /**
     * 
     * @param userPKC
     * @param signaturePKC
     * @param challenge
     * @return
     * @throws UserAlreadyExistsException
     * @throws InvalidUserPKCException
     */
    private long createNewUser(
        X509Certificate encryptionPKC,
        X509Certificate signaturePKC, 
        byte[] challenge
    )
    throws 
        UserAlreadyExistsException, 
        InvalidUserPKCException 
    {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "createNewUser", 
                new Object[]{ encryptionPKC, encryptionPKC, challenge }
            );
        }
        String emailAddress = MailHelper.extractEmailAddress(signaturePKC);
        byte[] userPubKey = signaturePKC.getPublicKey().getEncoded();
        byte[] userId = keyServer.hash(userPubKey);
        long timestamp = databaseServer.createNewUser(
            userId, 
            encryptionPKC, 
            signaturePKC, 
            emailAddress, 
            challenge
        );
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "createNewUser", 
                timestamp
            );
        }
        return timestamp;
    }

    

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteGroup(byte[] operatorId, String groupName)
            throws PermissionException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.entering(
                UserManagement.class.getName(),
                "deleteGroup", 
                new Object[]{ operatorId, groupName }
            );
        }
        UserInfo operator = new UserInfo();
        operator.setId(operatorId);
        String emailAddress = databaseServer.fetchEmailAddress(operatorId);
        operator.setEmailAddress(emailAddress);
        if (!databaseServer.isGroupOperator(operator, groupName)) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(String.format("Access to group '%d' denied for %s", groupName, operator));
            }
            throw new PermissionException();
        }
        databaseServer.deleteGroup(groupName);
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.exiting(
                UserManagement.class.getName(),
                "deleteGroup"
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IUserInfo> getAllKnownUsers(byte[] operatorId) {
        List<IUserInfo> users = databaseServer.getAllKnownUsers(operatorId);
        return users;
    }
    


    /**
     * {@inheritDoc}
     */
    @Override
    public IGroupInfo getGroupInfo(byte[] userId, String groupName)
    throws UserNotFoundException, PermissionException {
        // 
        if (userId == null || groupName == null) {
            
        }
        X509Certificate encryptionPKC = databaseServer.fetchEncryptionPKC(userId);
        return keyServer.getGroupInfo(userId, groupName, encryptionPKC);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IServerInfo getServerInfo() {
        // TODO: Define location of PKCs and don't use TestKeyProvider!
        // TODO: Discuss where to set/retrieve the endpoint URL (from)
        IServerInfo serverInfo = new IServerInfo() {
            
            @Override
            public X509Certificate getSignaturePKC() {
                return TestKeyProvider.getInstance().getKeyserverSignCert();
            }
            
            @Override
            public String getEndpointURL() {
                return "https://imAJarFileAndDontKnowMyEndpointURL:8080";
            }
            
            @Override
            public X509Certificate getEncryptionPKC() {
                return TestKeyProvider.getInstance().getKeyserverEncCert();
            }
        };
        return serverInfo;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public IGroupInfo modifyGroup(byte[] operatorId, IGroupInfo group) 
    throws PermissionException {
        if (group == null) {
            return null;
        }
        IUserInfo groupOperator = group.getOperator();
        if (groupOperator.getId() == null || operatorId == null) {
            //TODO: check exception type
            throw new PermissionException();
        }
        String groupName = group.getGroupName();
        UserInfo operator = new UserInfo();
        String emailAddress = databaseServer.fetchEmailAddress(operatorId);
        operator.setEmailAddress(emailAddress);
        
        if (!databaseServer.isGroupOperator(operator, groupName)) {
            throw new PermissionException();
        }
        return databaseServer.updateGroup(group);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean register(X509Certificate encryptionPKC,
            X509Certificate signaturePKC)
    throws InvalidUserPKCException, 
           UserAlreadyExistsException {
        // Check input params
        if (encryptionPKC == null || signaturePKC == null) {
            throw new InvalidUserPKCException();
        }
        
        try {
            byte[] challenge = keyServer.generateChallenge();
            long timestamp = createNewUser(encryptionPKC, signaturePKC, challenge);
            // here is where the magic happens
            String emailAddress = MailHelper.extractEmailAddress(signaturePKC);
            MailHelper.sendChallengeToUser(mailService, emailAddress, challenge, timestamp);
        }
        catch (InvalidUserPKCException e) {
            throw e;
        }
        catch (UserAlreadyExistsException e) {
            throw e;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUserInfo getUserInfo(String emailAddress) throws UserNotFoundException {
        if(emailAddress == null || emailAddress.isEmpty()) {
            throw new UserNotFoundException();
        }
        IUserInfo userInfo = databaseServer.fetchUserInfoByEmailAddress(emailAddress);
        return userInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUserInfo getUserInfo(byte[] userID) throws UserNotFoundException {
        if(userID == null || userID.length == 0) {
            throw new UserNotFoundException();
        }
        IUserInfo userInfo = databaseServer.fetchUserInfoByID(userID);
        return userInfo;
    }

    /**
     * {@inheritDoc}
     * @throws PermissionException 
     * @throws UserNotFoundException 
     */
    @Override
    public List<IGroupInfo> getAllKnownGroups(byte[] operatorId) throws UserNotFoundException, PermissionException {
        List<IGroupInfo> groups = keyServer.getAllKnownGroups(operatorId);
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        databaseServer.shutdown();
        Security.removeProvider("CryptoServer"); //was added by Sec2Provider
    }
}
