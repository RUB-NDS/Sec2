package org.sec2.android;

/**
 * This interface specifies a listener. It provides two methods. On is called,
 * if a connection to a service was established and the other is called, if
 * the connection to a service was closed.
 *
 * @author nike
 */
public interface IServiceConnectionListener
{
    /**
     * This method is called, if a connection to a service was established.
     */
    public void onServiceConnected();

    /**
     * This method is called, if the connection to a service was closed.
     */
    public void onServiceDisconnected();
}
