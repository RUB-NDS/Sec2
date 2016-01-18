package org.sec2.mwserver.core.rest;

import java.util.regex.Pattern;

import org.sec2.mwserver.core.HttpMethod;

/**
 * Class for determining from the path of the requested HTTP resource if the
 * execution of a REST-function is requested and if so which REST-function is
 * requested.
 *
 * @author schuessler
 *
 */
public class ResourceToFunctionConverter
{
    // location for REST interface
    private static final String REST = "rest";
    // pattern for function "register"
    private static final Pattern REGISTER = Pattern.compile("/register/?");
    // pattern for function "getRegisteredUser"
    private static final Pattern GET_REGISTERED_USER = Pattern.compile(
            "/usermanagement/registereduser/?");
    // pattern for function "getUsersInGroup"
    private static final Pattern GET_USERS_IN_GROUP = Pattern.compile(
            "/usermanagement/usersingrouplist/?");
    // pattern for function "getUser"
    private static final Pattern GET_USER = Pattern.compile(
            "/usermanagement/user/?");
    // pattern for function "getGroup"
    private static final Pattern GET_GROUP = Pattern.compile(
            "/usermanagement/group/?");
    // pattern for function "getGroupsForUser"
    private static final Pattern GET_GROUPS_FOR_USER = Pattern.compile(
            "/usermanagement/groupsforuserlist/?");
    // pattern for function "getKnownUsers"
    private static final Pattern GET_KNOWN_USERS = Pattern.compile(
            "/usermanagement/knownuserslist/?");

    //Private constructor to avoid object creation
    private ResourceToFunctionConverter(){}

    /**
     * Converts the path of the requested HTTP resource into an enum of type
     * RestFunction representing the requested REST-function.
     *
     * @param location - value of the HTTP header field "location"
     * @param method - the used HTTP method
     * @param ressourcePath - path of the requested HTTP resource
     *
     * @return Enum of type RestFunction representing the requested
     *  REST-function
     */
    public static RestFunction convertToFunction(final String location,
            final HttpMethod method, final String ressourcePath)
    {
        if (REST.equals(location.toLowerCase()) && method == HttpMethod.GET)
        {
            if (REGISTER.matcher(ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.REGISTER;
            }
            else if (GET_REGISTERED_USER.matcher(
                    ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.GET_REGISTERED_USER;
            }
            else if (GET_USERS_IN_GROUP.matcher(
                    ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.GET_USERS_IN_GROUP;
            }
            else if (GET_USER.matcher(ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.GET_USER;
            }
            else if (GET_GROUP.matcher(ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.GET_GROUP;
            }
            else if (GET_GROUPS_FOR_USER.matcher(
                    ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.GET_GROUPS_FOR_USER;
            }
            else if (GET_KNOWN_USERS.matcher(
                    ressourcePath.toLowerCase()).matches())
            {
                return RestFunction.GET_KNOWN_USERS;
            }
            else
            {
                return RestFunction.NO_REST_FUNCTION;
            }
        }
        else
        {
            return RestFunction.NO_REST_FUNCTION;
        }
    }
}
