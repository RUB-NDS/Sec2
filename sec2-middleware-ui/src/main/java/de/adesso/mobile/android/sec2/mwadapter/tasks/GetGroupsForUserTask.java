package de.adesso.mobile.android.sec2.mwadapter.tasks;

import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;

/**
 * Calls the method MwAdapter-getGroupsForUser().
 * 
 * @author schuessler
 */
public final class GetGroupsForUserTask extends MwAdapterTask<Group[]>
{
    private final String userId;
    private final String key;
    private final String algorithm;
    private final String appName;
    private final int port;

    public GetGroupsForUserTask(final String key, final String algorithm, final String appName, final int port, final String userId)
    {
        this.userId = userId;
        this.key = key;
        this.algorithm = algorithm;
        this.appName = appName;
        this.port = port;
    }

    @Override
    protected Group[] doInBackground(final Void... params)
    {
        final MwAdapter mwAdapter = MwAdapter.getInstance();

        try
        {
            return mwAdapter.getGroupsForUser(key, algorithm, appName, port, userId);
        }
        catch (final Exception e)
        {
            exception = e;
            return null;
        }
    }
}
