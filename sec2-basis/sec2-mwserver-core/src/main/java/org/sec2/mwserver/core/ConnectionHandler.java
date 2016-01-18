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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.logging.LogLevel;
import org.sec2.managers.exceptions.CreateAuthKeyException;
import org.sec2.mwserver.core.exceptions.ExServerConnectionException;
import org.sec2.mwserver.core.exceptions.HandleRequestException;
import org.sec2.mwserver.core.rest.AbstractRestFunctionExecutor;
import org.sec2.mwserver.core.rest.ResourceToFunctionConverter;
import org.sec2.mwserver.core.rest.RestFunction;
import org.sec2.mwserver.core.util.ICryptoUtils;
import org.sec2.mwserver.core.xml.XmlResponseCreator;
import org.sec2.pipeline.InputXMLProcessor;
import org.sec2.pipeline.OutputXMLProcessor;
import org.sec2.pipeline.exceptions.ExXMLProcessingException;
import org.slf4j.LoggerFactory;

/**
 * ConnectionHandler handles each incoming connection in the Sec2Middleware.
 * 
 * FIXME: one should rewrite this connection handler to use HTTP client
 * (reason: handling HTTP connections is complicated...chunked encoding etc)
 *
 * @author Juraj Somorovsky - juraj.somorovsky@rub.de
 * @author Nike Schüßler - nike.schuessler@rub.de
 * @version 1.0
 *
 */
public class ConnectionHandler implements Runnable {
    /* application socket */

    private final Socket applicationSocket;
    /*indicates that the connection is open and the XML processing is running*/
    private boolean running;
    /* Nonce for the response */
    private String nonce;
    /* server connection exception */
    private ExServerConnectionException connectionException;
    /* executor for REST functions*/
    private final AbstractRestFunctionExecutor funcExecutor;
    /* util class for common encoding and decoding methods*/
    private final ICryptoUtils cryptoUtils;
    /**
     * logger
     */
    private static org.slf4j.Logger logger =
            LoggerFactory.getLogger(ConnectionHandler.class);

