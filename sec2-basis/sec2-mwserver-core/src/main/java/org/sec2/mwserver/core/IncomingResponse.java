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
 * This class analyses an incoming HTTP-response and splits it up into its
 * component-parts.
 *
 * @author nike
 */
public class IncomingResponse {

    private int statusCode = 0;
    private String statusInfo = null;
    private String fullStatusLine = null;
    private String originalRequest = null;
    private String content = null;
    private LinkedHashMap<String, String> headers = null;
    private static final String ERR_LINE_NOT_ANALYZED =
            "Zeile {0} konnte nicht analysiert werden: {1}";
    
    private static Logger logger = LoggerFactory.getLogger(IncomingResponse.class);

    private IncomingResponse() {
    }

    ;

    /**
     * Analyses the incoming response.
     *
     * @param reader - The reader for the input stream
     * @return the analysed request
     * @throws HandleRequestException
     * @throws IOException
     */
    public static IncomingResponse analyseIncomingResponse(
            final BufferedReader reader) throws IOException {
        final IncomingResponse response = new IncomingResponse();
        final StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        String[] temp = line.split("\\s", 3);
        final LinkedHashMap<String, String> headerMap =
                new LinkedHashMap<String, String>();
        int linesRead = 1;

        if (temp.length >= 3) {
            //Setze den HTTP-Statuscode
            try {
                response.statusCode = Integer.parseInt(temp[1]);
            } catch (final NumberFormatException nfe) {
                throw new HandleRequestException(MessageFormat.format(
                        ERR_LINE_NOT_ANALYZED, linesRead, line));
            }
            response.statusInfo = temp[2];
            response.fullStatusLine = line;
            sb.append(line);

            //Restliche Header auslesen
            while ((line = reader.readLine()) != null) {
                //Falls eine Leerzeile kommt, ist der Headerbereich zuende und
                //der Content kann gelesen werden
                if (line.isEmpty()) {
                    sb.append("\r\n");
                    readContent(reader, sb, response, headerMap.get("Content-Length"));
                    break;
                }
                linesRead++;
                temp = line.split(":\\s", 2);
                if (temp.length == 2) {
                    headerMap.put(temp[0], temp[1]);
                    sb.append("\r\n" + line);
                    logger.debug("header map adding: " + temp[0] + ":" + temp[1]);
                } else {
                    throw new HandleRequestException(MessageFormat.format(
                            ERR_LINE_NOT_ANALYZED, linesRead, line));
                }
            }
            response.headers = headerMap;
            response.originalRequest = sb.toString();
        } else {
            throw new HandleRequestException(MessageFormat.format(
                    ERR_LINE_NOT_ANALYZED, linesRead, line));
        }

        return response;
    }

    /**
     * Returns the full status-line of the HTTP-response.
     *
     * @return The full status-line of the HTTP-response
     */
    public String getFullStatusLine() {
        return fullStatusLine;
    }

    /**
     * Returns the status-code of the HTTP-response.
     *
     * @return The status-code of the HTTP-response
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the original HTTP-response.
     *
     * @return The original HTTP-response.
     */
    public String getOriginalResponse() {
        return originalRequest;
    }

    /**
     * Returns the HTTP-headers of the HTTP-response.
     *
     * @return The HTTP-headers of the HTTP-response
     */
    public LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the content of the HTTP-response. If no content was available,
     * NULL is returned.
     *
     * @return The content of the HTTP-response or NULL, if no content was
     * available
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the status-info of the HTTP-response.
     *
     * @return The status-info of the HTTP-response
     */
    public String getStatusInfo() {
        return statusInfo;
    }

    @Override
    public String toString() {
        return getOriginalResponse();
    }

    private static void readContent(final BufferedReader reader,
            final StringBuilder sb, final IncomingResponse response,
            final String headerContentLength) throws IOException {

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
                if(contentLength != -1) {
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
        for(int i =-0; i<readBytes.length; i++) {
            readBytes[i] = byteList.get(i);
        }
        response.content = new String(readBytes);
        
        logger.debug("response arrived:\r\n " + response.content);
        if (response.content.isEmpty()) {
            response.content = null;
        }
    }
}
