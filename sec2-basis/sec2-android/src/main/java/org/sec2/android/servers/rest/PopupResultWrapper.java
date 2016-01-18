package org.sec2.android.servers.rest;

/**
 * This class wrappes the response from a popup, providing a "Yes"- and
 * "No"-button.
 *
 * @author nike
 */
public class PopupResultWrapper
{
    private boolean yesClicked = false;

    /**
     * Sets, if the "Yes"-button was clicked or not.
     *
     * @param yesClicked - Set to TRUE, if the "Yes"-button was clicked, or
     *  FALSE otherwise.
     */
    public void setYesClicked(final boolean yesClicked)
    {
        this.yesClicked = yesClicked;
    }

    /**
     * Returns, if the "Yes"-button was clicked or not.
     *
     * @return TRUE, if the "Yes"-button was clicked, FALSE otherwise
     */
    public boolean isYesClicked()
    {
        return yesClicked;
    }
}
