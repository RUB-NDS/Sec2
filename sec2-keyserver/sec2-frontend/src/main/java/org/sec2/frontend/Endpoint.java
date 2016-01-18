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
package org.sec2.frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.opensaml.xml.io.MarshallingException;
import org.sec2.frontend.exceptions.*;
import org.sec2.saml.exceptions.SAMLEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Servlet that processes the request from the client and passes it to the SAML
 * binding class.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 26, 2013
 */
public class Endpoint extends HttpServlet {

    /**
     * use serialVersionUID for interoperability.
     */
    static final long serialVersionUID = 5525419440656596300L;

    /**
     * Reads the HTTP-POST-Body, generates a response and returns it.
     *
     * @param request {@inheritDoc}
     * @param response {@inheritDoc}
     * @throws ServletException {@inheritDoc}
     * @throws IOException {@inheritDoc}
     */
    @Override
    public final void doPost(final HttpServletRequest request,
            final HttpServletResponse response)
            throws ServletException, IOException {
        try {
            SAMLBinding binding;
            Logger log = LoggerFactory.getLogger(Endpoint.class);

            try {
                binding = SAMLBinding.getInstance();
            } catch (SAMLEngineException e) {
                e.log();
                throw new ServletException(e);
            }

            // Read POST-Body
            final String requestBody = this.readPOSTBody(request);
            log.trace("Processing request: {}", requestBody); // log request
            String responseBody;

            try {
                if (requestBody == null || requestBody.isEmpty()) {
                    // Generate error message for invalid HTTP-Body
                    this.sendResponseWithStatus(response,
                            HttpStatus.SC_BAD_REQUEST,
                            binding.generateUnsupportedErrorResponse(
                            KeyserverFrontendConfig.DEFAULT_ERROR_MSG));
                } else {
                    try {
                        // This dispatches the request to the SAML binding
                        responseBody =
                                binding.generateResponse(requestBody,
                                getRequestURL(request));
                        this.sendResponse(response, responseBody);
                    } catch (XMLProcessException e) {
                        e.log();
                        this.sendResponseWithStatus(response,
                                HttpStatus.SC_BAD_REQUEST,
                                binding.generateUnsupportedErrorResponse(
                                KeyserverFrontendConfig.DEFAULT_ERROR_MSG));
                    } catch (KeyserverSecurityException e) {
                        e.log();
                        this.sendResponseWithStatus(response,
                                HttpStatus.SC_FORBIDDEN,
                                binding.generateDeniedErrorResponse(
                                KeyserverFrontendConfig.DEFAULT_SECURITY_MSG));
                    } catch (BackendProcessException e) {
                        e.log();
                        this.processBackendProcessException(e, response,
                                binding);
                    }
                }
                // Die when encountering unrecoverable errors
            } catch (MarshallingException e) {
                log.error("XML could not be marshalled", e);
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } catch (ErrorResponseException e) {
                e.log();
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            MDC.remove(KeyserverFrontendConfig.MDC_KEY_USER);
            MDC.remove(KeyserverFrontendConfig.MDC_KEY_REQUEST);
        }
    }

    /**
     * @param request The request to extract the URL from
     * @return The URL this request was sent to
     */
    protected String getRequestURL(final HttpServletRequest request) {
        try {
            return new URL(request.getScheme(), request.getServerName(),
                    request.getServerPort(), request.getRequestURI()).
                    toString();
        } catch (MalformedURLException e) {
            LoggerFactory.getLogger(Endpoint.class).error(
                    "This is a HttpServlet and it gets requests for "
                    + "the {}-protocol?!", request.getScheme());
            return null;
        }
    }

    /**
     * Extracts the HTTP-POST-Body from a HttpServletRequest.
     *
     * @param request The HTTP-Request object
     * @return The HTTP-Body as String or null if the body could not be read
     */
    protected final String readPOSTBody(final HttpServletRequest request) {
        final StringBuilder buffer = new StringBuilder();
        String body;
        try {
            final BufferedReader reader = request.getReader();
            try {
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
            body = buffer.toString();
        } catch (IOException e) {
            body = null;
            LoggerFactory.getLogger(Endpoint.class).error(
                    "POST-Body of HTTP request could not be read", e);
        }
        return body;
    }

    /**
     * Sends a response with a specific HTTP statuscode to the client.
     *
     * @param response The HTTP-Response object used
     * @param httpCode The HTTP-Statuscode to be used
     * @param message The message to respond
     * @throws IOException if the response could not be sent
     */
    protected final void sendResponseWithStatus(
            final HttpServletResponse response, final int httpCode,
            final String message) throws IOException {
        response.setStatus(httpCode);
        this.sendResponse(response, message);
    }

    /**
     * Sends a response to the client.
     *
     * @param response The HTTP-Response object used
     * @param message The message to respond
     * @throws IOException if the response could not be sent
     */
    protected final void sendResponse(final HttpServletResponse response,
            final String message) throws IOException {
        response.setContentType("text/xml");
        LoggerFactory.getLogger(Endpoint.class).trace(
                "Returning response: {}", message);
        final PrintWriter out = response.getWriter();
        out.println(message);
        out.flush();
        out.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        SAMLBinding.unregisterProviders();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            SAMLBinding.getInstance();  //bootstraps OpenSAML, SAMLEngine, etc.
        } catch (SAMLEngineException e) {
            e.log();
            throw new ServletException(e);
        }
    }

    /**
     * Generates a response from a BackendProcessException.
     *
     * @param e the BackendProcessException
     * @param response the response object
     * @param binding the binding object
     * @throws MarshallingException if the Response could not be marshalled.
     * @throws ErrorResponseException if the Response could not be created.
     * @throws IOException if the response could not be sent.
     */
    private void processBackendProcessException(
            final BackendProcessException e, final HttpServletResponse response,
            final SAMLBinding binding) throws MarshallingException,
            ErrorResponseException, IOException {
        String result;
        int httpCode;

        switch (e.getImpact()) {
            case INVALID_INPUT:
                result = binding.generateDeniedErrorResponse(e.getMessage(),
                        e.getClientID(), e.getRequestID());
                httpCode = HttpStatus.SC_FORBIDDEN;
                break;
            case NOT_FOUND:
                result = binding.generateNotFoundResponse(e.getMessage(),
                        e.getClientID(), e.getRequestID());
                httpCode = HttpStatus.SC_NOT_FOUND;
                break;
            case PROCESSING_ERROR:
                result = binding.generateDeniedErrorResponse(e.getMessage(),
                        e.getClientID(), e.getRequestID());
                httpCode = HttpStatus.SC_SERVICE_UNAVAILABLE;
                break;
            default:
                throw new IllegalStateException("You extended the "
                        + "Impact enum without handling the new "
                        + "impact(s)");
        }
        this.sendResponseWithStatus(response, httpCode, result);
    }
}
