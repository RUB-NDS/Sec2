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
package org.sec2.managers.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.ManagerProvider;

/**
 * Class representing a group of users. This class is threadsafe.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.3
 *
 * May 18, 2013
 */
public class Group implements Serializable {

    //TODO: Extract an interface IGroup and don't let every caller fiddle around
    //      with setSynced & getMembersINTERNAL()
    //      Could be done using protected modifiers, but needs a different
    //      package structure
    /**
     * Tells if the group has been synchronized with the keyserver or not.
     */
    private boolean synced;
    /**
     * The groups's name.
     */
    private String groupName;
    /**
     * The group's owner. Future extension: allow multiple owners
     */
    private final User owner;
    /**
     * The users in the group.
     */
    private final List<byte[]> members;
    /**
     * Lock to provide thread-safety.
     */
    private final ReentrantReadWriteLock lock;

    /**
     * Creates a new group. This constructor should only be used to create
     * objects for existing groups.
     *
     * @param groupName The group's name
     * @param owner The group's owner
     * @param members The group's members without the owner; no duplicates
     */
    public Group(final String groupName, final User owner,
            final List<byte[]> members) {
        super();
        if (groupName == null || owner == null || members == null) {
            throw new NullPointerException(
                    "Parameters groupName, owner and members"
                    + " must not be null!");
        }
        if (groupName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameters groupName must not be empty!");
        }

        this.synced = false;
        this.groupName = groupName;
        this.owner = owner;
        this.members = new ArrayList(members);
        this.members.add(owner.getUserID());
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Creates a new, empty group.
     *
     * @param groupName The group's name
     * @param owner The group's owner
     */
    public Group(final String groupName, final User owner) {
        this(groupName, owner, new ArrayList<byte[]>());
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        String returnGroupName;
        try {
            lock.readLock().lock();
            returnGroupName = groupName;
        } finally {
            lock.readLock().unlock();
        }
        return returnGroupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(final String groupName) {
        if (groupName == null) {
            throw new NullPointerException(
                    "Parameter groupName must not be null");
        }
        if (groupName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter groupName must not be empty");
        }
        try {
            lock.writeLock().lock();
            this.groupName = groupName;
            this.synced = false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @return the members of the group
     */
    public List<byte[]> getMembers() throws ExMiddlewareException, IOException {
        List<byte[]> returnedMemberList = null;
        try {
            lock.readLock().lock();
            if (this.members.isEmpty()) {
                ManagerProvider.getInstance().getGroupManager().
                        revertGroupMembers(this);
            }
            returnedMemberList = Collections.unmodifiableList(this.members);
        } finally {
            lock.readLock().unlock();
        }
        return returnedMemberList;
    }

    /**
     * @param user The user that should be added to this group
     * @return True, if adding the user was successful, false otherwise
     */
    public boolean addMember(final User user) {
        if (user == null) {
            throw new NullPointerException("Parameter user must not be null!");
        }
        boolean success = false;
        try {
            lock.writeLock().lock();
            if (!members.contains(getOtherArrayIfPresent(user.getUserID()))
                    && members.add(user.getUserID())) {
                this.synced = false;
                success = true;
            }
        } finally {
            lock.writeLock().unlock();
        }
        return success;
    }

    /**
     * @param user The user that is to be removed from this group
     * @return True if user was found and deleted, false otherwise
     */
    public boolean removeMember(final User user) {
        boolean success = false;
        try {
            lock.writeLock().lock();
            if (user != owner) {
                if (members.remove(getOtherArrayIfPresent(user.getUserID()))) {
                    this.synced = false;
                    success = true;
                }
            } else {
                throw new IllegalArgumentException(
                        "The group's owner cannot be removed from the group!");
            }
        } finally {
            lock.writeLock().unlock();
        }
        return success;
    }

    /**
     * It's so disturbing that array comparisons in Java are pointer-based, not
     * content-based.
     *
     * @param other another byte-array
     * @return the byte-array that's used internally, the provided array if no
     * match is found.
     */
    private byte[] getOtherArrayIfPresent(final byte[] other) {
        for (byte[] member : members) {
            if (Arrays.equals(member, other)) {
                return member;
            }
        }
        return other;
    }

    /**
     * @return the owner of the group
     */
    public User getOwner() {
        User returnOwner;
        try {
            lock.readLock().lock();
            returnOwner = owner;
        } finally {
            lock.readLock().unlock();
        }
        return returnOwner;
    }

    /**
     * Exchange the group's owner. If the new owner is not yet member of the
     * group, he is added.
     *
     * @param newOwner The new owner of the group
     * @deprecated Not supported yet
     */
    @Deprecated
    public void setOwner(final User newOwner) {
        throw new RuntimeException("Method is not supported yet");
        //        if (newOwner == null) {
        //            throw new NullPointerException(
        //                    "Parameter newOwner must not be null");
        //        }
        //        try {
        //            lock.writeLock().lock();
        //            if (!members.contains(newOwner)) {
        //                members.add(newOwner);
        //            }
        //            this.owner = newOwner;
        //            this.synced = false;
        //        } finally {
        //            lock.writeLock().unlock();
        //        }
    }

    /**
     * @return the sync status of the group
     */
    public boolean isSynced() {
        boolean returnedSynced;
        try {
            lock.readLock().lock();
            returnedSynced = synced;
        } finally {
            lock.readLock().unlock();
        }
        return returnedSynced;
    }

    /**
     * @param synced the sync status to set
     */
    public void setSynced(final boolean synced) {
        try {
            lock.writeLock().lock();
            this.synced = synced;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Don't use this method, only for internal use!!.
     *
     * @return the internal list of members
     */
    public List<byte[]> getMembersINTERNAL() {
        return this.members;
    }
}
