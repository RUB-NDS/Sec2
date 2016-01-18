package org.sec2.android;

import org.sec2.android.model.SessionKey;

interface ISec2MiddlewareService
{
	void useDbKey(String dbKey);
	void startServer(String dbKey);
	void stopServer();
	boolean isRunning();
	SessionKey getSessionKey(String oldSessionToken);
	boolean addUserToGroups(String userId, in String[] groupIds);
	boolean removeUserFromGroups(String userId, in String[] groupIds);
	boolean addUsersToGroup(in String[] userIds, String groupId);
	boolean removeUsersFromGroup(in String[] userIds, String userId);
	int getMemberCount(String groupId);
	String createNewGroup(String groupName);
	boolean deleteGroup(String groupId);
	String[] getRegisteredAppIds(String sessionToken);
	boolean unregisterApp(String appId);
        void registerUser(String emailAddress);
        void confirmUser(String challenge);
}