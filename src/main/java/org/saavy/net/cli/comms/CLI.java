/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.cli.comms;

/**
 *
 * @author rgsaavedra
 */
public class CLI {
    public enum TYPE {
        SSH,
        TELNET
    }
    public enum MODE {
        USER,
        PRIVILEGE,
        CONFIGURE,
        CURRENT
    }
}
