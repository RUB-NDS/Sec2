package de.adesso.mobile.android.sec2.util;

/**
 * Represents a selection of an EditText that has or will be encrypted. 
 * 
 * @author hoppe
 * 
 */
public class SpanFlag {

    public int start;
    public int end;

    public SpanFlag(final int start, final int end) {
        this.start = start;
        this.end = end;
    }
}
