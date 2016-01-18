/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.saml.client.managers;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

/**
 * A list that encapsulates a byte array. Adapted from
 * {@link com.google.common.primitives.Bytes}.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * August 21, 2013
 */
class ByteArrayAsList extends AbstractList<Byte>
        implements RandomAccess, Serializable {

    /**
     * The base for hashCode().
     */
    private static final int HASHCODE_BASE = 31;
    /**
     * The data source.
     */
    private final byte[] array;

    /**
     * Constructor.
     *
     * @param pArray The data source
     */
    ByteArrayAsList(final byte[] pArray) {
        this.array = pArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return array.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte get(final int index) {
        return array[index];
    }

    /**
     * Helper to traverse a byte array in forward direction.
     *
     * @param array The data source
     * @param target The byte to find
     * @return the first position of the target in the data source; -1 if the
     * target is not found in the data source
     */
    private static int indexOf(final byte[] array, final byte target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper to traverse a byte array in reverse direction.
     *
     * @param array The data source
     * @param target The byte to find
     * @return the last position of the target in the data source; -1 if the
     * target is not found in the data source
     */
    private static int lastIndexOf(final byte[] array, final byte target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object target) {
        return (target instanceof Byte) && indexOf(array, (Byte) target) != -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final Object target) {
        if (target instanceof Byte) {
            int i = indexOf(array, (Byte) target);
            if (i >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final Object target) {
        if (target instanceof Byte) {
            int i = lastIndexOf(array, (Byte) target);
            if (i >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte set(final int index, final Byte element) {
        if (element == null) {
            throw new NullPointerException();
        }
        byte oldValue = array[index];
        array[index] = element;
        return oldValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ByteArrayAsList) {
            ByteArrayAsList that = (ByteArrayAsList) object;
            int size = size();
            if (that.size() != size) {
                return false;
            }
            if (Arrays.equals(this.array, that.array)) {
                return true;
            }
        }
        return super.equals(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < array.length; i++) {
            result = HASHCODE_BASE * result + array[i];
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[').append(array[0]);
        for (int i = 1; i < array.length; i++) {
            builder.append(", ").append(array[i]);
        }
        return builder.append(']').toString();
    }

    /**
     * @return a copy of the data source
     */
    byte[] toByteArray() {
        return Arrays.copyOf(array, array.length);
    }
    /**
     * Needed for Serializable interface.
     */
    private static final long serialVersionUID = 0;
}
