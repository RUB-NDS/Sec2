package de.adesso.mobile.android.sec2.mwadapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.SAXException;

import de.adesso.mobile.android.sec2.mwadapter.crypto.AppAuthKey;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.HttpConnectionException;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.MwAdapterException;
import de.adesso.mobile.android.sec2.mwadapter.exceptions.XMLParseException;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.model.User;
import de.adesso.mobile.android.sec2.mwadapter.xml.XmlHandlerGetGroup;
import de.adesso.mobile.android.sec2.mwadapter.xml.XmlHandlerGetGroups;
import de.adesso.mobile.android.sec2.mwadapter.xml.XmlHandlerGetUser;
import de.adesso.mobile.android.sec2.mwadapter.xml.XmlHandlerGetUsers;
import de.adesso.mobile.android.sec2.mwadapter.xml.XmlHandlerRedirectResponse;
import de.adesso.mobile.android.sec2.mwadapter.xml.XmlHandlerRegister;

/**
 * This is the main class of the MwAdapter library. Apps using the library can
 * access the methods of the REST interface by means of methods of this class.
 * THe class follows the singleton pattern.
 *
 * @author nike
 */
public final class MwAdapter
{
    //TODO: Muß später localhost sein
    //public static final String HOST = "10.0.2.2";
    private static final String HOST = "127.0.0.1";
    //Location-Wert für die Ansprache der REST-Schnittstelle
    private static final String REST = "REST";
    private static HttpClient client = null;
    private static MwAdapter adapter = null;

    private MwAdapter()
    {
        final HttpParams parameter = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(parameter, 10000);
        client = new DefaultHttpClient(parameter);
    }

    /**
     * This method returns an instance of this singleton class.
     * 
     * @return An instance of this class
     */
    public static MwAdapter getInstance()
    {
        if(adapter == null) adapter = new MwAdapter();

        return adapter;
    }

