package org.sec2.android.app;

import org.sec2.android.app.component.CheckedUserHandler;
import org.sec2.middleware.R;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import de.adesso.mobile.android.sec2.mwadapter.logging.LogHelper;

/**
 * The abstract activity shows an intro-text, list with users who can be
 * selected through checkboxes and two buttons below the list. One buttons is
 * for save-actions, the other for cancel-action. The intro-text is represented
 * by a TextView object. The text of the TextView should be set by deriving
 * classes in the abstract method initTextView(). To gather access to the
 * TextView-object, the method getTextView() may be used. The list should be
 * filled by deriving classes in the abstract method initListView(). To gather
 * access to the ListView-object, the method getListView() may be used. The
 * button for the save-actions is deactivated by default and is only active, if
 * at least one list-element was checked. The save-actions have to be
 * implemented by deriving classes, using the abstract method save(). The
 * cancel-action is already implemented. It just closes this activity. It may
 * be overwritten by deriving classes.
 *
 * @author schuessler
 */
public abstract class AbstractUsersChooserActivity extends Activity
{
    private Button saveBtn = null;
    private ListView list = null;
    private TextView intro = null;
    private CheckedUserHandler userHandler = null;

    private static final Class<?> CLAZZ = AbstractUsersChooserActivity.class;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.user_chooser);

        saveBtn = (Button)(findViewById(R.id.user_chooser_save_btn));
        saveBtn.setEnabled(false);

        intro = (TextView)(findViewById(R.id.user_chooser_intro));
        list = (ListView)findViewById(R.id.user_chooser_list);

        initTextView();
        userHandler = initListView();
    }

    /**
     * Returns the TextView-object, which should be used to show an
     * introductive text. The object's text is empty by default and should be
     * set in the abstract method @see initTextView().
     *
     * @return The TextView-object
     */
    protected TextView getTextView()
    {
        return intro;
    }

    /**
     * This method should be used to set the text of the activity's
     * TextView-object (@see getTextView()).
     */
    protected abstract void initTextView();

    /**
     * Returns the ListView-object. The list should be filled by deriving
     * classes in the abstract method @see initListView().
     *
     * @return The ListView-object
     */
    protected ListView getListView()
    {
        return list;
    }

    /**
     * This method should be used to fill the activity's ListView-object (@see
     * getTextView()). The implementation of this method must return an object
     * of type CheckedUserHandler, which contains all User-objects, which are
     * in the list, too.
     *
     * @return A CheckedUserHandler-object containing all User-objects, which
     *  are in the list, too.
     */
    protected abstract CheckedUserHandler initListView();

    /**
     * This method is called, when the user clicks on a checkbox defined by
     * user_chooser.xml.
     *
     * @param view - The view that called this method.
     */
    public void onCheckboxClicked(final View view)
    {
        final int position = list.getPositionForView(view);

        if (userHandler != null)
        {
            if (position != AdapterView.INVALID_POSITION)
            {
                userHandler.setChecked(position, ((CheckBox)view).isChecked());
                if (userHandler.getNumberOfCheckedUsers() > 0)
                {
                    saveBtn.setEnabled(true);
                }
                else
                {
                    saveBtn.setEnabled(false);
                }
            }
            else
            {
                LogHelper.logW(CLAZZ, "Postion " + position
                        + " war nicht gültig!");
            }
        }
        else
        {
            LogHelper.logW(CLAZZ, "Variable \"userHandler\" war NULL!");
        }
    }

    /**
     * Is called, when the cancel-button is pressed, and finishes this
     * activity.
     *
     * @param view - The view
     */
    public void cancel(final View view)
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * Is called, when the save-button is pressed. The method must be
     * implemented in deriving classes.
     *
     * @param view - The view
     */
    public abstract void save(final View view);

    /**
     * Returns the CheckedUserHandler-object, which was returned before by the
     * method @see initListView().
     *
     * @return The CheckedUserHandler-object
     */
    protected CheckedUserHandler getUserHandler()
    {
        return userHandler;
    }
}
