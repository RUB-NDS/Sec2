package org.sec2.android.model;

import android.test.AndroidTestCase;

/**
 * This JUnit-Class tests the methods of class CountedGroup.
 * 
 * @author nike
 */
public final class CountedGroupTest extends AndroidTestCase
{
    /**
     * This method tests the methods getMemberCount() and setMemberCount().
     */
    public void testSetAndGetMemberCount()
    {
        CountedGroup group = new CountedGroup();

        group.setMemberCount(1);
        assertEquals(1, group.getMemberCount());
    }
}
