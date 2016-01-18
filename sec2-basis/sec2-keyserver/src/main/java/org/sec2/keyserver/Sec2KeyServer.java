/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.keyserver;


import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 *
 * @author hiphop-dave
 *
 */

@javax.jws.WebService
@SOAPBinding(style = Style.RPC)
public class Sec2KeyServer {
    
      public String sayHello(String name) {
    if (name == null) {
      return "Hello";
    }

    return "Hello, " + name + "!";
  }
    
}

