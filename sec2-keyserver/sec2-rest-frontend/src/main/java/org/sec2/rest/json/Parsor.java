/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.rest.json;

import java.io.IOException;
import net.iharder.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sec2.rest.RestException;


/**
 *
 * @author Thorsten Schreiber <thorsten.schreiber@rub.de>
 */
public class Parsor {
    
    static JSONParser parser= new JSONParser();
    
    public static JSONObject parse(byte[] data) throws RestException{
        try {
            return (JSONObject) parser.parse(new String(data));
        } catch (ParseException ex) {
            throw new RestException(ex);
        }
    }
    
    public static byte[] deparse(JSONObject data)throws RestException{
        try {
            return Base64.decode(data.toString());
        } catch (IOException ex) {
            throw new RestException("Could not make byte from json", ex);
        }
    }
    
}
