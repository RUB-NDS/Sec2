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
package org.sec2.datawrapper;

/**
 * Wrapper for arbitrary data. Wraps any kind of data into a WrappedData object.
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * 03.01.2011
 */
public final class DataWrapper {

    /**
     * Constructor made private to prevent instantiation.
     */
    private DataWrapper() {
    }

    /**
     * Wraps arbitrary data into an WrappedData object.
     *
     * @param <T>     Generic data type of the wrapped data
     * @param data    Data to be wrapped
     * @return        Wrapped data
     */
    public static <T> WrappedData<T> wrap(final T data) {
        // remember that raw types may cause problems here
        return new WrappedData<T>(data);
    }

    /**
     * Unwraps WrappedData objects into its origin data type.
     *
     * @param <T>     Generic data type of the wrapped data
     * @param data    WrappedData object
     * @return        Contained data
     */
    public static <T> T unwrap(final WrappedData<T> data) {
        return data.getData();
    }
}
