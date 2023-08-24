/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;


import org.saavy.dom.SaavyElement;
import org.saavy.platform.Engine;
import org.saavy.platform.Module;

/**
 *
 * @author rgsaavedra
 */
public abstract class CommStack<E extends CommStackHandler> implements Engine {
    
    private String id;
    
    private E handler;
    
    private boolean loggedIn = false;
    
    public CommStack(E handler){
        setHandler(handler);
    }
    
    public void init(SaavyElement init){
        
    }
    
    public abstract void start();
        
    public abstract void stop();
    
    public abstract boolean isConnected();
    
    
    
    public abstract void doAction(Module module, Packet req);
    
    public LoginDetails getLoginDetails(){
        return (LoginDetails) handler.getLoginDetails();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public E getHandler() {
        return (E) handler;
    }

    public void setHandler(E handler) {
        this.handler = handler;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Packet forceAction(Module module,Packet request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
