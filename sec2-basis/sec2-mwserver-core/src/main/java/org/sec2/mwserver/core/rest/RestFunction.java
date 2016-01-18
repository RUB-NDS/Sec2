package org.sec2.mwserver.core.rest;

/**
 * Enum representing the public REST functions.
 * @author schuessler
 *
 */
public enum RestFunction
{
    /** Represents the "Register"-function.*/
    REGISTER,
    /** Represents the "getRegisteredUser"-function.*/
    GET_REGISTERED_USER,
    /** Represents the "getUsersInGroup"-function.*/
    GET_USERS_IN_GROUP,
    /** Represents the "getUser"-function.*/
    GET_USER,
    /** Represents the "getGroup"-function.*/
    GET_GROUP,
    /** Represents the "getGroupsForUser"-function.*/
    GET_GROUPS_FOR_USER,
    /** Represents the "getKnownUsers"-function.*/
    GET_KNOWN_USERS,
    /** Represents no REST functions.*/
    NO_REST_FUNCTION
}
