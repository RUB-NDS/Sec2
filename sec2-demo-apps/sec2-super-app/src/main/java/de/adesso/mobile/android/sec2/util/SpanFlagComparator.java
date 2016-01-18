package de.adesso.mobile.android.sec2.util;

import java.util.Comparator;

/**
 *  Comparator used to sort the list of SpanFlags that represent the parts which will be encrypted 
 *  
 *  @author hoppe
 *  
 */
public class SpanFlagComparator implements Comparator<SpanFlag> {

    @Override
    public int compare(final SpanFlag object1, final SpanFlag object2) {
        return (object1.start < object2.start ? -1 : (object1.start == object2.start ? 0 : 1));
    }
}
