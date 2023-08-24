/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.cli;

import org.saavy.platform.net.CommStackHandler;

/**
 *
 * @author rgsaavedra
 */
public class CLIHandler extends CommStackHandler<CLIDetails> {
    
    public CLIHandler(){
        setLoginDetails(new CLIDetails());
    }
    
    @Override
    public void close() {
        
    }

    @Override
    public boolean isConnected() {
        return true;
    }
    
    public CLISession createCLISession(){
        return new CLISession(getLoginDetails());
    }

}
