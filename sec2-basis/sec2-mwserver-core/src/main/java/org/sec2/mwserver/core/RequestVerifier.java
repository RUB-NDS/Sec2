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

package org.sec2.mwserver.core;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import org.sec2.extern.org.apache.commons.codec.binary.Base64;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;
import org.sec2.managers.IAppKeyManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.mwserver.core.exceptions.HandleRequestException;
import org.sec2.mwserver.core.util.HeaderComparator;
import org.sec2.mwserver.core.validators.IHeaderValidator;
import org.sec2.mwserver.core.validators.impl.AppNameHeaderValidator;
import org.sec2.mwserver.core.validators.impl.DefaultHeaderValidator;
import org.sec2.mwserver.core.validators.impl.PortHeaderValidator;
import org.sec2.mwserver.core.validators.impl.UnixTimeHeaderValidator;
import org.sec2.persistence.IPersistenceManager;
import org.sec2.persistence.PersistenceManagerContainer;

/**
 * Singleton object handling header and HMAC verifying of the incoming requests.
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @author  Nike Schüßler - nike.schuessler@rub.de
 * @version 1.0
 *
 */
public class RequestVerifier
{
    /*
     * contains all required headers except of the header field for signature
     * validation.
     */
    private static final HashMap<String, IHeaderValidator>
    REQUIRED_HEADER_FIELDS =
    new HashMap<String, IHeaderValidator>(5, 1.0f);
    /* Tolerance for timestamp*/
    private static final long TOLERANCE = 500000;

    static
    {
        REQUIRED_HEADER_FIELDS.put(HttpHeaderNames.LOCATION,
                new DefaultHeaderValidator());
        REQUIRED_HEADER_FIELDS.put(HttpHeaderNames.APP_NAME,
                new AppNameHeaderValidator());
        REQUIRED_HEADER_FIELDS.put(HttpHeaderNames.NONCE,
                new DefaultHeaderValidator());
        REQUIRED_HEADER_FIELDS.put(HttpHeaderNames.TIMESTAMP,
                new UnixTimeHeaderValidator());
        REQUIRED_HEADER_FIELDS.put(HttpHeaderNames.SOCKET_PORT,
                new PortHeaderValidator());
    }

    private RequestVerifier(){}

    /**
     * Verifies the request by mean if all required header fields are
     * available. It doesn't check the signature of the headers. This means,
     * the method does neither check, if the header field
     * x-sec2-authentication is available nor if the value of the header
     * field is correct. The freshness of the request is verified, too. This
     * means that it is verified if value of field x-sec2-timestamp is within
     * TOLERANCE and if value of vield x-sec2-nonce wasn't send before to the
     * middleware.
     *
     * @param request - The incoming request to be verified.
     *
     * @throws HandleRequestException if a required header field is missing or
     *  empty.
     */
    public static synchronized void verifyRequest(
            final IncomingRequest request) throws HandleRequestException
            {
        RequestVerifier.verifyRequest(request, true);
            }

    /**
     * Verifies the request by mean if all required header fields are
     * available. It doesn't check the signature of the headers. This means,
     * the method does neither check, if the header field
     * x-sec2-authentication is available nor if the value of the header
     * field is correct. If variable "verifyFreshness" is set to TRUE, the
     * freshness of the request is verified, too. This means that it is
     * verified if value of field x-sec2-timestamp is within TOLERANCE and if
     * value of vield x-sec2-nonce wasn't send before to the middleware.
     *
     * @param request - The incoming request to be verified.
     * @param verifyFreshness - Flag, if the freshness of the request siould be
     *  verified, too
     *
     * @throws HandleRequestException if a required header field is missing or
     *  empty.
     */
    public static synchronized void verifyRequest(
            final IncomingRequest request, final boolean verifyFreshness)
                    throws HandleRequestException
                    {
        final HashMap<String, String> requestHeaders = request.getHeaders();
        final Iterator<Entry<String, IHeaderValidator>> iter =
                REQUIRED_HEADER_FIELDS.entrySet().iterator();
        Entry<String, IHeaderValidator> requiredHeader = null;
        String requestHeaderValue = null;
        IPersistenceManager dbManager = null;
        long timestamp;
        long now;

        //Verify header fields
        while (iter.hasNext())
        {
            requiredHeader = iter.next();
            requestHeaderValue = requestHeaders.get(requiredHeader.getKey());
            REQUIRED_HEADER_FIELDS.get(requiredHeader.getKey()).validate(
                    requiredHeader.getKey(), requestHeaderValue);
        }
        //Verify freshness
        if (verifyFreshness)
        {
            //Verify timestamp
            requestHeaderValue = requestHeaders.get(HttpHeaderNames.TIMESTAMP);
            timestamp = Long.parseLong(requestHeaderValue.substring(0,
                    requestHeaderValue.indexOf("-")));
            now = new Date().getTime();
            if (timestamp < now - TOLERANCE || timestamp > now + TOLERANCE)
            {
                throw new HandleRequestException("Timestamp " + timestamp
                        + "is outside of tolerance!");
            }
            //Verify nonce
            dbManager = PersistenceManagerContainer.getPersistenceManager();
            if (dbManager == null)
            {
                throw new NullPointerException("No persistence manager could "
                        + "have been found. Reference is NULL!");
            }
            requestHeaderValue = requestHeaders.get(HttpHeaderNames.NONCE);
            if(dbManager.isNonceInDb(requestHeaderValue))
            {
                throw new HandleRequestException("Nonce " + requestHeaderValue
                        + " was already used!");
            }
            dbManager.saveNonceInDb(now, requestHeaderValue);
        }
                    }

