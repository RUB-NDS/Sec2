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
package org.sec2.saml.client.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.util.Base64;
import org.sec2.exceptions.EntityUnknownException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.client.connector.IKeyserverConnector;
import org.sec2.saml.client.connector.KeyserverConnectorFactory;
import org.sec2.saml.client.engine.SecurityProviderConnectorFactory;
import org.sec2.saml.client.exceptions.SAML2MiddlewareProxyException;
import org.sec2.saml.client.exceptions.UnregisteredUserException;
import org.sec2.saml.exceptions.KeyserverException;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.sec2.saml.xml.GroupList;
import org.sec2.saml.xml.GroupResponseType;
import org.sec2.saml.xml.UserListType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager used to manage groups: create, view, delete.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.4
 *
 * August 21, 2013
 */
public final class GroupManagerImpl implements IGroupManager {

    /**
     * Class logger.
     */
    private final Logger log =
            LoggerFactory.getLogger(GroupManagerImpl.class);

    /**
     * Singleton instance.
     */
    private static IGroupManager instance;
    /**
     * The connector to communicate with the keyserver.
     */
    private final IKeyserverConnector connector;
    /**
     * A List of all groups the registered used is member of. Makes sure that a
     * group is reflected by exactly one object at a time.
     */
    private final HashMap<String, WeakReference<Group>> groups;
    /**
     * Cache of groups. Makes sure that a group is updated recently. Fetches
     * missing groups automatically.
     */
    private final LoadingCache<String, Group> groupCache;

    /**
     * @return The singleton instance of the GroupManager
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     */
    public static synchronized IGroupManager getInstance()
            throws EntityUnknownException, SAMLEngineException {
        if (instance == null) {
            instance = new GroupManagerImpl();
        }
        return instance;
    }

    /**
     * Creates a new GroupManager and intializes all fields.
     *
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     */
    private GroupManagerImpl()
            throws EntityUnknownException, SAMLEngineException {
        this.connector = KeyserverConnectorFactory.getKeyserverConnector();
        this.groups = new HashMap<String, WeakReference<Group>>();
        this.groupCache = CacheBuilder.from(
                SAMLClientConfig.MANAGERS_CACHE_CONFIG).build(
                new CacheLoader<String, Group>() {
            /**
             * Retrieves a group.
             */
            @Override
            public Group load(final String key)
                    throws SAMLEngineException, IOException,
                    EntityUnknownException {
                return fetchGroup(key);
            }
        });
    }

