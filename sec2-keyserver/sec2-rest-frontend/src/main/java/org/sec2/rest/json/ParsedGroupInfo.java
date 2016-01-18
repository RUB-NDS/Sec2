/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.json;

import java.util.List;
import org.json.simple.JSONArray;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.backend.impl.UserInfo;
import org.sec2.rest.GroupType;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class ParsedGroupInfo implements IGroupInfo {

    private String name;
    private List<IUserInfo> members;
    private String keyID;
    private byte[] wrappedKey;
    private IUserInfo operator;

    public ParsedGroupInfo(JSONGroupInfo g) {

        this.name = (String) g.get(GroupType.NAME.toString());
        this.keyID = (String) g.get(GroupType.KEY_ID.toString());

        JSONArray members = (JSONArray) g.get(GroupType.MEMBERS.toString());

        for (Object ui : members) {
            this.members.add(new ParsedUserInfo((UserInfo) ui));
        }
        this.operator = (IUserInfo) g.get(GroupType.OPERATOR.toString());
        this.wrappedKey = (byte[]) g.get(GroupType.WRAPPED_KEY.toString());
    }

    @Override
    public String getGroupName() {
        return name;
    }

    @Override
    public String getKeyId() {
        return keyID;
    }

    @Override
    public byte[] getWrappedKey() {
        return wrappedKey;
    }

    @Override
    public IUserInfo getOperator() {
        return operator;
    }

    @Override
    public List<IUserInfo> getMembers() {
        return members;
    }
}
