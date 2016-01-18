/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.frontend;

import org.sec2.rest.json.Parsor;
import org.sec2.rest.backend.Cryptor;
import org.sec2.rest.backend.Backend;
import org.sec2.rest.RestException;
import org.sec2.rest.ActionType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.iharder.Base64;
import org.json.simple.JSONObject;
import org.sec2.backend.IGroupInfo;
import org.sec2.rest.*;
import org.sec2.rest.backend.Noncinator;
import org.sec2.rest.json.JSONGroupInfo;
import org.sec2.rest.json.ParsedGroupInfo;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class Endpoint extends HttpServlet {

    public final static String ACTION = "Action";
    public final static String USERID = "User-Id";
    public final static String WRPMSGKEY = "Wrapped-Msg-Key";
    public final static String NONCE = "Nonce";
    public final static String SIGNATURE = "Signature";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {
        try {
            String action = req.getHeader(ACTION);

            byte[] userId = Base64.decode(req.getHeader(USERID));

            String wrappedKey = req.getHeader(WRPMSGKEY);

            String signature = req.getHeader(SIGNATURE);

            byte[] body = doIncomingCrypto(userId, signature,
                    wrappedKey, readPOSTBody(req));

            JSONObject arguments = Parsor.parse(body);

            Noncinator.getInstance().verify((String) arguments.get(NONCE));

            JSONObject answerObj = execute(action, userId, arguments);

            byte[] plainAnswer = Parsor.deparse(answerObj);

            byte[] encryptedAnswer = doOutgoingCrypto(userId, resp, plainAnswer);

            String answer = Base64.encodeBytes(encryptedAnswer);

            sendResponseWithStatus(resp, 200, answer);



        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RestException ex) {
            ex.printStackTrace();
        }

    }

    private byte[] doIncomingCrypto(byte[] userId, String signature,
            String wrappedKey, String body)
            throws IOException, RestException {
        byte[] encryptedKey = Base64.decode(wrappedKey);

        byte[] plainKey = Cryptor.unwrap(userId, encryptedKey);

        byte[] encryptedBody = Base64.decode(body);

        byte[] plainSignature = Base64.decode(signature);

        byte[] verifiedEncryptedBody = Cryptor.verify(userId, encryptedBody,
                plainSignature);

        byte[] verifiedBody = Cryptor.decrypt(userId, verifiedEncryptedBody);

        return Cryptor.decrypt(plainKey, verifiedBody);

    }

    private byte[] doOutgoingCrypto(byte[] userId,
            HttpServletResponse resp, byte[] plainAnswer) {
        byte[] sendKey = Cryptor.generateKey();

        byte[] encryptedAnswer = Cryptor.encrypt(sendKey, plainAnswer);
        
        byte[] wrappedKey = Cryptor.wrap(userId, sendKey);

        resp.addHeader(WRPMSGKEY, Base64.encodeBytes(wrappedKey));

        byte[] encryptedAnswerSignature = Cryptor.sign(encryptedAnswer);

        resp.addHeader(SIGNATURE, Base64.encodeBytes(encryptedAnswerSignature));

        return encryptedAnswer;

    }

    private JSONObject execute(String action, byte[] verifiedUserID,
            JSONObject arguments)
            throws RestException {

        JSONObject result = null;

        switch (ActionType.valueOf(action)) {
            case CREATE_GROUP:
                String groupname = (String) arguments.get(GroupType.NAME);
                IGroupInfo group =
                        Backend.createGroup(verifiedUserID, groupname);
                result = new JSONGroupInfo(group);
                break;


            case DELETE_GROUP:
                groupname = (String) arguments.get(GroupType.NAME);
                group = Backend.createGroup(verifiedUserID, groupname);
                result = new JSONGroupInfo(group);
                break;


            case MODIFY_GROUP:
                group = new ParsedGroupInfo((JSONGroupInfo) arguments.get(GroupType.NAME));
                group = Backend.modifyGroup(verifiedUserID, group);
                result = new JSONGroupInfo(group);
                break;


            case GET_ALL_KNOWN_GROUPS:
                break;
            case GET_ALL_KNOWN_USERS:
                break;
            case GET_GROUP:
                break;


            case GET_GROUP_INFO:

                break;


            case GET_SERVER_INFO:
                break;
            case GET_USER_INFO:
                break;
            case REGISTER:
                break;


        }
        return result;
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
}
