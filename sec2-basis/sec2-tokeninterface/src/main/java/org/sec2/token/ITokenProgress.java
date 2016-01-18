/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.token;

/**
 *
 * @author benedikt
 */
public interface ITokenProgress {

    public void init(int maxSteps);
    public void update(String desc);
    public void done();
}
