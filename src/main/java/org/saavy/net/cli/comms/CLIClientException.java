/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.cli.comms;

/**
 *
 * @author rgsaavedra
 */
public class CLIClientException extends Exception{
    public CLIClientException(String msg,Throwable th){
        super(msg, th);
    }
    public CLIClientException(String msg){
        super(msg);
    }
}
