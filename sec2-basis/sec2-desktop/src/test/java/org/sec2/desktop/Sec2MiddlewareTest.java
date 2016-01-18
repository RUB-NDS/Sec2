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
package org.sec2.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import junit.framework.TestCase;
import org.sec2.exceptions.ExMiddlewareException;

/**
 * <DESCRIPTION>
 * @author  Juraj Somorovsky - juraj.somorovsky@rub.de
 * @date    Jul 17, 2012
 * @version 0.1
 *
 */
public class Sec2MiddlewareTest extends TestCase {

    /** path to the file in the database */
    private static final String path =
            "http://localhost:50001/exist/rest/db/calendar/test.xml";
    /** database address */
    private static final String databaseAddress = "localhost";
    /** database port */
    private static final String databasePort = "50082";
    /** Sec2 Middleware */
    private Sec2Middleware sm;
    /** default pin */
    byte[] pin = {0x1,0x2,0x3,0x4,0x5};

    @Override
    protected void setUp() throws ExMiddlewareException {
        System.out.println("\n==== Starting Middleware Tests ====");

        System.out.println("= Starting the database");
        //TODO start the exist database
        
        System.out.println("= Starting the Sec2 Middleware");
        sm = Sec2Middleware.getSec2Middleware();
//        sm.activateSmartCard(pin);
        sm.startMiddlewareServer("*", 50001);

    }

    @Override
    protected void tearDown() {
        System.out.println("\n==== Leaving Sec2 Middleware Tests ====");
        sm.stopMiddlewareServer();
    }

    
//    /**
//     * Retrieval data test
//     *
//     * @throws Exception
//     */
//    public void testGetWithEmptyPath() throws Exception {
//        System.out.println("--- retrieving " + path + " --- ");
//
//        URL u = new URL(path);
//        HttpURLConnection connect = (HttpURLConnection) u.openConnection();
//        connect.setRequestMethod("GET");
//        connect.setRequestProperty("Location",
//                databaseAddress + ":" + databasePort);
//        connect.setRequestProperty("Connection", "close");
//        connect.connect();
//
//        int r = connect.getResponseCode();
//        System.out.println(readInputStream(connect.getInputStream()));
//        connect.disconnect();
//    }
//
//    /**
//     * Test upload data to the database
//     * @throws Exception
//     */
//    public void testUploadData() throws Exception {
//        URL u = new URL(path);
//        HttpURLConnection connect = (HttpURLConnection) u.openConnection();
//        connect.setRequestMethod("PUT");
//        connect.setRequestProperty("Location",
//                databaseAddress + ":" + databasePort);
//        connect.setRequestProperty("Connection", "close");
////            connect.setRequestProperty("Transfer-Encoding", "chunked");
////            connect.setChunkedStreamingMode(0);
//
//        connect.setDoOutput(true);
//        connect.setRequestProperty("Content-Type", "text/xml");
//
//        Writer writer = new OutputStreamWriter(connect.getOutputStream(),
//                "UTF-8");
//        writer.write(
//                "<a><xenc:EncryptedData xmlns:xenc='http://www.w3.org/2001/04/xmlenc#'><xenc:EncryptionMethod Algorithm='http://www.w3.org/2001/04/xmlenc#aes128-cbc'/></xenc:EncryptedData><b/></a>");
//        writer.close();
//
//        connect.connect();
//        System.out.println(connect.getHeaderFields());
//        System.out.println(readInputStream(connect.getInputStream()));
//    }

    /**
     * Reads the input stream to a string
     *
     * TODO: move to some util class
     *
     * @param is
     * @return
     */
    protected static String readInputStream(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            String line;
            StringBuilder out = new StringBuilder(150);
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append("\r\n");
            }
            return out.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
}
