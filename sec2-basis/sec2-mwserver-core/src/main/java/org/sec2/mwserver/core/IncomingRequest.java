package org.sec2.mwserver.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.sec2.mwserver.core.exceptions.HandleRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class analyses an incoming HTTP-request and splits it up into its
 * component-parts.
 *
 * @author nike
 */
public class IncomingRequest {

    private HttpMethod method = null;
    private String requestedRessource = null;
    private String originalRequest = null;
    private String content = null;
    private LinkedHashMap<String, String> headers = null;
    private static final String ERR_LINE_NOT_ANALYZED =
            "Zeile {0} konnte nicht analysiert werden: {1}";
    private static Logger logger = LoggerFactory.getLogger(IncomingRequest.class);

    private IncomingRequest() {
    }

    ;

    /**
     * Analyses the incoming request.
     *
     * @param reader - The reader for the input stream
     * @return the analysed request
     * @throws IOException
     */
    public static IncomingRequest analyseIncomingRequest(
            final BufferedReader reader) throws IOException {
        final IncomingRequest request = new IncomingRequest();
        final StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        String[] temp = line.split("\\s");
        final LinkedHashMap<String, String> headerMap =
                new LinkedHashMap<String, String>();
        int linesRead = 1;

        if (temp.length >= 2) {
            //Prüfen, welche HTTP-Request-Methode benutzt wurde
            temp[0] = temp[0].toUpperCase();
            if (temp[0].equals("GET")) {
                request.method = HttpMethod.GET;
            } else if (temp[0].equals("PUT")) {
                request.method = HttpMethod.PUT;
            } else if (temp[0].equals("DELETE")) {
                request.method = HttpMethod.DELETE;
            } else {
                throw new HandleRequestException(
                        MessageFormat.format(ERR_LINE_NOT_ANALYZED, linesRead,
                        line));
            }

            //Die angeforderte Ressource extrahieren
            if (!temp[1].isEmpty()) {
                request.requestedRessource = temp[1];
            } else {
                throw new HandleRequestException(
                        MessageFormat.format(ERR_LINE_NOT_ANALYZED, linesRead,
                        line));
            }

            sb.append(line);

            //Restliche Header auslesen
            while ((line = reader.readLine()) != null) {
                //Falls eine Leerzeile kommt, ist der Headerbereich zuende und
                //der Content kann gelesen werden
                if (line.isEmpty()) {
                    if (request.getMethod() == HttpMethod.PUT) {
                        readContent(reader, sb, request, headerMap.get("Content-Length"));
                    }
                    break;
                }
                linesRead++;
                temp = line.split(":\\s", 2);
                if (temp.length == 2) {
                    headerMap.put(temp[0], temp[1]);
                    sb.append("\n" + line);
                    logger.debug("header map adding: " + temp[0] + ":" + temp[1]);
                } else {
                    throw new HandleRequestException(
                            MessageFormat.format(ERR_LINE_NOT_ANALYZED,
                            linesRead, line));
                }
            }
            request.headers = headerMap;
            request.originalRequest = sb.toString();
        } else {
            throw new HandleRequestException(
                    MessageFormat.format(ERR_LINE_NOT_ANALYZED, linesRead,
                    line));
        }

        return request;
    }

    /**
     * Returns the HTTP-method of the incoming request.
     *
     * @return The HTTP-method of the incoming request
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Returns the requested resource of the incoming request.
     *
     * @return The requested resource of the incoming request
     */
    public String getRequestedRessource() {
        return requestedRessource;
    }

    /**
     * Returns the original/raw request.
     *
     * @return The original/raw request
     */
    public String getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Returns a hashmap of the HTTP-request-headers.
     *
     * @return A hashmap of the HTTP-request-headers
     */
    public LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the HTTP-request's content if available. Otherwise returns NULL.
     *
     * @return The HTTP-request's content. May return NULL
     */
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getOriginalRequest();
    }

    /**
     * TODO this is very very ugly
     *
     * @param reader
     * @param sb
     * @param request
     * @param headerContentLength
     * @throws IOException
     */
    private static void readContent(final BufferedReader reader,
            final StringBuilder sb, final IncomingRequest request,
            final String headerContentLength)
            throws IOException {

        int contentLength = -1;
        List<Byte> byteList = null;

        try {
            // we remove puctations etc from the content length string so we 
            // can parse it
            String cs = "";
            if (headerContentLength != null) {
                cs = headerContentLength.replace(",", "");
                cs = cs.replace(".", "");
            }
            contentLength = Integer.parseInt(cs);
            byteList = new ArrayList<Byte>(contentLength);
        } catch (NumberFormatException ex) {
            // in case there is no content-length or the content-length cannot be
            // parsed we just print the stack trace and try to download the data
            // without the content-length information
            logger.info("Content-Length HTTP header not provided or cannot be "
                    + "parsed", ex);
            ex.printStackTrace();
            byteList = new LinkedList<Byte>();
        }

        int r;
        int counter = 0;
        while ((r = reader.read()) != -1) {
            byteList.add((byte) r);
            counter++;
            if (counter >= contentLength) {
                // content length achieved
                if (contentLength != -1) {
                    break;
                } else {
                    // we are handling chunked encoding and have to take a look at
                    // the end of chunk
                    if (r == 0) {
                        break;
                    }
                }
            }
        }

        byte[] readBytes = new byte[counter];
        for (int i = -0; i < readBytes.length; i++) {
            readBytes[i] = byteList.get(i);
        }

        request.content = new String(readBytes);
        logger.debug("request read: \r\n" + request.content);
        if (request.content.isEmpty()) {
            request.content = null;
        }
    }
}
