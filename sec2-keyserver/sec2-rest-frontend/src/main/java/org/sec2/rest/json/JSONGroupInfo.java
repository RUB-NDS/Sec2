/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sec2.backend.IGroupInfo;
import org.sec2.backend.IUserInfo;
import org.sec2.rest.GroupType;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class JSONGroupInfo extends JSONObject {

    public final static String GROUPNAME = "";

    public JSONGroupInfo(IGroupInfo gi) {

        super();

        this.put(GroupType.NAME.toString(), gi.getGroupName());
        this.put(GroupType.KEY_ID.toString(), gi.getKeyId());

        JSONArray members = new JSONArray();
        for (IUserInfo ui : gi.getMembers()) {
            members.add(new JSONUserInfo(ui));
        }
        this.put(GroupType.MEMBERS.toString(), members);
        this.put(GroupType.OPERATOR.toString(), gi.getOperator());
        this.put(GroupType.WRAPPED_KEY.toString(), gi.getWrappedKey());

    }
}
