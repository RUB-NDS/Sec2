package de.adesso.mobile.android.sec2.mwadapter.gui;

import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;

/**
 * Base Activity class of most of the Activities of the MwAdapter. It provides a counter for the number of
 * Activites started. If a certain threshold is exceeded, the lowermost Activity in the stack is destroyed.
 * 
 * @author schuessler
 *
 */
public class BaseMwAdapterActivity extends Activity
{
    private static LinkedList<Activity> activities = new LinkedList<Activity>();

    private synchronized void pushToStack()
    {
        if(activities.size() >= 10) activities.removeFirst().finish();
        activities.addLast(this);
    }

    private synchronized void pullFromStack()
    {
        int i = activities.size() - 1;

        while(i >= 0)
        {
            //Here == is really correct, because we want to get the object
            if(activities.get(i) == this)
            {
                activities.remove(i);
                break;
            }
            else i--;
        }
    }

    /**
     * Pattern for actions, which should be done within the onCreate()-method in derived
     * activities. This allows derived activites to call super.onCreate(), but for any
     * undesired actions are done.
     * 
     * @param savedInstanceState - The saved instance-state
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        doOnCreateLayout(savedInstanceState);
        doOnCreateMisc(savedInstanceState);
    }

    /**
     * This method is called in the onCreate()-method of this activity. The onCreate()-method itself is nearly empty.
     * It only calls super.onCreate(), this method and at last doOnCreateMisc(). This allows derived activities to call super.onCreate(),
     * but for any undesired actions are done, and so this method should be overwritten by derived activities. In
     * BaseMwAdapterActivity this method is empty.
     * Within this method all actions concerning the layout of the activity should be done here.
     * 
     * @param savedInstanceState - The saved instance-state
     */
    protected void doOnCreateLayout(final Bundle savedInstanceState)
    {
        //To be overwritten in derived activities
    }

    /**
     * This method is called in the onCreate()-method of this activity. The onCreate()-method itself is nearly empty.
     * It only calls super.onCreate(), doOnCreateLayout() and at last this method. This allows derived activites to call super.onCreate(),
     * but for any undesired actions are done, and so this method should be overwritten by derived activities. In
     * BaseMwAdapterActivity this method is empty.
     * Within this method all other actions, which concern not the layout of the activity, should be done here.
     * 
     * @param savedInstanceState - The saved instance-state
     */
    protected void doOnCreateMisc(final Bundle savedInstanceState)
    {
        //To be overwritten in derived activities
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        pullFromStack();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(isPushToStack()) pushToStack();
    }

    /**
     * This method return, whether the activity should be pushed to the stack or not. An activity derived from this class should always allow,
     * to be pushed to the stack to avoid activity flooding. So this method always return TRUE. This method should not be overwritten in derived
     * classes unless you know what you do.
     * 
     * @return TRUE, if the activity may be pushed to the stack of the anti-activity-flooding-mechanism, otherwise FALSE
     */
    protected boolean isPushToStack()
    {
        return true;
    }
}
