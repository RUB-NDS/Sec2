/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.client.connector;

import java.io.IOException;
import java.util.Collection;
import org.opensaml.xml.signature.KeyInfo;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GroupList;
import org.sec2.saml.xml.GroupResponseType;
import org.sec2.saml.xml.UserList;
import org.sec2.saml.xml.UserListType;
import org.sec2.saml.xml.UserResponseType;

/**
 * Interface for all operations where the keyserver is involved.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.4
 *
 * August 23, 2013
 */
public interface IKeyserverConnector {

    /**
     * Registers current user with a certain email address.
     *
     * @param emailAddress The email address to register
     * @return The XML element representing the user
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    UserResponseType registerUser(String emailAddress)
            throws SAMLEngineException, IOException;

    /**
     * Confirms current user with a challenge.
     *
     * @param challenge The challenge the user got somehow
     * @return the private group of the user
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupResponseType confirmUser(String challenge)
            throws SAMLEngineException, IOException;

    /**
     * Creates a new group.
     *
     * @param group The group to add
     * @return the new group
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupResponseType createGroup(Group group)
            throws SAMLEngineException, IOException;

    /**
     * Retrieves information about a certain group.
     *
     * @param groupName the name of the group that is to be queried
     * @return the group requested
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupResponseType getGroup(String groupName)
            throws SAMLEngineException, IOException;

    /**
     * Synchronizes a group with the keyserver.
     *
     * @param group the ID of the group that is to be changed
     * @return the updated group
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupResponseType updateGroup(Group group)
            throws SAMLEngineException, IOException;

    /**
     * Deletes a group from the collection of groups.
     *
     * @param groupName The name of the group to delete
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    void deleteGroup(String groupName)
            throws SAMLEngineException, IOException;

    /**
     * Requests the group key for the group provided.
     *
     * @param groupName The name of the group for which the key is to be
     * requested
     * @return the group's group key
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    KeyInfo getGroupKey(String groupName)
            throws SAMLEngineException, IOException;

    /**
     * Returns all members of a certain group.
     *
     * @param groupName The name of the group whose members are requested
     * @return A List of Users that are in this particular group, null if the
     * group could not be found. If the return value is not null, it is
     * guaranteed that the list contains at least one element.
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    UserList getGroupMembers(String groupName)
            throws SAMLEngineException, IOException;

    /**
     * Returns all groups where a user is member.
     *
     * @param userID The ID of the user whose memberships are requested
     * @return A List of Groups where the user is member of, null if the user
     * could not be found. The list may be empty, meaning that the user was
     * found, but is not member of any group
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupList getGroupsForUser(byte[] userID)
            throws SAMLEngineException, IOException;

    /**
     * Searches for a user with a certain ID.
     *
     * @param userID The userID to search for
     * @return The XML element representing the user, null if the ID could not
     * be not found
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    UserResponseType getUser(byte[] userID)
            throws SAMLEngineException, IOException;

    /**
     * Searches for a user with a certain email address.
     *
     * @param emailAddress The email address to search for
     * @return The XML element representing the user, null if the email address
     * could not be not found
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    UserResponseType getUser(String emailAddress)
            throws SAMLEngineException, IOException;

    /**
     * Adds users to a certain group.
     *
     * @param groupName The name of the group that gets new members
     * @param users The userIDs that are added to the group
     * @return the group with the added users
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupResponseType addUsersToGroup(String groupName,
            Collection<byte[]> users) throws SAMLEngineException, IOException;

    /**
     * Removes users from a certain group.
     *
     * @param groupName The name of the group that loses members
     * @param users The userIDs that are removed from the group
     * @return the group without the removed users
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    GroupResponseType removeUsersFromGroup(String groupName,
            Collection<byte[]> users) throws SAMLEngineException, IOException;

    /**
     * Synchronizes a user with the keyserver.
     *
     * @param user The user that was modified
     * @return The modified user
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    UserResponseType updateUser(User user)
            throws SAMLEngineException, IOException;

    /**
     * Retrieves all users a given user has connections to.
     *
     * @param userID The user whose connections are requested
     * @return a list of users
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    UserListType getKnownUsers(byte[] userID)
            throws SAMLEngineException, IOException;

    /**
     * Reads the registered user's public key and returns his ID.
     *
     * @return The ID of the registered User (the hash of his public key)
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     */
    byte[] getRegisteredUserID() throws EntityUnknownException;
}