    /**
     * ConnectionHandler constructor.
     *
     * @param socket - The socket of the connection
     * @param funcExecutor - The instance to be used for executing
     * REST-functions
     * @param cryptoUtils - The instance to be used for performing some
     * cryptographic functions
     */
    public ConnectionHandler(final Socket socket,
            final AbstractRestFunctionExecutor funcExecutor,
            final ICryptoUtils cryptoUtils) {
        applicationSocket = socket;
        running = true;
        this.funcExecutor = funcExecutor;
        this.cryptoUtils = cryptoUtils;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run() {

        // first, try to handle the application request. If this fails, return
        // an error to the application socket
        // Otherwise, connect to the server and wait for the response, redirect
        // it
        Socket serverSocket = null;
        try {
            serverSocket = handleRequest();
            try {
                if (serverSocket != null) {
                    handleResponseRedirect(serverSocket);
                }
            } catch (final ExXMLProcessingException ex) {
                handleError(500, "Application problem", ex.getMessage());
                connectionException = new ExServerConnectionException(
                        ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
                logger.info(ex.getLocalizedMessage(), ex);
            } catch (final IOException ex) {
                handleError(500, "Application problem", ex.getMessage());
                connectionException = new ExServerConnectionException(
                        ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
                logger.info(ex.getLocalizedMessage(), ex);
            }
        } catch (final CreateAuthKeyException ex) {
            handleError(500, "Application problem", ex.getMessage());
            connectionException = new ExServerConnectionException(
                    ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            logger.info(ex.getLocalizedMessage(), ex);
        } catch (final HandleRequestException ex) {
            handleError(400, "Bad Request", ex.getMessage());
            connectionException = new ExServerConnectionException(
                    ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            logger.info(ex.getLocalizedMessage(), ex);
        } catch (final ExMiddlewareException ex) {
            handleError(500, "Application problem", ex.getMessage());
            connectionException = new ExServerConnectionException(
                    ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            logger.info(ex.getLocalizedMessage(), ex);
        } catch (final ExServerConnectionException ex) {
            handleError(500, "Connection problem", ex.getMessage());
            connectionException = ex;
        } catch (final ExXMLProcessingException ex) {
            handleError(500, "XML parsing problem", ex.getMessage());
            connectionException = new ExServerConnectionException(
                    ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            logger.info(ex.getLocalizedMessage(), ex);
        } catch (final IOException ex) {
            handleError(500, "Connection problem", ex.getLocalizedMessage());
            connectionException = new ExServerConnectionException(
                    ex.getLocalizedMessage(), ex, LogLevel.PROBLEM);
            logger.info(ex.getLocalizedMessage(), ex);
        } finally {
            running = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                applicationSocket.close();
            } catch (final IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Handles all the incoming requests and decides what action should be
     * taken.
     *
     * @return A Socket-object if needed
     * @throws IOException
     * @throws ExXMLProcessingException
     * @throws ExMiddlewareException
     * @throws ExServerConnectionException
     */
    private Socket handleRequest() throws IOException, ExMiddlewareException,
            ExServerConnectionException, ExXMLProcessingException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(
                applicationSocket.getInputStream()));
        final IncomingRequest request =
                IncomingRequest.analyseIncomingRequest(br);
        String location = null;
        String requestedRessource = null;
        RestFunction restFunction = null;
        Socket returnSocket = null;

        logger.debug("Handling a request with the following headers: \n" + request);

        // Prüfe Request
        RequestVerifier.verifyRequest(request);
        location = request.getHeaders().get(HttpHeaderNames.LOCATION);
        requestedRessource = request.getRequestedRessource();
        if (requestedRessource == null || requestedRessource.isEmpty()) {
            throw new HandleRequestException(
                    "Ungültiger Request. Angefrage Ressource nicht genannt!");
        }
        restFunction = ResourceToFunctionConverter.convertToFunction(location,
                request.getMethod(), requestedRessource);
        switch (restFunction) {
            case NO_REST_FUNCTION:
                //Wenn der Request nicht für die REST-Schnittstelle ist, leite
                //den Request weiter
                RequestVerifier.verifySignature(request);
                nonce = request.getHeaders().get(HttpHeaderNames.NONCE);
                returnSocket = handleRequestRedirect(request);
                break;
            case REGISTER:
                // Falls die App registriert werden soll, muß der HMAC nicht
                //geprüft werden.
                funcExecutor.register(
                        request.getHeaders().get(HttpHeaderNames.APP_NAME),
                        request.getHeaders().get(HttpHeaderNames.NONCE),
                        new BufferedWriter(new OutputStreamWriter(
                        applicationSocket.getOutputStream())));
                returnSocket = null;
                break;
            default:
                RequestVerifier.verifySignature(request);
                callRestFunctions(restFunction, request);
                returnSocket = null;
        }

        return returnSocket;
    }

    private void callRestFunctions(final RestFunction restFunction,
            final IncomingRequest request)
            throws ExMiddlewareException, ExServerConnectionException,
            IOException {
        final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(applicationSocket.getOutputStream()));
        final String nonceTmp = request.getHeaders().get(HttpHeaderNames.NONCE);

        switch (restFunction) {
            case GET_REGISTERED_USER:
                funcExecutor.getRegisteredUser(nonceTmp, writer);
                break;
            case GET_USERS_IN_GROUP:
                funcExecutor.getUsersInGroup(request.getHeaders().get(
                        HttpHeaderNames.GROUP_ID), nonceTmp, writer);
                break;
            case GET_USER:
                funcExecutor.getUser(request.getHeaders().get(
                        HttpHeaderNames.USER_ID), nonceTmp, writer);
                break;
            case GET_GROUP:
                funcExecutor.getGroup(request.getHeaders().get(
                        HttpHeaderNames.GROUP_ID), nonceTmp, writer);
                break;
            case GET_GROUPS_FOR_USER:
                funcExecutor.getGroupsForUser(request.getHeaders().get(
                        HttpHeaderNames.USER_ID), nonceTmp, writer);
                break;
            case GET_KNOWN_USERS:
                funcExecutor.getKnownUsers(nonceTmp, writer);
                break;
        }
    }

    /*
     * Handles request to the XML server
     *
     * @param incomingRequest - The incoming request
     * @return
     * @throws IOException
     * @throws ExXMLProcessingException
     */
    private Socket handleRequestRedirect(final IncomingRequest incomingRequest)
            throws IOException, ExXMLProcessingException {
        String key;
        String contentType;
        StringWriter sw = null;
        OutputXMLProcessor oxp;

        logger.debug("Handling request redirect with the following content: \n"
                + incomingRequest.toString());

        final boolean contentAvailable =
                (incomingRequest.getContent() != null
                && !incomingRequest.getContent().isEmpty());
        final LinkedHashMap<String, String> headers =
                incomingRequest.getHeaders();
        final Socket serverSocket =
                new Socket(headers.get(HttpHeaderNames.LOCATION),
                Integer.parseInt(headers.get(
                HttpHeaderNames.SOCKET_PORT)));
        final BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(serverSocket.getOutputStream()));
        final StringBuilder outgoingRequest =
                new StringBuilder(incomingRequest.getMethod() + " "
                + incomingRequest.getRequestedRessource()
                + " HTTP/1.1\r\n");

        contentType = headers.get("Content-Type");
        if (contentType == null || contentType.isEmpty()) {
            contentType = headers.get("content-type");
        }
        
        // connection has to be closed in order to handle content-length etc
        headers.remove("connection");
        headers.remove("Connection");
        headers.put("Connection", "close");        
        
        //Verschlüssele ggfs. XML-Teile
        if (contentAvailable && contentType != null
                && contentType.toLowerCase().contains("xml")) {
            sw = new StringWriter();
            oxp = new OutputXMLProcessor(new StringReader(
                    incomingRequest.getContent()), sw);
            oxp.createHandlerChain();
            oxp.processXMLStream();
            sw.flush();
        }
        //Entferne alle Header, die Sec2-spezifisch sind oder neu belegt
        //werden. Alle anderen Header werden in den neuen Request übertragen
        for (final Map.Entry<String, String> header : headers.entrySet()) {
            key = header.getKey();
            if (!(HttpHeaderNames.APP_NAME.equals(key)
                    || HttpHeaderNames.GROUP_ID.equals(key)
                    || HttpHeaderNames.LOCATION.equals(key)
                    || HttpHeaderNames.NONCE.equals(key)
                    || HttpHeaderNames.SOCKET_PORT.equals(key)
                    || HttpHeaderNames.USER_ID.equals(key)
                    || HttpHeaderNames.TIMESTAMP.equals(key)
                    || HttpHeaderNames.AUTHENTICATION.equals(key)
                    || "Host".equals(key) || "host".equals(key)
                    || "Content-Length".equals(key)
                    || "content-length".equals(key))) {
                outgoingRequest.append(key + ": " + header.getValue()
                        + "\r\n");
            }
        }
        outgoingRequest.append("Host: " + headers.get(HttpHeaderNames.LOCATION)
                + ":" + headers.get(HttpHeaderNames.SOCKET_PORT) + "\r\n");
        //Füge ggfs. Content an den neue Request an
        if (contentAvailable) {
            if (sw != null && !sw.toString().isEmpty()) {
                outgoingRequest.append("Content-Length: "
                        + sw.toString().getBytes("utf-8").length + "\r\n");
                outgoingRequest.append("\r\n" + sw.toString());
            } else {
                outgoingRequest.append("Content-Length: "
                        + incomingRequest.getContent().getBytes("utf-8").length
                        + "\r\n");
                outgoingRequest.append("\r\n" + incomingRequest.getContent());
            }
        } else {
            outgoingRequest.append("\r\n");
        }
        //Sende den Request ab
        bw.write(outgoingRequest.toString());
        bw.flush();

        return serverSocket;
    }

    /**
     * Handles Response from the XML server and forwards it to the Application.
     *
     * @param serverSocket - The serverSocket-object
     * @throws IOException
     * @throws ExXMLProcessingException
     */
    private void handleResponseRedirect(final Socket serverSocket)
            throws IOException, ExXMLProcessingException {

        final BufferedReader br = new BufferedReader(new InputStreamReader(
                serverSocket.getInputStream()));
        final IncomingResponse incomingResponse = IncomingResponse.analyseIncomingResponse(br);
        br.close();
        
        logger.debug("Handling response redirect with the following content: \n" +
                incomingResponse.toString());
        
        final boolean contentAvailable =
                (incomingResponse.getContent() != null
                && !incomingResponse.getContent().isEmpty());
        BufferedWriter bw = null;
        LinkedHashMap<String, String> headers = null;
        StringWriter sw = null;
        InputXMLProcessor ixp = null;
        String contentType = null;
        String content = null;

        if (incomingResponse.getStatusCode() >= 200
                && incomingResponse.getStatusCode() < 300) {
            headers = incomingResponse.getHeaders();
            contentType = headers.get("Content-Type");
            if (contentType == null || contentType.isEmpty()) {
                contentType = headers.get("content-type");
            }

            //Cookie-Handling wird nicht unterstützt. Deswegen werden Cookies
            //entfernt
            headers.remove("Set-Cookie");
            headers.remove("set-cookie");

            //Content-Length muß neu berechnet werden...
            headers.remove("Content-Length");
            headers.remove("content-length");
            
            bw = new BufferedWriter(new OutputStreamWriter(
                    applicationSocket.getOutputStream()));
            bw.append(incomingResponse.getFullStatusLine() + "\r\n");

            //Übertrage restliche Headers in die neue XML-Resposne
            for (final Map.Entry<String, String> header : headers.entrySet()) {
                bw.append(header.getKey() + ": " + header.getValue() + "\r\n");
            }

            sw = new StringWriter();

            //Ggfs XML-Teile entschlüsseln
            if (contentAvailable && contentType != null
                    && contentType.toLowerCase().contains("/xml")) {
                ixp = new InputXMLProcessor(new StringReader(
                        incomingResponse.getContent()), sw);
                ixp.createHandlerChain();
                ixp.processXMLStream();
                sw.append("\r\n");
                logger.debug("after content parsing / decryption " + sw);
            }
            
            //Erstelle Content für die Response
            sw.flush();
            try {
                content = XmlResponseCreator.createRedirectResponse(
                        sw.toString(), nonce, cryptoUtils);
            } catch (final Exception e) {
                throw new ExXMLProcessingException(e.getMessage(), e,
                        LogLevel.PROBLEM);
            }

            //Setze Content-Length
            bw.append("Content-Length: " + content.getBytes("utf-8").length
                    + "\r\n");

            //Hänge Content ran
            bw.append("\r\n" + content);
            logger.debug("application gets from the middleware the following "
                    + "content (it is base64 encoded):\n"+ content);
            bw.flush();
            bw.close();
        } else {
            handleError(incomingResponse.getStatusCode(),
                    incomingResponse.getStatusInfo(),
                    "Error-Response from cloud service");
        }
    }

    /**
     * Returns whether the thread is running or not.
     *
     * @return TRUE, if the thread is running, otherwise false
     */
    public final boolean isRunning() {
        return running;
    }

    /**
     * Returns the exception.
     *
     * @return Returns an exception
     */
    public final ExServerConnectionException getExServerConnectionException() {
        return connectionException;
    }

    private void handleError(final int httpStatusCode,
            final String statusMessage, final String exceptionMessage) {
        BufferedWriter writer = null;
        final StringBuffer response = new StringBuffer();

        try {
            response.append("HTTP/1.1 " + httpStatusCode + " " + statusMessage
                    + "\r\n");
            response.append("Content-Length: "
                    + exceptionMessage.getBytes("utf-8").length + "\r\n");
            response.append("Content-Type: text/plain; charset=UTF-8\r\n");
            response.append("Connection: close\r\n");
            response.append("\r\n");
            response.append(exceptionMessage);
            writer = new BufferedWriter(new OutputStreamWriter(
                    applicationSocket.getOutputStream()));
            writer.write(response.toString());
            writer.flush();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
