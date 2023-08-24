/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

/**
 *
 * @author rgsaavedra
 */
public abstract class CommStackHandler<E extends LoginDetails> {
    private E loginDetails;
    public abstract void close();
    public abstract boolean isConnected();

    public E getLoginDetails() {
        return loginDetails;
    }

    public void setLoginDetails(E loginDetails) {
        this.loginDetails = (E) loginDetails;
    }
    
    //public abstract void setLoginDetails(E element);
    
}
