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
package org.sec2.mwserver.core.managerDummies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;

/**
 * A Dummy Group Manager for testing and developing purposes.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * September 03, 2013
 */
public class DummyGroupManagerImpl implements IGroupManager {

    /**
     * ...
     */
    private  DummyGroupManagerImpl() {
        createGroups();
    }

    private static DummyGroupManagerImpl instance = null;
    private static int groupId = 60;
    private LinkedList<Group> groups = null;
    LinkedList<User> users = null; //Prevent that the users get garbage collected

    public static DummyGroupManagerImpl getInstance() {
        if (instance == null) {
            instance = new DummyGroupManagerImpl();
        }

        return instance;
    }

    /**
     * {@inheritDoc} Creates a new group. The created group is not persistent in
     * this dummy-implementation. As soon as this class is reloaded, all newly
     * created groups are gone. Furthermore the implementation of this method
     * isn't thread-safe.
     */
    @Override
    public void createGroup(final Group group)
            throws ExMiddlewareException, IOException {
        group.setSynced(true); //Dirty hack...It's ok here, because it's only a dummy
        groups.add(group);
    }

    /**
     * {@inheritDoc} Updates the passed group. The update is not persistent in
     * this dummy-implementation. As soon as this class is reloaded, all updates
     * done are reverted.
     */
    @Override
    public void updateGroup(final Group group)
            throws ExMiddlewareException, IOException {
        group.setSynced(true); //Dirty hack...It's ok here, because it's only a dummy
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getGroupName().equals(group.getGroupName())) {
                groups.remove(i);
                groups.add(i, group);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revertGroupMembers(final Group group)
            throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ensureGroupKeyIsAvailable(String groupName)
            throws ExMiddlewareException, IOException {
        getGroup(groupName);
    }

    /**
     * {@inheritDoc} Removes the group with the passed group-ID. This change is
     * not persistent in this dummy-implementation. As soon as this class is
     * reloaded, all changes done are reverted.
     */
    @Override
    public void deleteGroup(final String groupName)
            throws ExMiddlewareException, IOException {
        for (int i = 0; i < groups.size(); i++) {
            if (groupName.equals(groups.get(i).getGroupName())) {
                groups.remove(i);
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getGroup(final String groupName)
            throws ExMiddlewareException, IOException {
        Group group = null;

        for (int i = 0; i < groups.size(); i++) {
            if (groupName.equals(groups.get(i).getGroupName())) {
                group = groups.get(i);
                break;
            }
        }
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getGroupsForUser(final byte[] userId)
            throws ExMiddlewareException, IOException {
        final List<String> memberships = new ArrayList<String>();
        Iterator<byte[]> iter = null;
        int pointer = 0;

        for (int i = 0; i < groups.size(); i++) {
                iter = groups.get(i).getMembers().iterator();
                while (iter.hasNext()) {
                    final byte[] user = iter.next();
                    if (Arrays.equals(user, userId)) {
                        memberships.add(groups.get(i).getGroupName());
                        break;
                    }
                }
        }
        String[] result = new String[memberships.size()];
        return memberships.toArray(result);
    }

    private void createGroups() {
        try {
            final IUserManager um = ManagerProvider.getInstance().getUserManager();
            groups = new LinkedList<Group>();

            Group g = new Group("10", um.getRegisteredUser());
            g.addMember(um.getUser(new byte[]{11}));
            g.addMember(um.getUser(new byte[]{12}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);

            g = new Group("20", um.getUser(new byte[]{20}));
            g.addMember(um.getUser(new byte[]{21}));
            g.addMember(um.getUser(new byte[]{22}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);

            g = new Group("30", um.getUser(new byte[]{30}));
            g.addMember(um.getUser(new byte[]{31}));
            g.addMember(um.getUser(new byte[]{32}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);

            g = new Group("40", um.getUser(new byte[]{40}));
            g.addMember(um.getUser(new byte[]{10}));
            g.addMember(um.getUser(new byte[]{11}));
            g.addMember(um.getUser(new byte[]{41}));
            g.addMember(um.getUser(new byte[]{42}));
            g.addMember(um.getUser(new byte[]{43}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);

            g = new Group("Studium", um.getRegisteredUser());
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);
        } catch (final Exception e) {
            e.printStackTrace(); //the most 1337 way to handle Exceptions ;)
        }
    }
}
