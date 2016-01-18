package de.adesso.mobile.android.sec2.mwadapter;

/**
 * Container class for all properties required for a HTTP DELETE request to the cloud.
 * @author schuessler
 *
 */
public final class DeleteRequestProperties extends RequestProperties
{
    /**
     * The empty contructor.
     */
    public DeleteRequestProperties()
    {
        super(HttpMethod.DELETE);
    }

    /**
     * Preferred constructor
     * @param host - The host's name or its IP address of the cloud service without the protocoll specification,
     *  e.g. www.example.com
     * @param resource - The resource to be send to the cloud service, may contain a path
     * @param port - The port where the host is listening
     */
    public DeleteRequestProperties(final String host, final String resource, final int port)
    {
        super(host, resource, port, HttpMethod.DELETE);
    }
}
