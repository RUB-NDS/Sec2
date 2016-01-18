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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.httpclient.HttpStatus;
import org.sec2.saml.client.SAMLClientConfig;
import org.sec2.saml.client.connector.MagicKey;
import sun.net.www.protocol.http.HttpURLConnection; // it's only a test...

/**
 * A URLConnection for the sec2test: namespace.
 *
 * Adapted from
 * http://www.javaworld.com/javaworld/jw-07-2002/jw-0719-networkunittest.html
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 16, 2013
 */
public class Sec2TestURLConnection extends HttpURLConnection {

    /**
     * The key telling which content the caller gets.
     */
    private String key;

    /**
     * Construct an URLConnection for sec2test:.
     *
     * @param url The url that is opened
     */
    protected Sec2TestURLConnection(final URL url) {
        super(getRealURL(), null, null);
        key = url.getRef();
    }

    /**
     * @return the real keyserver's URL
     */
    private static URL getRealURL() {
        try {
            return new URL(SAMLClientConfig.SEC2_KEYSERVER_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final InputStream getInputStream() throws IOException {
        this.checkKey();
        if (key.equals(MagicKey.broken.getKey())) {
            return new BrokenInputStream(
                    Sec2TestKeyserverResponses.getInstance().
                    getInputStream(key));
        }
        if (key.equals(MagicKey.errorGetInput.getKey())) {
            throw new IOException("Simulated error on getting InputStream.");
        }
        if (key.equals(MagicKey.good.getKey())) {
            return super.getInputStream();
        }
        throw new IOException("Should be impossible");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws IOException {
        this.checkKey();
        if (key.equals(MagicKey.errorOnConnect.getKey())) {
            throw new IOException("Simulated error on opening connection.");
        }
        if (key.equals(MagicKey.good.getKey())) {
            super.connect();
        }
    }

    /**
     * @throws IOException if something is wrong with the URL key
     */
    private void checkKey() throws IOException {
        if (key == null) {
            throw new IOException("URL-Key is null");
        }

        if (!MagicKey.getKeys().contains(key)) {
            throw new IOException("No testurl registered with key " + key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDoOutput(final boolean dooutput) {
        if (key.equals(MagicKey.good.getKey())) {
            this.disconnect();
            super.setDoOutput(dooutput);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequestProperty(final String pkey, final String value) {
        if (pkey.equals(MagicKey.good.getKey())) {
            super.setRequestProperty(pkey, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if (key.equals(MagicKey.good.getKey())) {
            return super.getOutputStream();
        } else {
            return new ByteArrayOutputStream();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getResponseCode() throws IOException {
        if (key.equals(MagicKey.good.getKey())) {
            return super.getResponseCode();
        } else {
            return HttpStatus.SC_OK;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResponseMessage() throws IOException {
        if (key.equals(MagicKey.good.getKey())) {
            return super.getResponseMessage();
        } else {
            return "Simulated HTTP Response";
        }
    }
}
