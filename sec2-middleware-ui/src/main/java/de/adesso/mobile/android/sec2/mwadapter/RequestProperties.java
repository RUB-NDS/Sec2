package de.adesso.mobile.android.sec2.mwadapter;

/**
 * Container class for all properties required for an HTTP request to the cloud. The class should be derived by a class using a
 * concrete HTTP method like GET.
 * @author schuessler
 *
 */
class RequestProperties
{
    private String host = null; //The host name for the cloud
    private String resource = null; //The resource for the cloud
    private int port = - 1; //The port where the host is listening
    private HttpMethod httpMethod = null; //The HTTP method

    /**
     * The constructor
     * 
     * @param httpMethod - The HTTP method to be used
     */
    protected RequestProperties(final HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    /**
     * Preferred constructor
     * @param host - The host's name or its IP address of the cloud service without the protocoll specification,
     *  e.g. www.example.com
     * @param resource - The resource to be send to the cloud service, may contain a path
     * @param port - The port where the host is listening
     * @param httpMethod - The HTTP method to be used
     */
    protected RequestProperties(final String host, final String resource, final int port, final HttpMethod httpMethod)
    {
        this.host = host;
        this.resource = resource;
        this.port = port;
        this.httpMethod = httpMethod;
    }

    /**
     * Returns the host's name
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * The host's name respectively the URL of the cloud without the protocoll specification, e.g. www.example.com
     * @param host - The host to set
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Returns the name of the resource to be sent to the cloud service. May contain a path.
     * 
     * @return the name of the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets the the name of the resource to be sent to the cloud service. May contain a path.
     * 
     * @param resource - The name of the resource
     */
    public void setResource(final String resource) {
        this.resource = resource;
    }

    /**
     * Returns the port
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * The port where the host is listening
     * @param port - The port to set
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Returns the HTTP method
     * @return the HTTP method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * The HTTP method to be used for the request
     * @param httpMethod - The HTTP method to set
     */
    public void setHttpMethod(final HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }
}
