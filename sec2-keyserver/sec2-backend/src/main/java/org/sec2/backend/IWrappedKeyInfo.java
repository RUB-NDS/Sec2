package org.sec2.backend;

/**
 * @author Utimaco Safeware
 * @XXX: more documentation
 */
public interface IWrappedKeyInfo {
    /**
     * 
     * @return
     */
    public byte[] getWrappedKey();

    public String getId();
}
