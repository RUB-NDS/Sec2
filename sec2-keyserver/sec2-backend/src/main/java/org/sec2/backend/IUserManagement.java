package org.sec2.backend;

import java.security.cert.X509Certificate;
import java.util.List;
import org.sec2.backend.exceptions.ChallengeVerficationFailedException;
import org.sec2.backend.exceptions.GroupAlreadyExistsException;
import org.sec2.backend.exceptions.InvalidGroupNameException;
import org.sec2.backend.exceptions.InvalidUserPKCException;
import org.sec2.backend.exceptions.PermissionException;
import org.sec2.backend.exceptions.UserAlreadyExistsException;
import org.sec2.backend.exceptions.UserNotFoundException;

/**
 * @author Utimaco Safeware
 */
public interface IUserManagement {

    /**
     * This method creates a group.
     * 
     * @param operatorId
     *            Id of the user that creates the group.
     * @param groupName
     *            Name of the group.
     * @return If the creation of a group succeeds, an instance of IWrappedKey
     *         is returned that contains information about the created group and
     *         the wrapped groupKey. Otherwise the returned IWrappedKey contains
     *         a error message.
     * @throws UserNotFoundException
     * @throws InvalidGroupNameException
     * @throws PermissionException
     */
    public IGroupInfo createGroup(byte[] operatorId, String groupName)
    throws GroupAlreadyExistsException, 
           UserNotFoundException,
           InvalidGroupNameException, 
           PermissionException;

    /**
     * This method is used to confirm an email address.
     * 
     * @param challenge
     *            Signed challenge ([randomNumber+timestamp], SHA256withRSA).
     * @param userPkc
     *            PKC of the user (x509).
     * 
     * @return If the confirmation succeeds, an instance of IWrappedKey is
     *         returned that contains information about the created group and
     *         the wrapped groupKey. Otherwise the returned IWrappedKey contains
     *         a error message.
     * @throws PermissionException
     * @throws InvalidUserPKCException
     * 
     */
    public IGroupInfo confirmEmail(byte[] challenge,
            X509Certificate userPKC)
    throws ChallengeVerficationFailedException,
           GroupAlreadyExistsException, 
           PermissionException,
           InvalidUserPKCException;

    /**
     * This method returns the group key if the user specified by userId is
     * member of the group with the name groupName.
     * 
     * @param userId
     *            Id of the user
     * @param groupName
     *            Name of the group.
     * 
     * @return If the retrieval of the key succeeds, an instance of IWrappedKey
     *         is returned that contains information about the group the key
     *         belongs to and the wrapped groupKey. Otherwise the returned
     *         IWrappedKey contains a error message.
     * @throws UserNotFoundException
     * @throws PermissionException
     */
    public IGroupInfo getGroupInfo(byte[] userId, String groupName)
    throws UserNotFoundException, PermissionException;

    /**
     * This method is the first step to register a new user to the KeyServer.
     * 
     * @param userPKC
     *            PKC of the user.
     * 
     * @return true if the first registration step succeeds, false otherwise.
     * @throws {@link UserAlreadyExistsException} 
     * @throws {@link InvalidUserPKCException}
     */
    public boolean register(X509Certificate encryptionPKC,
            X509Certificate signaturePKC)
    throws InvalidUserPKCException, UserAlreadyExistsException;

    /**
     * Modifies a group. The group to be changed is identified by the Id given
     * by the passed IGroupInfo object.
     * 
     * @param group
     *            A IGroupInfo object with information about the group.
     * 
     * @return The changed IGroupInfo object.
     * 
     * @throws PermissionException
     */
    public IGroupInfo modifyGroup(byte[] operatorId, IGroupInfo group)
    throws PermissionException;

    /**
     * Deletes the group with the name groupName. operatorId needs to be the
     * operator of the group, otherwise a PermissionException is thrown.
     * 
     * @param operator
     *            The group operator.
     * @param groupName
     *            Name of the group to be deleted.
     * @throws PermissionException
     */
    void deleteGroup(byte[] operatorId, String groupName)
    throws PermissionException;


    /**
     * Returns an array of all users the user identified by operatorId knows.
     * 
     * @param operatorId
     *            Id of the user.
     * @return An array of all users the user knows.
     */
    public List<IUserInfo> getAllKnownUsers(byte[] operatorId);
    
    
    /**
     * Returns an array of all groups the user identified by operatorId knows.
     * 
     * @param operatorId
     *            Id of the user.
     * @return An array of all groups the user knows.
     * @throws PermissionException 
     * @throws UserNotFoundException 
     */
    public List<IGroupInfo> getAllKnownGroups(byte[] operatorId) throws UserNotFoundException, PermissionException;

    /**
     * @return unique information about the server
     */
    public IServerInfo getServerInfo();

    /**
     * Returns a user identified by email address.
     * 
     * @param emailAddress The user's email address.
     * @return The requested user
     * @throws UserNotFoundException if the user was not found
     */
    public IUserInfo getUserInfo(String emailAddress) throws UserNotFoundException;

    /**
     * Returns a user identified by id.
     * 
     * @param emailAddress The user's id.
     * @return The requested user
     * @throws UserNotFoundException if the user was not found
     */
    public IUserInfo getUserInfo(byte[] userId) throws UserNotFoundException;

    /**
     * Prepares the backend for a shutdown of the keyserver.
     */
    public void shutdown();
}