    /**
     * Registers the App at the middleware with the passed app name
     * 
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * 
     * @return The authentication key for the app in case the registration process was successful, otherwise NULL
     */
    public AppAuthKey register(final String appName, final int port) throws HttpConnectionException, MwAdapterException, XMLParseException
    {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerRegister registerHandler = null;
        StringBuffer message = null;
        String nonce = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/register"));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, REST, appName, port);
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 10000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            registerHandler = new XmlHandlerRegister();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), registerHandler);
            if(registerHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + registerHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < registerHandler.getErrors().size(); i++)
                {
                    message.append("\t" + registerHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(registerHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return new AppAuthKey(registerHandler.getKey(), registerHandler.getAlgorithm());
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
    }

    /**
     * Returns the user which is registered at the middleware
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * 
     * @return The user which is registered at the middleware.
     */
    public User getRegisteredUser(final String key, final String hashAlgorithm, final String appName, final int port)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerGetUser getUserHandler = null;
        StringBuffer message = null;
        String nonce = null;
        SecretKeySpec keySpec = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/usermanagement/registereduser"));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, REST, appName, port);
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 10000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            getUserHandler = new XmlHandlerGetUser();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), getUserHandler);
            if(getUserHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + getUserHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < getUserHandler.getErrors().size(); i++)
                {
                    message.append("\t" + getUserHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(getUserHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return getUserHandler.getUser();
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    /**
     * Returns an array of groups that the user can communicate with. If no group is available an empty array
     * is returned. If an error has occured, NULL is returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * 
     * @return A String array with the group names or NULL, if an error has occured.
     */
    public Group[] getGroupsForRegisteredUser(final String key, final String hashAlgorithm, final String appName, final int port)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        User user = null;

        user = getRegisteredUser(key, hashAlgorithm, appName, port);

        return getGroupsForUser(key, hashAlgorithm, appName, port, user.getUserId());
            }

    /**
     * Returns an array of users that belongs to the passed group. If no user is available an empty array
     * is returned. If an error has occured, NULL is returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param groupId - The groupId of the users to be listed
     * 
     * @return A User array with the users of the passed group or NULL, if an error has occured.
     */
    public User[] getUsersInGroup(final String key, final String hashAlgorithm, final String appName, final int port, final String groupId)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerGetUsers getUsersHandler = null;
        StringBuffer message = null;
        String nonce = null;
        SecretKeySpec keySpec = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/usermanagement/usersingrouplist"));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, REST, appName, port);
            request.setHeader("x-sec2-groupId", groupId);
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 1000000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            getUsersHandler = new XmlHandlerGetUsers();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), getUsersHandler);
            if(getUsersHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + getUsersHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < getUsersHandler.getErrors().size(); i++)
                {
                    message.append("\t" + getUsersHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(getUsersHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return getUsersHandler.getUsers();
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    /**
     * Returns the user with the passed user ID. If no user with the passed user ID was found or if an error has
     * occured, NULL is returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param userId - The userId of the requested user
     * 
     * @return The User with the passed user ID or NULL, if no user was found or if an error has occured.
     */
    public User getUser(final String key, final String hashAlgorithm, final String appName, final int port, final String userId)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerGetUser getUserHandler = null;
        StringBuffer message = null;
        String nonce = null;
        SecretKeySpec keySpec = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/usermanagement/user"));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, REST, appName, port);
            request.setHeader("x-sec2-userId", userId);
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 10000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            getUserHandler = new XmlHandlerGetUser();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), getUserHandler);
            if(getUserHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + getUserHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < getUserHandler.getErrors().size(); i++)
                {
                    message.append("\t" + getUserHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(getUserHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return getUserHandler.getUser();
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    /**
     * Returns an array of the groups where the passed user is member. If the user isn't member in any group
     * an empty array is returned. If an error has occured, NULL is returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param userId - The userId of the user which group memberships should be listed
     * 
     * @return A Group array with the groups of the passed user or NULL, if an error has occured.
     */
    public Group[] getGroupsForUser(final String key, final String hashAlgorithm, final String appName, final int port, final String userId)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerGetGroups getGroupsForUserHandler = null;
        StringBuffer message = null;
        String nonce = null;
        SecretKeySpec keySpec = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/usermanagement/groupsforuserlist"));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, REST, appName, port);
            request.setHeader("x-sec2-userId", userId);
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 1000000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            getGroupsForUserHandler = new XmlHandlerGetGroups();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), getGroupsForUserHandler);
            if(getGroupsForUserHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + getGroupsForUserHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < getGroupsForUserHandler.getErrors().size(); i++)
                {
                    message.append("\t" + getGroupsForUserHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(getGroupsForUserHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return getGroupsForUserHandler.getGroups();
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    /**
     * Returns an array of all users known to the registered user. If no user is known an empty array
     * is returned. If an error has occured, NULL is returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * 
     * @return A User array with all to the middleware known users or NULL, if an error has occured.
     */
    public User[] getAllUsers(final String key, final String hashAlgorithm, final String appName, final int port)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerGetUsers getUsersHandler = null;
        StringBuffer message = null;
        String nonce = null;
        SecretKeySpec keySpec = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/usermanagement/knownuserslist"));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, REST, appName, port);
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 1000000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            getUsersHandler = new XmlHandlerGetUsers();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), getUsersHandler);
            if(getUsersHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + getUsersHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < getUsersHandler.getErrors().size(); i++)
                {
                    message.append("\t" + getUsersHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(getUsersHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return getUsersHandler.getUsers();
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    /**
     * Sends a HTTP-PUT request to the passed URL. Returns the content of the HTTP response as input stream or NULL, if no content
     * was returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param putRequestProperties - Needed properties for the HTTP-PUT request
     * 
     * @return An input stream containing the content of the HTTP response or NULL if no content was returned.
     */
    public InputStream putRequestToUrl(final String key, final String hashAlgorithm, final String appName, final int port,
            final PutRequestProperties putRequestProperties) throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        putRequestProperties.setData(putRequestProperties.getData());
        HttpPut request = null;
        final StringBuilder sb = new StringBuilder("http://" + HOST + ":" + port);
        String nonce = null;
        SecretKeySpec keySpec = null;

        validatePutProperties(putRequestProperties);
        try
        {
            if(!putRequestProperties.getResource().startsWith("/")) sb.append("/");
            sb.append(putRequestProperties.getResource());
            request = new HttpPut(new URI(sb.toString()));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, putRequestProperties.getHost(), appName, putRequestProperties.getPort());
            request.setHeader("Content-Type", "application/xml; charset=UTF-8");
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            request.setEntity(new StringEntity(putRequestProperties.getData(), "utf-8"));

            return sendRequestToUrl(nonce, request);
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
            }

    /**
     * Sends a HTTP-GET request to the passed URL. Returns the content of the HTTP response as input stream or NULL if no content was
     * returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param getRequestProperties - Needed properties for the HTTP-GET request
     * 
     * @return An input stream containing the data of the HTTP response or NULL if no content was returned
     */
    public InputStream getRequestToUrl(final String key, final String hashAlgorithm, final String appName, final int port,
            final GetRequestProperties getRequestProperties) throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        final StringBuilder sb = new StringBuilder("http://" + HOST + ":" + port);
        String nonce = null;
        SecretKeySpec keySpec = null;

        validateProperties(getRequestProperties);
        try
        {
            if(!getRequestProperties.getResource().startsWith("/")) sb.append("/");
            sb.append(getRequestProperties.getResource());
            request = new HttpGet(new URI(sb.toString()));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, getRequestProperties.getHost(), appName, getRequestProperties.getPort());
            request.setHeader("Content-Type", "application/xml; charset=UTF-8");
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));

            return sendRequestToUrl(nonce, request);
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
            }

    /**
     * Sends a HTTP-DELETE request to the passed URL. Returns the content of the HTTP response as input stream or NULL if no content was
     * returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param deleteRequestProperties - Needed properties for the HTTP-DELETE request
     * 
     * @return An input stream containing the data of the HTTP response or NULL if no content was returned
     */
    public InputStream deleteRequestToUrl(final String key, final String hashAlgorithm, final String appName, final int port,
            final DeleteRequestProperties deleteRequestProperties) throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpDelete request = null;
        final StringBuilder sb = new StringBuilder("http://" + HOST + ":" + port);
        String nonce = null;
        SecretKeySpec keySpec = null;

        validateProperties(deleteRequestProperties);
        try
        {
            if(!deleteRequestProperties.getResource().startsWith("/")) sb.append("/");
            sb.append(deleteRequestProperties.getResource());
            request = new HttpDelete(new URI(sb.toString()));
            request.setHeader("Connection", "close");
            nonce = setHeader(request, deleteRequestProperties.getHost(), appName, deleteRequestProperties.getPort());
            request.setHeader("Content-Type", "application/xml; charset=UTF-8");
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));

            return sendRequestToUrl(nonce, request);
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
            }

    /**
     * Returns the group with the passed group ID. If no group with the passed group ID was found or if an error has
     * occured, NULL is returned.
     * 
     * @param key - The symmetric key for app authentication. The key can be gathered via the register-function.
     * @param hashAlgorithm - The hash algorithm
     * @param appName - The unique app name is used as identifier on side of the middleware
     * @param port - The port on which the middleware listens
     * @param groupId - The groupId of the requested group
     * 
     * @return The group with the passed group ID or NULL, if no group was found or if an error has occured.
     */
    public Group getGroup(final String key, final String hashAlgorithm, final String appName, final int port, final String groupId)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpGet request = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerGetGroup getGroupHandler = null;
        StringBuffer message = null;
        String nonce = null;
        SecretKeySpec keySpec = null;

        try
        {
            request = new HttpGet(new URI("http://" + HOST + ":" + port + "/usermanagement/group"));
            nonce = setHeader(request, REST, appName, port);
            request.setHeader("x-sec2-groupId", groupId);
            request.setHeader("Connection", "close");
            keySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), hashAlgorithm);
            request.setHeader("x-sec2-authentication", sign(request.getAllHeaders(), keySpec));
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != HttpStatus.SC_OK)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten");
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 10000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            getGroupHandler = new XmlHandlerGetGroup();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), getGroupHandler);
            if(getGroupHandler.isErrorOccured())
            {
                message = new StringBuffer("Es sind " + getGroupHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < getGroupHandler.getErrors().size(); i++)
                {
                    message.append("\t" + getGroupHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(message.toString());
            }
            if(!nonce.equals(getGroupHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            return getGroupHandler.getGroup();
        }
        catch(final GeneralSecurityException gse)
        {
            throw new MwAdapterException(gse);
        }
        catch(final DecoderException de)
        {
            throw new MwAdapterException(de);
        }
        catch(final URISyntaxException use)
        {
            throw new MwAdapterException(use);
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    private InputStream sendRequestToUrl(final String nonce, final HttpRequestBase request)
            throws HttpConnectionException, MwAdapterException, XMLParseException
            {
        HttpResponse response = null;
        HttpEntity entity = null;
        StatusLine statusLine = null;
        SAXParser xmlParser = null;
        XmlHandlerRedirectResponse xmlHandler = null;
        StringBuilder sb = null;

        try
        {
            request.setHeader("Connection", "close");
            response = client.execute(request);
            statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() < 200 || statusLine.getStatusCode() >= 300)
            {
                throw new HttpConnectionException(statusLine.getStatusCode(), statusLine.getReasonPhrase(),
                        "Es ist ein Fehler während der Verbindung zur Middleware aufgetreten: " + statusLine.getReasonPhrase());
            }
            entity = response.getEntity();
            if(entity == null) throw new XMLParseException("Die Antwort enthielt keinen Inhalt");
            if(entity.getContentLength() > 100000000 || entity.getContentLength() < 0)
                throw new XMLParseException("Die Antwort ist zu groß");
            xmlHandler = new XmlHandlerRedirectResponse();
            xmlParser = SAXParserFactory.newInstance().newSAXParser();
            xmlParser.parse(entity.getContent(), xmlHandler);
            if(xmlHandler.isErrorOccured())
            {
                sb = new StringBuilder("Es sind " + xmlHandler.getErrors().size() + "Fehler während des Parsens aufgetreten:\n");
                for(int i = 0; i < xmlHandler.getErrors().size(); i++)
                {
                    sb.append("\t" + xmlHandler.getErrors().get(i) + "\n");
                }
                throw new XMLParseException(sb.toString());
            }
            if(!nonce.equals(xmlHandler.getNonce())) throw new XMLParseException("Die Nonce stimmt nicht überein.");

            if(xmlHandler.getOriginalContent() == null) return null;
            else return new ByteArrayInputStream(xmlHandler.getOriginalContent());
        }
        catch(final ClientProtocolException cpe)
        {
            throw new MwAdapterException(cpe);
        }
        catch(final IOException ioe)
        {
            throw new MwAdapterException(ioe);
        }
        catch(final ParserConfigurationException pce)
        {
            throw new XMLParseException(pce.getMessage());
        }
        catch(final SAXException se)
        {
            throw new XMLParseException(se.getMessage());
        }
            }

    //Liefert eine Nonce im Format 99999999 zurück.
    private String getNonce()
    {
        final Date seed = new Date();
        final Random rng = new Random(seed.getTime());
        final StringBuffer nonce = new StringBuffer(Long.toString(rng.nextLong()));

        if(nonce.length() > 8) return nonce.substring(nonce.length() - 8);
        if(nonce.length() < 8)
        {
            while(nonce.length() < 8) nonce.insert(0, "0");
            return nonce.toString();
        }
        return nonce.toString();
    }

    //Erzeugt den Header für die Requests und liefert die aktuell verwendete Nonce zurück
    private String setHeader(final HttpMessage request, final String location, final String appName, final int port)
    {
        final String nonce = getNonce();
        final Date timestamp = new Date();

        request.setHeader("Location", location);
        request.setHeader("x-sec2-appName", appName);
        request.setHeader("x-sec2-nonce", nonce);
        request.setHeader("x-sec2-timestamp", timestamp.getTime() + "-" + timestamp.getTime());
        request.setHeader("x-sec2-socketport", Integer.toString(port));

        return nonce;
    }

    //Signiert die Headers mittels HMAC und dem key als Schlüssel. Anschließend wird die Signature Base64-kodiert zurückgeliefert.
    private String sign(final Header[] headers, final SecretKey key) throws GeneralSecurityException
    {
        final TreeSet<Header> sortedHeaders = new TreeSet<Header>(new HeaderComparator());
        final StringBuffer message = new StringBuffer();
        Iterator<Header> iter = null;
        Header header = null;
        Mac macCreator = null;
        String signature = null;

        //Sortiere Header
        for(byte i = 0; i < headers.length; i++) sortedHeaders.add(headers[i]);
        //Baue die zu signierende Message zusammen
        iter = sortedHeaders.iterator();
        while(iter.hasNext())
        {
            header = iter.next();
            message.append(header.getName() + ":" + header.getValue() + ";");
        }
        macCreator = Mac.getInstance(key.getAlgorithm());
        macCreator.init(key);
        signature = new String(Base64.encodeBase64(macCreator.doFinal(message.toString().getBytes())));

        return signature;
    }

    private void validatePutProperties(final PutRequestProperties properties) throws MwAdapterException
    {
        validateProperties(properties);
        if(properties.getData() == null) throw new MwAdapterException("Das Feld \"data\" darf nicht NULL sein");
    }

    private void validateProperties(final RequestProperties properties) throws MwAdapterException
    {
        if(properties == null) throw new MwAdapterException("Variable \"putRequestProperties\" darf nicht NULL sein");
        if(properties.getHost() == null || properties.getHost().isEmpty()) throw new MwAdapterException("Der Hostname darf nicht leer sein");
        if(properties.getHttpMethod() == HttpMethod.GET && REST.toLowerCase().equals(properties.getHost().toLowerCase()))
            throw new MwAdapterException("Der Hostname darf bei GET nicht \"REST\" sein");
        if(properties.getPort() < 0 || properties.getPort() > 65535) throw new MwAdapterException("Ungültiger Port");
        if(properties.getResource() == null || properties.getResource().isEmpty()) throw new MwAdapterException("Die Ressource darf nicht leer sein");

    }

    private class HeaderComparator implements Comparator<Header>
    {
        @Override
        public int compare(final Header o1, final Header o2)
        {
            if(o1 == null || o1.getName() == null || o1.getName().isEmpty())
            {
                if(o2 == null || o2.getName() == null || o2.getName().isEmpty()) return 0;
                else return -1;
            }
            else
            {
                if(o2 == null || o2.getName() == null || o2.getName().isEmpty()) return 1;
                else return o1.getName().compareTo(o2.getName());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        client.getConnectionManager().shutdown();
    }
}
