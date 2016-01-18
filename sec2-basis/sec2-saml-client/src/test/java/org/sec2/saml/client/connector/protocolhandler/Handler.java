/*
 * Copyright 2012 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.client.connector.protocolhandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Java protocol handler for the sec2test: namespace.
 *
 * Adapted from
 *  http://www.javaworld.com/javaworld/jw-07-2002/jw-0719-networkunittest.html
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 06, 2012
 */
public class Handler extends URLStreamHandler {
    /**
     * Create a URLConnection object with data from the stream registered with
     * the sec2test: host.
     *
     * @param url the URL that is to be opened
     * @return the openend connection
     * @throws IOException if the connection cannot be opened
     */
    @Override
    protected final URLConnection openConnection(final URL url)
            throws IOException {
        URLConnection uc = new Sec2TestURLConnection(url);
        uc.connect();
        return uc;
    }
}
