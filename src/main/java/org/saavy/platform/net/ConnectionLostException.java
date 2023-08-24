/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

/**
 *
 * @author rgsaavedra
 */
public class ConnectionLostException extends CommStackException {
    public ConnectionLostException(String msg){
        super(msg);
    }
    public ConnectionLostException(String msg,Throwable th){
        super(msg, th);
    }
}
