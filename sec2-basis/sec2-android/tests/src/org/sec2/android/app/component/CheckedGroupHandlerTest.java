package org.sec2.android.app.component;

import android.test.AndroidTestCase;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;

/**
 * This JUnit-Class tests the methods of class CheckedGroupHandler.
 * 
 * @author nike
 */
public final class CheckedGroupHandlerTest extends AndroidTestCase
{
    private CheckedGroupHandler emptyHandler = new CheckedGroupHandler();
    private CheckedGroupHandler filledHandler = new CheckedGroupHandler
            (new Group[]{new Group("1", "A"), new Group("2", "B"),
                    new Group("3", "C")});

    /**
     * This method tests the getCheckedGroups()-method, when the handler is
     * empty.
     */
    public void testGetCheckedGroupsEmpty()
    {
        assertEquals(0, emptyHandler.getCheckedGroups().length);
    }

    /**
     * This method tests the getNumberOfCheckedGroups()-method, when the
     * handler is empty.
     */
    public void testGetNumberOfCheckedGroupsEmpty()
    {
        assertEquals(0, emptyHandler.getNumberOfCheckedGroups());
    }

    /**
     * This method tests the getCheckedGroups()-method, when the handler is
     * not empty.
     */
    public void testGetCheckedGroupsFilled()
    {
        boolean[] checkedGroups = filledHandler.getCheckedGroups();

        assertEquals(3, checkedGroups.length);
        for(int i = 0; i < 3; i++) assertFalse(checkedGroups[i]);
    }

    /**
     * This method tests the getNumberOfCheckedGroups()-method, when the
     * handler is not empty.
     */
    public void testGetNumberOfCheckedGroupsFilled()
    {
        assertEquals(0, filledHandler.getNumberOfCheckedGroups());
    }

    /**
     * This method tests the isChecked()-method, when the handler is not empty.
     */
    public void testIsCheckedFilled()
    {
        assertFalse(filledHandler.isChecked(0));
    }

    /**
     * This method tests the setChecked()-method, when the handler is not
     * empty.
     */
    public void testSetCheckedFilled()
    {
        boolean[] checkedGroups = null;

        filledHandler.setChecked(1, true);
        checkedGroups = filledHandler.getCheckedGroups();
        assertFalse(checkedGroups[0]);
        assertTrue(checkedGroups[1]);
        assertFalse(checkedGroups[2]);
        assertEquals(1, filledHandler.getNumberOfCheckedGroups());
        assertTrue(filledHandler.isChecked(1));
        filledHandler.setChecked(1, false);
    }

    /**
     * This method tests the toggle()-method, when the handler is not
     * empty.
     */
    public void testToggleFilled()
    {
        boolean[] checkedGroups = null;

        filledHandler.toggle(1);
        checkedGroups = filledHandler.getCheckedGroups();
        assertFalse(checkedGroups[0]);
        assertTrue(checkedGroups[1]);
        assertFalse(checkedGroups[2]);
        assertEquals(1, filledHandler.getNumberOfCheckedGroups());
        assertTrue(filledHandler.isChecked(1));
        filledHandler.toggle(1);
    }
}
