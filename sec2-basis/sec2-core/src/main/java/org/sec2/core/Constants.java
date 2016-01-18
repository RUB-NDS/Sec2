/*
/*
 * Copyright 2011 Sec2 Consortium
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit written
 * permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://www.sec2.org
 */

package org.sec2.core;

/**
 * Commonly used constants which remain final during application lifetime.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * 03.01.2011
 */
public final class Constants {

    /**
     * Private default constructor in order to prevent instantiation.
     */
    private Constants() {
    }
    /**
     * Determines the line separator.
     */
    public static final String LINE_SEPARATOR =
            System.getProperty("line.separator");
    /**
     * Determines the file separator.
     */
    public static final String FILE_SEPARATOR =
            System.getProperty("file.separator");
    /**
     * Path to the running application.
     */
    public static final String APPLICATION_PATH =
            System.getProperty("user.dir");
}
