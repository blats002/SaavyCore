/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

/**
 *
 * @author rgsaavedra
 */
public class CommStackException extends Exception{
    public CommStackException(String msg){
        super(msg);
    }
    
    public CommStackException(String msg, Throwable th){
        super(msg,th);
    }

}
