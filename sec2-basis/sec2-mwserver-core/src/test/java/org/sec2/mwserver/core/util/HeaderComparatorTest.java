package org.sec2.mwserver.core.util;

import junit.framework.TestCase;

/**
 * This JUnit-Class tests the methods of class HeaderComparator.
 *
 * @author nike
 */
public final class HeaderComparatorTest extends TestCase
{
    /**
     * This method tests the compare()-method, if it returns the correct
     * values.
     */
    public void testCompare()
    {
        final HeaderComparator comparator = new HeaderComparator();

        assertEquals(comparator.compare(null, null), 0);
        assertEquals(comparator.compare(null, "abc"), -1);
        assertEquals(comparator.compare("abc", null), 1);
        assertEquals(comparator.compare("abc", "aaa"), 1);
        assertEquals(comparator.compare("abc", "abc"), 0);
        assertEquals(comparator.compare("aaa", "abc"), -1);
    }
}
