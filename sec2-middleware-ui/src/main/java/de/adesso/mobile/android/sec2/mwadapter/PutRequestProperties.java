package de.adesso.mobile.android.sec2.mwadapter;

/**
 * Container class for all properties required for a HTTP PUT request to the cloud.
 * @author schuessler
 *
 */
public final class PutRequestProperties extends RequestProperties
{
    private String data = null; //The XML document itself

    /**
     * The empty contructor.
     */
    public PutRequestProperties()
    {
        super(HttpMethod.PUT);
    }

    /**
     * Preferred constructor
     * @param host - The host's name or its IP address of the cloud service without the protocoll specification,
     * 	e.g. www.example.com
     * @param resource - The resource to be send to the cloud service, may contain a path
     * @param port - The port where the host is listening
     * @param data - The XML document to be posted in the request
     */
    public PutRequestProperties(final String host, final String resource, final int port, final String data)
    {
        super(host, resource, port, HttpMethod.PUT);
        this.data = data;
    }

    /**
     * Returns the XML document
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * The XML document to be posted in the request.
     * @param data - The data to set
     */
    public void setData(final String data)
    {
        this.data = data;
    }
}
