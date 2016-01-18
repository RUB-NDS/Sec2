package org.sec2.mwserver.core.managerDummies;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IUserManager;
import org.sec2.managers.beans.User;

/**
 * A Dummy User Manager for testing and developing purposes.
 *
 * @author schuessler
 *
 */
public final class DummyUserManagerImpl implements IUserManager {

    private static DummyUserManagerImpl instance = null;
    private User registeredUser = null;
    private LinkedList<User> users = null;

    private DummyUserManagerImpl() {
        if (users == null) {
            createUsers();
        }
    }

    /**
     * This is a dummy user manager. It returns always the same user name, which
     * is hardcoded.
     */
    @Override
    public User getRegisteredUser() {
        if (registeredUser == null) {
            registeredUser = users.get(0);
        }
        return registeredUser;
    }

    /**
     * This is a dummy user manager. It returns always the same list of known
     * users, which is hardcoded.
     */
    @Override
    public List<byte[]> getKnownUsers() {
        final LinkedList<byte[]> userIds = new LinkedList<byte[]>();

        for (int i = 1; i < users.size(); i++) {
            userIds.add(users.get(i).getUserID());
        }

        return userIds;
    }

    /**
     * This is a dummy user manager. It uses a dirty hack to synchronise itself
     * with the GroupManager to get all available users. It doesn't check for
     * double occurencies of users.
     */
    @Override
    public User getUser(final byte[] userID)
            throws ExMiddlewareException, IOException {
        for (final User u : this.users) {
            if (Arrays.equals(u.getUserID(), userID)) {
                return u;
            }
        }
        return null;
    }

    /**
     * This is a dummy user manager. It uses a dirty hack to synchronise itself
     * with the GroupManager to get all available users. It doesn't check for
     * double occurencies of users.
     */
    @Override
    public User getUser(final String emailAddress)
            throws ExMiddlewareException, IOException {
        for (final User u : this.users) {
            if (u.getEmailAddress().equals(emailAddress)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void changeEmailAddressOfRegisteredUser(final String newEmailAddress)
            throws ExMiddlewareException, IOException {
        throw new RuntimeException("Not implemented, dont use!");
    }

    //	/**
    //	 * This is a dummy user manager. It uses a dirty hack to synchronise itself with the GroupManager
    //	 * to get all available users.
    //	 */
    //	@Override
    //	public User getUser(int userId)
    //	{
    //		User actualUser = null;
    //		Iterator<User> iter = null;
    //		IGroupManager groupManager = null;
    //		List<Group> groups = null;
    //		boolean userFound = false;
    //
    //		if (users == null) createUsers();
    //		iter = users.iterator();
    //		while(iter.hasNext())
    //		{
    //			actualUser = iter.next();
    //			if(actualUser.getUserID() == userId)
    //			{
    //				userFound = true;
    //				break;
    //			}
    //		}
    //		//If the searched user can't be found in the class' own user list, the user list of
    //		//the group manager is searched.
    //		if(!userFound)
    //		{
    //			groupManager = GroupManagerFactory.createGroupManager();
    //			groups = groupManager.getGroups();
    //			for(byte i = 0; i < groups.size(); i++)
    //			{
    //				iter = groups.get(i).getMembers().iterator();
    //				while(iter.hasNext())
    //				{
    //					actualUser = iter.next();
    //					if(actualUser.getUserID() == userId)
    //					{
    //						userFound = true;
    //						break;
    //					}
    //				}
    //				if(userFound) break;
    //			}
    //		}
    //
    //		if(userFound) return actualUser;
    //		else return null;
    //	}
    public static DummyUserManagerImpl getInstance() {
        if (instance == null) {
            instance = new DummyUserManagerImpl();
        }

        return instance;
    }

    private void createUsers() {
        users = new LinkedList<User>();
        users.add(new User(new byte[]{10}, "nike@sec2.org"));
        users.add(new User(new byte[]{11}, "lena@sec2.org"));
        users.add(new User(new byte[]{12}, "anton@sec2.org"));
        users.add(new User(new byte[]{20}, "bianca@sec2.org"));
        users.add(new User(new byte[]{21}, "charles@sec2.org"));
        users.add(new User(new byte[]{22}, "dana@sec2.org"));
        users.add(new User(new byte[]{30}, "emil@sec2.org"));
        users.add(new User(new byte[]{31}, "finja@sec2.org"));
        users.add(new User(new byte[]{32}, "gustav@sec2.org"));
        users.add(new User(new byte[]{40}, "hannah@sec2.org"));
        users.add(new User(new byte[]{41}, "ingo@sec2.org"));
        users.add(new User(new byte[]{43}, "kira@sec2.org"));
        users.add(new User(new byte[]{42}, "janine@sec2.org"));
    }
}
