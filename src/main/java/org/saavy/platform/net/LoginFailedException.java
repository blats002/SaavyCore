/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

/**
 *
 * @author rgsaavedra
 */
public class LoginFailedException extends CommStackException {
    public LoginFailedException(String msg){
        super(msg);
    }
    public LoginFailedException(String msg,Throwable th){
        super(msg, th);
    }
}
