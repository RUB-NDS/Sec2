package de.adesso.mobile.android.sec2.mwadapter.tasks;

import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;
import de.adesso.mobile.android.sec2.mwadapter.model.Group;

/**
 * Calls the method MwAdapter-getGroups().
 * 
 * @author schuessler
 */
public final class GetGroupTask extends MwAdapterTask<Group>
{
    private final String key;
    private final String algorithm;
    private final String appName;
    private final String groupId;
    private final int port;

    public GetGroupTask(final String key,
            final String algorithm, final String appName, final int port, final String groupId)
    {
        this.key = key;
        this.algorithm = algorithm;
        this.appName = appName;
        this.port = port;
        this.groupId = groupId;
    }

    @Override
    protected Group doInBackground(final Void... params)
    {
        final MwAdapter mwAdapter = MwAdapter.getInstance();
        Group group = null;

        try
        {
            group = mwAdapter.getGroup(key, algorithm, appName, port,
                    groupId);
        }
        catch (final Exception e)
        {
            exception = e;
        }

        return group;
    }
}
