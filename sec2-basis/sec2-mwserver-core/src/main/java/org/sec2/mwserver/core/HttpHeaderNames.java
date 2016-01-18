package org.sec2.mwserver.core;

/**
 * Interface representing the HTTP header names.
 * @author schuessler
 *
 */
public class HttpHeaderNames
{
    /** Header-Key for the nonce-value.*/
    public static final String NONCE = "x-sec2-nonce";
    /** Header-Key for the app-name-value.*/
    public static final String APP_NAME = "x-sec2-appName";
    /** Header-Key for the group-ID-value.*/
    public static final String GROUP_ID = "x-sec2-groupId";
    /** Header-Key for the user-ID-value.*/
    public static final String USER_ID = "x-sec2-userId";
    /** Header-Key for the location-value.*/
    public static final String LOCATION = "Location";
    /** Header-Key for the socket-port-value.*/
    public static final String SOCKET_PORT = "x-sec2-socketport";
    /** Header-Key for the timestamp-value.*/
    public static final String TIMESTAMP = "x-sec2-timestamp";
    /** Header-Key for the authentication-value.*/
    public static final String AUTHENTICATION = "x-sec2-authentication";

    //Constructor is private to prohibit creation of objects
    private HttpHeaderNames(){}
}
