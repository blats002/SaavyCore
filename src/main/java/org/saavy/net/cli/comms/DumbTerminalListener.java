/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.cli.comms;


/**
 *
 * @author rgsaavedra
 */
public interface DumbTerminalListener {
     public void clientClosed();
     public void clientConnected();
     public void clientError(CLIClientException e);
}