    /**
     * Retrieves a group by name and puts it in the map.
     *
     * @param key The group's name
     * @return The group
     * @throws SAMLEngineException if something went wrong creating or parsing a
     * SAML message.
     * @throws IOException if either no connection to the keyserver is available
     * or the keyserver detected an error.
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     */
    private Group fetchGroup(final String key) throws SAMLEngineException,
            IOException, EntityUnknownException {
        Group group;
        GroupResponseType response = connector.getGroup(key);
        if (!response.getGroupName().getValue().equals(key)) {
            throw new KeyserverException("Keyserver responded group with ID "
                    + response.getGroupName().getValue() + " when ID "
                    + key + " was requested");
        }
        Set<ByteArrayAsList> memberSet = convertXMLUserListToByteArrayAsListSet(
                response.getGroupMemberList());
        if (groups.containsKey(key)) {
            group = groups.get(key).get();
            if (group != null) {
                // group already requested before and is still in memory
                if (!group.getGroupName().equals(
                        response.getGroupName().getValue())) {
                    group.setGroupName(response.getGroupName().getValue());
                }

                for (ByteArrayAsList newMember : memberSet) {
                    if (!convertListOfByteArraysToByteArrayAsListSet(
                            group.getMembersINTERNAL()).contains(newMember)) {
                        group.getMembersINTERNAL().add(newMember.toByteArray());
                    }
                }
                for (byte[] oldMember : group.getMembersINTERNAL()) {
                    if (!memberSet.contains(new ByteArrayAsList(oldMember))) {
                        group.getMembersINTERNAL().remove(oldMember);
                    }
                }
            } else {
                group = createNewGroup(response,
                        convertCollectionOfByteArrayAsListToListOfByteArrays(
                        memberSet));
            }
        } else {
            group = createNewGroup(response,
                    convertCollectionOfByteArrayAsListToListOfByteArrays(
                    memberSet));
        }
        group.setSynced(true);
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createGroup(final Group group)
            throws SAML2MiddlewareProxyException, IOException {
        if (group.isSynced()) {
            throw new IllegalArgumentException("The group cannot be created "
                    + "because it already exists!");
        }
        GroupResponseType response;
        try {
            response = this.connector.createGroup(group);
        } catch (SAMLEngineException e) {
            e.log();
            throw new SAML2MiddlewareProxyException(e);
        }
        group.setGroupName(response.getGroupName().getValue());
        group.setSynced(true);
        groups.put(response.getGroupName().getValue(),
                new WeakReference<Group>(group));
        groupCache.put(response.getGroupName().getValue(), group);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getGroup(final String groupName)
            throws ExMiddlewareException, IOException {
        Group group;
        try {
            group = groupCache.get(groupName);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SAMLEngineException) {
                SAMLEngineException cause2 = (SAMLEngineException) cause;
                cause2.log();
                throw new SAML2MiddlewareProxyException(cause2);
            }
            if (cause instanceof EntityUnknownException) {
                throw (ExMiddlewareException) cause;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IllegalStateException(e.getCause().getClass()
                    + " should not be thrown by load(String)");
        }
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(final Group group)
            throws ExMiddlewareException, IOException {
        final String groupName = group.getGroupName();
        if (!group.isSynced()) {
            if (!groups.containsKey(groupName)) {
                LoggerFactory.getLogger(this.getClass()).warn(
                        "Group {} ('{}') could not be updated since it does "
                        + "not exist", groupName, groupName);
            }
            try {
                // update name, owner, etc.
                /* not supported by keyserver */
                // this.connector.updateGroup(group);

                // fetch members from keyserver and group as sets
                Set<ByteArrayAsList> currentMembers =
                        convertXMLUserListToByteArrayAsListSet(
                        this.connector.getGroupMembers(groupName));
                Set<ByteArrayAsList> modifiedMembers =
                        convertListOfByteArraysToByteArrayAsListSet(
                        group.getMembers());
                Set<ByteArrayAsList> removedMembers =
                        Sets.difference(currentMembers, modifiedMembers);
                Set<ByteArrayAsList> addedMembers =
                        Sets.difference(modifiedMembers, currentMembers);

                // update members on the keyserver
                if (!removedMembers.isEmpty()) {
                    this.connector.removeUsersFromGroup(groupName,
                        convertCollectionOfByteArrayAsListToListOfByteArrays(
                            removedMembers));
                }
                if (!addedMembers.isEmpty()) {
                    this.connector.addUsersToGroup(group.getGroupName(),
                        convertCollectionOfByteArrayAsListToListOfByteArrays(
                            addedMembers));
                }
            } catch (SAMLEngineException e) {
                e.log();
                throw new SAML2MiddlewareProxyException(e);
            }
            group.setSynced(true);
        } else {
            LoggerFactory.getLogger(this.getClass()).debug(
                    "Group {} could not be updated since it was not "
                    + "modified", groupName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteGroup(final String groupName)
            throws ExMiddlewareException, IOException {
        try {
            this.getGroup(groupName).setSynced(false);
            this.connector.deleteGroup(groupName);
        } catch (SAMLEngineException e) {
            e.log();
            throw new SAML2MiddlewareProxyException(e);
        } finally {
            groups.remove(groupName);
            groupCache.invalidate(groupName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getGroupsForUser(final byte[] userId)
            throws SAML2MiddlewareProxyException, IOException {
        GroupList response;
        try {
            response = this.connector.getGroupsForUser(userId);
        } catch (SAMLEngineException e) {
            e.log();
            throw new SAML2MiddlewareProxyException(e);
        }
        String[] groupNames = new String[response.getGroups().size()];
        for (int i = 0; i < response.getGroups().size(); i++) {
            groupNames[i] = response.getGroups().get(i).getValue();
        }
        return groupNames;
    }

    /**
     * Creates a new group object and adds it to the map.
     *
     * @param response The response to create the group from
     * @param members The members of the group
     * @return The group
     * @throws EntityUnknownException if the registered user cannot be
     * determined
     * @throws SAMLEngineException if something went wrong parsing the SAML
     * message.
     * @throws IOException if the current user cannot be fetched from the
     * keyserver
     */
    private Group createNewGroup(final GroupResponseType response,
            final List<byte[]> members)
            throws SAMLEngineException, EntityUnknownException, IOException {
        // group never requested before
        User owner;
        try {
            owner = UserManagerImpl.getInstance().getUserInternal(
               Base64.decode(response.getGroupOwnerID().getValue()));
        } catch (UnregisteredUserException ex) {
            log.error("Current user is not yet registered at the keyserver. "
                    + "Please initialize UserManagerImpl first and handle the "
                    + "error there.");
            throw new EntityUnknownException(ex);
        }
        Group group = new Group(response.getGroupName().getValue(), owner,
                members);
        // store group key in KeyStore
        byte[] keyData = Base64.decode(response.getGroupKey().
                getEncryptedKeys().get(0).getCipherData().
                getCipherValue().getValue());
        SecurityProviderConnectorFactory.getSecurityProviderConnector().
                storeEncryptedGroupKey(response.getGroupName().getValue(),
                keyData);

        //put group in map
        groups.put(response.getGroupName().getValue(),
                new WeakReference<Group>(group));
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ensureGroupKeyIsAvailable(final String groupName)
            throws ExMiddlewareException, IOException {
        this.getGroup(groupName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revertGroupMembers(final Group group)
            throws ExMiddlewareException, IOException {
        try {
            Set<ByteArrayAsList> members =
                    convertXMLUserListToByteArrayAsListSet(
                    this.connector.getGroupMembers(group.getGroupName()));
            if (!group.getMembersINTERNAL().isEmpty()) {
                group.getMembersINTERNAL().clear();
            }
            for (ByteArrayAsList member : members) {
                group.getMembersINTERNAL().add(member.toByteArray());
            }
        } catch (SAMLEngineException e) {
            e.log();
            throw new SAML2MiddlewareProxyException(e);
        }
    }

    /**
     * Converts a GroupMemberList into a set of byte arrays containing user IDs.
     *
     * @param xmlUserList the UserListType
     * @return a set of byte arrays containing user IDs.
     */
    private Set<ByteArrayAsList> convertXMLUserListToByteArrayAsListSet(
            final UserListType xmlUserList) {
        HashSet<ByteArrayAsList> memberSet = new HashSet<ByteArrayAsList>();
        if (xmlUserList != null) {
            for (XSBase64Binary encID : xmlUserList.getUserIDs()) {
                memberSet.add(new ByteArrayAsList(
                        Base64.decode(encID.getValue())));
            }
        }
        return memberSet;
    }

    /**
     * Simple converter.
     *
     * @param collection the source collection
     * @return the transformed list
     */
    private List<byte[]> convertCollectionOfByteArrayAsListToListOfByteArrays(
            final Collection<ByteArrayAsList> collection) {
        List<byte[]> returnList = new ArrayList(collection.size());
        for (ByteArrayAsList sublist : collection) {
            returnList.add(sublist.toByteArray());
        }
        return returnList;
    }

    /**
     * Simple converter.
     * @param list the source list
     * @return the transformed set
     */
    private Set<ByteArrayAsList> convertListOfByteArraysToByteArrayAsListSet(
            final List<byte[]> list) {
        HashSet<ByteArrayAsList> returnSet =
                new HashSet<ByteArrayAsList>(list.size());
        for (byte[] array : list) {
            returnSet.add(new ByteArrayAsList(array));
        }
        return returnSet;
    }
}
