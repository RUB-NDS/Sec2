/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.backend;

import java.util.ArrayList;
import java.util.HashSet;
import org.sec2.rest.RestException;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class Noncinator {

    private final static int MAX = 1024;
    private long[] received = new long[MAX];
    private int index = 0;
    private int stored = 0;

    private Noncinator() {
    }
    private static Noncinator INSTANCE;

    public static Noncinator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Noncinator();
        }
        return INSTANCE;
    }

    private boolean isInArray(long nonce) {
        int stop = max(received.length,stored);
        for (int i = 0; i < stop; i++) {
            if (received[i] == nonce) {
                return true;
            }
        }
        return false;
    }

    private void store(long l) {
        if (index >= MAX) {
            index = 0;
        }
        received[index] = l;
        index++;
        if (stored < MAX) {
            stored++;
        }
    }

    private int max(int a, int b) {
        return a > b ? a : b;
    }

    public void verify(String nonce) throws RestException {
        Long l = Long.parseLong(nonce);
        if (isInArray(l)) {
            throw new RestException("Nonce is used!");
        } else {
             store(l);
        }
    }
;
}
