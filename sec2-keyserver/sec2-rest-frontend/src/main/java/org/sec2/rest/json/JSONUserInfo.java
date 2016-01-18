/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.json;

import org.json.simple.JSONObject;
import org.sec2.backend.IUserInfo;
import org.sec2.rest.UserType;

/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class JSONUserInfo extends JSONObject{

    public JSONUserInfo(IUserInfo ui) {
        super();        
        this.put(UserType.USER_E_MAIL,ui.getEmailAddress());
        this.put(UserType.USER_ID,ui.getId());
    }
    
    
}
