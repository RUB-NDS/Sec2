/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.saml.client.gui.log;

/**
 *
 * @author dev
 */
public class CustomLogEntry {
    
    final public static String PREFIX_RESPONSE_ENCRYPTED = "Received response: ";
    final public static String PREFIX_RESPONSE_PLAIN = "Decrypted response content: ";
    final public static String PREFIX_REQUEST_ENCRYPTED = "Sending request: ";
    final public static String PREFIX_REQUEST_PLAIN = "Unencrypted request content: ";
    final public static String NULL_VALUE = "<null/>";

    private String name = "";
    private String requestEncrypted = NULL_VALUE;
    private String requestPlain = NULL_VALUE;
    private String responseEncrypted = NULL_VALUE;
    private String responsePlain = NULL_VALUE;

    public CustomLogEntry() {
    }

    public CustomLogEntry(String name) {
        setName(name);
    }
    
    

    public String getRequestEncrypted() {
        return requestEncrypted;
    }

    void setRequestEncrypted(String requestEncrypted) {
        this.requestEncrypted = requestEncrypted;
    }

    public String getRequestPlain() {
        return requestPlain;
    }

    void setRequestPlain(String requestPlain) {
        this.requestPlain = requestPlain;
    }

    public String getResponseEncrypted() {
        return responseEncrypted;
    }

    void setResponseEncrypted(String responseEncrypted) {
        this.responseEncrypted = responseEncrypted;
    }

    public String getResponsePlain() {
        return responsePlain;
    }

    void setResponsePlain(String responsePlain) {
        this.responsePlain = responsePlain;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
    
}
