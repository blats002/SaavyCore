/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

/**
 *
 * @author rgsaavedra
 */
public interface CommStackListener {
    public void errorOccured(CommStackException ex);
    public void packetReceived(Packet element);
}
