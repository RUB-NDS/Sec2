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
 * Wrapping container for data.
 * @param <T> Generic data type of the wrapped data
 * @author  Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 * 03.01.2011
 *
 * Acts as a container element for any kind of data, generics improvement
 */
public final class WrappedData<T> {

    /**
     * Wrapped data of this container.
     */
    private T data = null;

    /**
     * Default Constructor - should only be called using ADataWrapper.
     *
     * @param dataToWrap      Wrapped data
     */
    protected WrappedData(final T dataToWrap) {
        if (dataToWrap != null) {
            this.data = dataToWrap;
        }
    }

    /**
     * Getter for contained data.
     *
     * @return      Data
     */
    public T getData() {
        return this.data;
    }

    /**
     * Setter for wrapped data.
     *
     * @param dataToWrap Data to be wrapped.
     */
    public void setData(final T dataToWrap) {
        this.data = dataToWrap;
    }
}
