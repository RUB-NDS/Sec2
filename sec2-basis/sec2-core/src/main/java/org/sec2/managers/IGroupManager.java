/*
 * Copyright 2011 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.managers;

import java.io.IOException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.beans.Group;

/**
 * Manager Interface used to manage groups: create, view, delete.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.3
 *
 * May 18, 2013
 */
public interface IGroupManager {

    /**
     * Adds a group to the collection of groups.
     *
     * @param group The group to add
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    void createGroup(Group group)
            throws ExMiddlewareException, IOException;

    /**
     * Returns information about a group.
     *
     * @param groupName The name of the group
     * @return A group object that corresponds to the provided ID
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    Group getGroup(String groupName)
            throws ExMiddlewareException, IOException;

    /**
     * Synchronizes modifications of a group with the keyserver.
     *
     * @param group The group that was modified
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    void updateGroup(Group group)
            throws ExMiddlewareException, IOException;

    /**
     * Deletes a group from the collection of groups.
     *
     * @param groupName The name of the group to delete
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    void deleteGroup(String groupName)
            throws ExMiddlewareException, IOException;

    /**
     * Returns all groups where a user is member.
     *
     * @param userId - The user's id whose memberships are requested
     * @return An array of Group names where the user is member of
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    String[] getGroupsForUser(byte[] userId)
            throws ExMiddlewareException, IOException;

    /**
     * Makes sure, that the group key of a group is available through the sec2
     * security provider.
     *
     * @param groupName The name of the group
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    void ensureGroupKeyIsAvailable(String groupName)
            throws ExMiddlewareException, IOException;

    /**
     * Overwrites the group's member list with the list from the keyserver.
     *
     * @param group The group to revert
     * @throws ExMiddlewareException if something went wrong creating or parsing
     * a SAML message.
     * @throws IOException if no connection to the keyserver is available.
     */
    void revertGroupMembers(Group group)
            throws ExMiddlewareException, IOException;
}
