package org.sec2.backend;

import java.util.List;
import org.sec2.backend.exceptions.PermissionException;

/**
 * Interface to describe a group.
 * 
 * @author Utimaco Safeware
 */
public interface IGroupInfo {

    /**
     * Returns the name of the group.
     * 
     * @return user chosen name of group
     */
    public String getGroupName();

    /**
     * Returns the identifier of the key that belongs to this group.
     * This is the same as the group's name. 
     * 
     * @return key id (same as group's name)
     */
    public String getKeyId();

    /**
     * Returns the wrapped cluster key. 
     * The returned cluster key is wrapped with the user's public encryption key. 
     * 
     * @return cluster key wrapped with the requesting user's public key
     */
    public byte[] getWrappedKey();

    /**
     * Returns an {@link IUserInfo} object with information about the 
     * group operator/owner.
     * 
     * @return owner of the group
     */
    public IUserInfo getOperator();

    /**
     * Returns a {@link List<IUserInfo>} with all members of this group. 
     * The returned list also includes the group operator/owner.
     * 
     * @return A list of the members of the group (including the operator)
     * @throws PermissionException
     *             if callee is not in group (race condition prevention)
     */
    public List<IUserInfo> getMembers();
}
