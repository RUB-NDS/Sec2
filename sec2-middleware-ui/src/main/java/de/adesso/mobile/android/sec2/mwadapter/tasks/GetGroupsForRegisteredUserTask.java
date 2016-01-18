package de.adesso.mobile.android.sec2.mwadapter.tasks;

import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;

/**
 * Calls the method MwAdapter-getGroupsForRegisteredUser().
 * 
 * @author schuessler
 */
public final class GetGroupsForRegisteredUserTask extends MwAdapterTask<Group[]>
{
    private final String key;
    private final String algorithm;
    private final String appName;
    private final int port;

    public GetGroupsForRegisteredUserTask(final String key,
            final String algorithm, final String appName, final int port)
    {
        this.key = key;
        this.algorithm = algorithm;
        this.appName = appName;
        this.port = port;
    }

    @Override
    protected Group[] doInBackground(final Void... params)
    {
        final MwAdapter mwAdapter = MwAdapter.getInstance();
        Group[] groups = null;

        try
        {
            groups = mwAdapter.getGroupsForRegisteredUser(key, algorithm,
                    appName, port);
        }
        catch (final Exception e)
        {
            exception = e;
        }

        return groups;
    }
}
