package org.sec2.backend.impl;

import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;

/**
 * Default implementation of the {@link IGroupInfo} interface used by the 
 * {@link UserManagement} class.
 * 
 * @author Utimaco Safeware
 *
 */
public class GroupInfo implements IGroupInfo {

    private byte[] encapsulatedKey;
    private String groupName;
    private UserInfo operator;
    private int id;
    private List<IUserInfo> members;

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GroupInfo)) {
            return false;
        }
        GroupInfo otherGroup = (GroupInfo) other;
        if (otherGroup.getId() != this.getId()
                || !otherGroup.getGroupName().equals(this.getGroupName())) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * {@inheritDoc}
     */
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public String getKeyId() {
        return groupName;
    }

    /**
     * {@inheritDoc}
     */
    public List<IUserInfo> getMembers() {
        return members;
    }
    
    /**
     * 
     * @param members
     */
    public void setMembers(List<IUserInfo> members) {
        this.members = members;
    }

    /**
     * {@inheritDoc}
     */
    public IUserInfo getOperator() {
        return operator;
    }
    
    /**
     * 
     * @param operator
     */
    public void setOperator(final UserInfo operator) {
        this.operator = operator;
    }
    
    /**
     * {@inheritDoc}
     */
    public byte[] getWrappedKey() {
        return encapsulatedKey;
    }


    /**
     * 
     * @param encapsulatedKey
     */
    public void setEncapsulatedKey(byte[] encapsulatedKey) {
        this.encapsulatedKey = encapsulatedKey;
    }


    /**
     * 
     * @param groupName
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    /**
     * 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Group #" + id + "\nName: " + groupName + "\nKey: " + DatatypeConverter.printHexBinary(encapsulatedKey) + "\nOperator: " + operator.toString();
    }
    
}
