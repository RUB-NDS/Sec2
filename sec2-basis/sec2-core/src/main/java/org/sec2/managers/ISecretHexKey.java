package org.sec2.managers;

import javax.crypto.SecretKey;

public interface ISecretHexKey extends SecretKey
{
	/**
	 * Returns a key in the hexadecimal representation including leading zeros.
	 * @return The hexadecimal representation of the key.
	 */
	public String toHexString();
}
