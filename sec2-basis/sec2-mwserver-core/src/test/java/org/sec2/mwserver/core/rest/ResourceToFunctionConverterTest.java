package org.sec2.mwserver.core.rest;

import junit.framework.TestCase;

import org.sec2.mwserver.core.HttpMethod;

/**
 * This JUnit-Class tests the methods of class ResourceFunctionConverter.
 *
 * @author nike
 */
public final class ResourceToFunctionConverterTest extends TestCase
{
    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.NO_REST_FUNCTION when location is abc or REST and
     * HTTP-method PUT or REST and HTTP-method GET and resource /abc
     */
    public void testConvertToFunctionNoRest()
    {
        final String location = "rest";
        final String resource = "/abc";

        assertTrue(ResourceToFunctionConverter.convertToFunction("abc",
                HttpMethod.GET, resource)
                == RestFunction.NO_REST_FUNCTION);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.PUT, "/register")
                == RestFunction.NO_REST_FUNCTION);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.NO_REST_FUNCTION);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.REGISTER when location is REST, HTTP-method GET and
     * resource /register or /register/
     */
    public void testConvertToFunctionRegister()
    {
        final String location = "rest";
        final String resource = "/register";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.REGISTER);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.REGISTER);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.GET_REGISTERED_USER when location is REST, HTTP-method GET
     * and resource /usermanagement/registereduser or
     * /usermanagement/registereduser/
     */
    public void testConvertToFunctionGetRegisteredUser()
    {
        final String location = "rest";
        final String resource = "/usermanagement/registereduser";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.GET_REGISTERED_USER);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.GET_REGISTERED_USER);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.GET_USERS_IN_GROUP when location is REST, HTTP-method GET
     * and resource /usermanagement/usersingrouplist or
     * /usermanagement/usersingrouplist/
     */
    public void testConvertToFunctionGetUsersInGroup()
    {
        final String location = "rest";
        final String resource = "/usermanagement/usersingrouplist";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.GET_USERS_IN_GROUP);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.GET_USERS_IN_GROUP);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.GET_USER when location is REST, HTTP-method GET and
     * resource /usermanagement/user or /usermanagement/user/
     */
    public void testConvertToFunctionGetUser()
    {
        final String location = "rest";
        final String resource = "/usermanagement/user";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.GET_USER);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.GET_USER);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.GET_GROUP when location is REST, HTTP-method GET and
     * resource /usermanagement/group or /usermanagement/group/
     */
    public void testConvertToFunctionGetGroup()
    {
        final String location = "rest";
        final String resource = "/usermanagement/group";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.GET_GROUP);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.GET_GROUP);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.GET_GROUPS_FOR_USER when location is REST, HTTP-method GET
     * and resource /usermanagement/groupsforuserlist or
     * /usermanagement/groupsforuserlist/
     */
    public void testConvertToFunctionGetGroupsForUser()
    {
        final String location = "rest";
        final String resource = "/usermanagement/groupsforuserlist";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.GET_GROUPS_FOR_USER);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.GET_GROUPS_FOR_USER);
    }

    /**
     * This method tests the convertToFunctionMethod()-method, if it returns
     * RestFunction.GET_KNOWN_USERS when location is REST, HTTP-method GET and
     * resource /usermanagement/knownuserslist or
     * /usermanagement/knownuserslist/
     */
    public void testConvertToFunctionGetKnownUsers()
    {
        final String location = "rest";
        final String resource = "/usermanagement/knownuserslist";

        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource)
                == RestFunction.GET_KNOWN_USERS);
        assertTrue(ResourceToFunctionConverter.convertToFunction(location,
                HttpMethod.GET, resource + "/")
                == RestFunction.GET_KNOWN_USERS);
    }
}
