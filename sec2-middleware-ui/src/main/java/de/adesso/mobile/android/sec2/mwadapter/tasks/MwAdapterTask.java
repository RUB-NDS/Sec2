package de.adesso.mobile.android.sec2.mwadapter.tasks;

import android.os.AsyncTask;

public abstract class MwAdapterTask<Result> extends AsyncTask<Void, Integer, Result>
{
    protected Exception exception;

    @Override
    protected final void onPostExecute(final Result result)
    {
        if (exception == null)
        {
            onPostExecuteWithoutException(result);
        }
        else
        {
            onPostExecuteWithException(result);
        }
    };

    public Exception getException()
    {
        return exception;
    }

    protected void onPostExecuteWithException(final Result result) {}

    protected void onPostExecuteWithoutException(final Result result) {}
}
