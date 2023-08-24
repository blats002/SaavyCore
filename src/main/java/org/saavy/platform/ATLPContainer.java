/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform;

/**
 *
 * @author rgsaavedra
 */
public interface ATLPContainer<E> {
    public void set(E obj);
    public void remove();
    public void logout();
}
