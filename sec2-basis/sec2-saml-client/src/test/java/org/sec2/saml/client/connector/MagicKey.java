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
package org.sec2.saml.client.connector;

import java.util.ArrayList;
import java.util.List;

/**
 * An enum of magic keys with test URLs.
 */
public enum MagicKey {

    /**
     * Magic URL key for a URL that redirects to the real keyserver.
     */
    good("good"),
    /**
     * Magic URL key for a URL that returns a broken stream.
     */
    broken("broken"),
    /**
     * Magic URL key for a URL that cannot even be opened.
     */
    errorOnConnect("errorConnect"),
    /**
     * Magic URL key for a URL that cannot be read.
     */
    errorGetInput("errorGetInput");
    /**
     * The URL key.
     */
    private String key;

    /**
     * Constructor.
     *
     * @param pkey the URL key
     */
    private MagicKey(final String pkey) {
        this.key = pkey;
    }

    /**
     * @return the URL key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @return a list of URL keys
     */
    public static List<String> getKeys() {
        ArrayList l = new ArrayList(MagicKey.values().length);
        for (MagicKey k : MagicKey.values()) {
            l.add(k.getKey());
        }
        return l;
    }
}