    /**
     * Verifies the HMAC signature of the headers. So the header field
     * <x-sec2-authentication> must be available; otherwise a
     * HandleRequestException is thrown. A HandleRequestException is also
     * thrown if the signature is not valid.
     *
     * @param request - The incoming request whos signature for the header has
     *  to be verified
     *
     * @throws HandleRequestException if the header field
     *  <x-sec2-authentication> is missing or if the signature is not valid.
     * @throws ExMiddlewareException if either an unsupported key algorithm was
     *  passed or if the key itself is invalid.
     */
    public static synchronized void verifySignature(
            final IncomingRequest request)
                    throws HandleRequestException, ExMiddlewareException
                    {
        IAppKeyManager keyManager = null;
        SecretKey key = null;
        TreeMap<String, String> sortedHeaders = null;
        Entry<String, String> header = null;
        Iterator<Entry<String, String>> iter = null;
        StringBuffer headerString = null;
        Mac macCreator = null;
        String sig = null;
        final String sigToVerify = request.getHeaders().get(
                HttpHeaderNames.AUTHENTICATION);

        if (sigToVerify == null || sigToVerify.isEmpty())
        {
            throw new HandleRequestException(
                    "Ungültiger Request: Header-Feld \""
                            + HttpHeaderNames.AUTHENTICATION
                            + "\" fehlt oder ist leer.");
        }
        verifyRequest(request, false);
        keyManager = ManagerProvider.getInstance().getAppKeyManager();
        key = keyManager.getKeyForApp(request.getHeaders().get(
                HttpHeaderNames.APP_NAME));
        if (key == null)
        {
            throw new HandleRequestException(
                    "Ungültiger Request: Kein Schlüssel gefunden.");
        }
        //Sortiere Header
        sortedHeaders = new TreeMap<String, String>(new HeaderComparator());
        sortedHeaders.putAll(request.getHeaders());
        //Entferne nicht gehashte Felder. Wir haben keine direkte Kontrolle
        //ueber diese. Manche werden vom Netzwerk-Stack noch veraendert.
        // This is dirty, lieber white list!!!
        sortedHeaders.remove(HttpHeaderNames.AUTHENTICATION);
        sortedHeaders.remove("Connection");
        sortedHeaders.remove("connection");
        sortedHeaders.remove("Host");
        sortedHeaders.remove("host");
        sortedHeaders.remove("Content-Length");
        sortedHeaders.remove("content-length");
        sortedHeaders.remove("Content-Type");
        iter = sortedHeaders.entrySet().iterator();
        //Baue String für Signaturverifizierung zusammen
        headerString = new StringBuffer();
        while (iter.hasNext())
        {
            header = iter.next();
            headerString.append(header.getKey() + ":" + header.getValue()
                    + ";");
        }
        try
        {
            macCreator = Mac.getInstance(key.getAlgorithm());
            macCreator.init(key);
            sig = new String(Base64.encodeBase64(macCreator.doFinal(
                    headerString.toString().getBytes())));
            if (!sigToVerify.equals(sig))
            {
                //FIXME: Anpassen
//                throw new HandleRequestException(
//                        "Ungültiger Request: Signatur ist nicht gültig!");
            }
        }
        catch (final NoSuchAlgorithmException nsae)
        {
            throw new ExMiddlewareException("Ungültiger Schlüsselalgorithmus!",
                    nsae, LogLevel.PROBLEM);
        }
        catch (final InvalidKeyException ike)
        {
            throw new ExMiddlewareException("Ungültiger Schlüssel!", ike,
                    LogLevel.PROBLEM);
        }
                    }
}
