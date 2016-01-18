package org.sec2.android.servers.rest;

import android.test.AndroidTestCase;

/**
 * This JUnit-Class tests the methods of class PopupResultWrapper.
 *
 * @author nike
 */
public final class PopupResultWrapperTest extends AndroidTestCase
{
    /**
     * This method tests the methods setYesClicked() and isYesClicked().
     */
    public void testSetAndIsYesClicked()
    {
        PopupResultWrapper prw = new PopupResultWrapper();

        assertFalse(prw.isYesClicked());
        prw.setYesClicked(true);
        assertTrue(prw.isYesClicked());
    }
}
