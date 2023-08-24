/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

/**
 *
 * @author rgsaavedra
 */
public class ConnectionFailedException extends CommStackException {
    public ConnectionFailedException(String msg){
        super(msg);
    }
    public ConnectionFailedException(String msg,Throwable th){
        super(msg, th);
    }

}
