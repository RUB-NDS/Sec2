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
package org.sec2.managers.beans;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * Class representing a user. This class is threadsafe.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.3
 *
 * June 31, 2012
 */
public class User implements Serializable {

    /**
     * The format a valid email address needs to have.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]+",
            Pattern.CASE_INSENSITIVE);
    /**
     * the user's ID. This is the SHA-256 hash of the user's public key.
     */
    private final byte[] userID;
    /**
     * The user's email address.
     */
    private String emailAddress;
    /**
     * Lock to provide thread-safety.
     */
    private final ReentrantReadWriteLock lock;

    /**
     * Create a new user based on ID and email address. Constructor is package
     * private; it is only used by managers to create objects for existing
     * users.
     *
     * @param userID the userID to set
     * @param emailAddress the email address to set
     */
    public User(final byte[] userID, final String emailAddress) {
        super();
        if (userID.length == 0) {
            throw new IllegalArgumentException(
                    "Parameter userID must not be empty!");
        }
        if (emailAddress == null) {
            throw new NullPointerException(
                    "Parameter emailAddress must not be null!");
        }
        if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
            throw new IllegalArgumentException(
                    "Parameter emailAddress is no valid email address!");
        }
        this.userID = userID;
        this.emailAddress = emailAddress;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * @return the user's ID
     */
    public byte[] getUserID() {
        byte[] returnID;
        try {
            lock.readLock().lock();
            returnID = this.userID;
        } finally {
            lock.readLock().unlock();
        }
        return returnID;
    }

    /**
     * @return the user's emailAddress
     */
    public String getEmailAddress() {
        String returnEmailAddress = null;
        try {
            lock.readLock().lock();
            returnEmailAddress = emailAddress;
        } finally {
            lock.readLock().unlock();
        }
        return returnEmailAddress;
    }

    /**
     * @param emailAddress the email address to set. Package private because the
     * email address should only be changed by a UserManager.
     */
    public void setEmailAddress(final String emailAddress) {
        if (emailAddress == null) {
            throw new NullPointerException(
                    "Parameter emailAddress must not be null!");
        }
        if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
            throw new IllegalArgumentException(
                    "Parameter emailAddress is no valid email address!");
        }
        try {
            lock.writeLock().lock();
            this.emailAddress = emailAddress;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
