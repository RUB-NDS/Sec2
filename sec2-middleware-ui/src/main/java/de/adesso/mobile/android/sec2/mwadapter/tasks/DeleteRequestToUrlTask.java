package de.adesso.mobile.android.sec2.mwadapter.tasks;

import java.io.InputStream;

import de.adesso.mobile.android.sec2.mwadapter.DeleteRequestProperties;
import de.adesso.mobile.android.sec2.mwadapter.MwAdapter;

/**
 * Calls the method MwAdapter.deleteRequestToUrl().
 * 
 * @author schuessler
 */
public final class DeleteRequestToUrlTask extends MwAdapterTask<InputStream>
{
    private final String key;
    private final String algorithm;
    private final String appName;
    private final DeleteRequestProperties properties;
    private final int port;

    public DeleteRequestToUrlTask(final String key, final String algorithm, final String appName, final int port,
            final DeleteRequestProperties properties)
    {
        this.key = key;
        this.algorithm = algorithm;
        this.appName = appName;
        this.port = port;
        this.properties = properties;
    }

    @Override
    protected InputStream doInBackground(final Void... params)
    {
        final MwAdapter mwAdapter = MwAdapter.getInstance();
        InputStream inputStream = null;

        try
        {
            inputStream = mwAdapter.deleteRequestToUrl(key, algorithm, appName, port, properties);
        }
        catch (final Exception e)
        {
            exception = e;
        }

        return inputStream;
    }
}
