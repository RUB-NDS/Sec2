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
package org.sec2.securityprovider.serviceparameter;

/**
 * Container for PIN service parameter.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Sep 19, 2011
 */
public final class PIN implements IServiceParameter {

    /**
     * Minimum StackTrace length for PIN calls.
     */
    private static final int MINIMUM_STACKTRACE = 3;
    /**
     * Own package name.
     */
    private static final String OWN_PACKAGENAME;

    static {
        /*
         * Determine allowed accessor packages. Convention: It is assumed that
         * this class is located under a sub- package "serviceParameter" which
         * is a direct child of the security provider main package.
         */
        String canonicalName = PIN.class.getCanonicalName();
        int start = canonicalName.indexOf("serviceparameter");
        OWN_PACKAGENAME = canonicalName.substring(0, start - 1);
    }
    /**
     * PIN for card access.
     */
    private final byte[] pin;

    /**
     * Constructor for the PIN.
     *
     * @param handeldPin The PIN to be stored in this object.
     */
    public PIN(final byte[] handeldPin) {
        // final arrays aren't final at all ;-)
        if (handeldPin != null) {
            this.pin = new byte[handeldPin.length];
            System.arraycopy(handeldPin, 0, this.pin, 0, handeldPin.length);
        } else {
            this.pin = null;
        }
    }

    /**
     * Getter for the PIN code.
     *
     * @return PIN code
     */
    public byte[] getPin() {
        byte[] result = null;
        Throwable snapshot = new Throwable();
        StackTraceElement[] stackTrace = snapshot.getStackTrace();

        // protect the PIN from non security provider callers
        if (pin != null && stackTrace != null
                && stackTrace.length >= MINIMUM_STACKTRACE) {
            String caller = stackTrace[2].getClassName();
            //TODO look into the callers, but these are neccessary since the
            //java classes call the enginge classes, so not by OWN_PACKAGENAME
            if (caller.startsWith(OWN_PACKAGENAME) || caller.startsWith("java.security")
                    || caller.startsWith("javax.crypto")) {
                result = new byte[this.pin.length];
                // protect inner object
                System.arraycopy(this.pin, 0, result, 0, this.pin.length);
            }
        }

        return result;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        // wiping
        if (pin != null) {
            for (int i = 0; i < this.pin.length; i++) {
                this.pin[i] = 0;
            }
        }
    }
}
