package org.sec2.android.app.component;

import java.util.Arrays;

import de.adesso.mobile.android.sec2.mwadapter.model.Group;
import de.adesso.mobile.android.sec2.mwadapter.util.GroupHandler;

/**
 * Works like its super class, but adds an additional array to mark, if a group
 * is checked or not.
 *
 * @author nike
 */
public class CheckedGroupHandler extends GroupHandler
{
    private boolean[] checked;
    private int numberOfCheckedGroups = 0;

    /**
     * This constructor constructs an object of type CheckedGroupHandler for an
     * empty array of type Group.
     */
    public CheckedGroupHandler()
    {
        this(null);
    }

    /**
     * The constructor for the CheckedGroupHandler. It splits up the passed
     * array of type Group in three arrays. One for all the containing IDs, one
     * for all the containing names and one for the information, if a group is
     * checked or net.
     *
     * @param groups - An array of type Group
     */
    public CheckedGroupHandler(final Group[] groups)
    {
        super(groups);
        if (groups != null)
        {
            checked = new boolean[groups.length];
            for (int i = 0; i < groups.length; i++)
            {
                checked[i] = false;
            }
        }
        else
        {
            checked = new boolean[0];
        }
    }

    /**
     * Sets the group at "index" to the value of "checked".
     *
     * @param index - The index of the group, to be checked/unchecked
     * @param checked - TRUE if the group is checked, otherwise FALSE
     */
    public void setChecked(final int index, final boolean checked)
    {
        if (this.checked[index] != checked)
        {
            if (checked)
            {
                numberOfCheckedGroups++;
            }
            else
            {
                numberOfCheckedGroups--;
            }
        }
        this.checked[index] = checked;
    }

    /**
     * Returns, whether the group at "index" is checked or not.
     *
     * @param index - The index of the group
     * @return Whether the group is checked or not
     */
    public boolean isChecked(final int index)
    {
        return checked[index];
    }

    /**
     * Toggles the checked-status of the group at "index".
     *
     * @param index - The index of the group
     */
    public void toggle(final int index)
    {
        if (checked[index])
        {
            numberOfCheckedGroups--;
        }
        else
        {
            numberOfCheckedGroups++;
        }
        checked[index] = !checked[index];
    }

    /**
     * Returns the number of checked groups.
     *
     * @return The number of checked groups
     */
    public int getNumberOfCheckedGroups()
    {
        return numberOfCheckedGroups;
    }

    /**
     * Returns a copy of the array, where it is marked, whether a group is
     * checked or not.
     *
     * @return A copy of the array where it is marked, whether a group is
     *  checked or not
     */
    public boolean[] getCheckedGroups()
    {
        return Arrays.copyOf(checked, checked.length);
    }
}
