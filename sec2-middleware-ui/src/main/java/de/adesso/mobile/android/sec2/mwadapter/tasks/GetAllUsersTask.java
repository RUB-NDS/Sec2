package de.adesso.mobile.android.sec2.mwadapter.tasks;

import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.model.User;

/**
 * Calls the method MwAdapter.getUsersInGroup().
 * 
 * @author schuessler
 */
public final class GetAllUsersTask extends MwAdapterTask<User[]>
{
    private final String key;
    private final String algorithm;
    private final String appName;
    private final int port;

    public GetAllUsersTask(final String key,
            final String algorithm, final String appName, final int port)
    {
        this.key = key;
        this.algorithm = algorithm;
        this.appName = appName;
        this.port = port;
    }

    @Override
    protected User[] doInBackground(final Void... params)
    {
        final MwAdapter mwAdapter = MwAdapter.getInstance();
        User[] user = null;

        try
        {
            user = mwAdapter.getAllUsers(key, algorithm, appName, port);
        }
        catch (final Exception e)
        {
            exception = e;
        }

        return user;
    }
}
