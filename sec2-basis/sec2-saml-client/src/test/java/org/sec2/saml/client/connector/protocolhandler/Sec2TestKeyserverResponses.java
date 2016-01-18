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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * A set of responses to mock a keyserver.
 *
 * Adapted from
 *  http://www.javaworld.com/javaworld/jw-07-2002/jw-0719-networkunittest.html
 *
 * @author  Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * November 06, 2012
 */
public final class Sec2TestKeyserverResponses {

    /** Mapping of key to response string. **/
    private HashMap<String, String>  inputStrings =
            new HashMap<String, String>();

    /**
     * Singleton constructor.
     */
    private Sec2TestKeyserverResponses() {
        //TODO: fill
        inputStrings.put(null, "");
        inputStrings.put("failure", "failurebla");
        inputStrings.put("broken", "brokenbla");
    }

    /**
     * Singleton getter.
     * @return The singleton instance
     */
    public static Sec2TestKeyserverResponses getInstance() {
        return Sec2TestKeyserverResponsesHolder.INSTANCE;
    }

    /**
     * Return an input stream associated with the given key, or null
     * if none.
     * @param key the key to identify the input stream
     * @return the input stream associated with the given key, or null
     *          if none.
     */
    protected InputStream getInputStream(final String key) {
        String s = inputStrings.get(key);
        if (s == null) {
            return null;
        } else {
            return new ByteArrayInputStream(s.getBytes());
        }
    }

    /**
     * Nested class holding Singleton instance.
     */
    private static class Sec2TestKeyserverResponsesHolder {
        /**
         * The singleton instance.
         */
        private static final Sec2TestKeyserverResponses INSTANCE =
                new Sec2TestKeyserverResponses();
    }
 }
