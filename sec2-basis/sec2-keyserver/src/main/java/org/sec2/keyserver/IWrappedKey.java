package org.sec2.keyserver;
/**
 * @author Utimaco Safeware
 * @XXX: more documentation
 */
public interface IWrappedKey {
	public String getErrorMessage();
	public Exception getException();
	public String getGroupId();
	public String getGroupName();
	public String getKeyId();
	public byte[] getWrappedKey();
}
