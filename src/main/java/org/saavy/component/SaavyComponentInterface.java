/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.component;

import org.saavy.bean.SaavyBean;
import org.saavy.platform.Module;
import org.saavy.platform.net.Packet;
import org.saavy.platform.net.Poll;

/**
 *
 * @author rgsaavedra
 */
public interface SaavyComponentInterface<E extends SaavyComponentInterface> {

    public void destroy();

    public E getSubComponent(String id);

    public void packetReceived(Packet packet);

    public void setId(String id);

    public void setModule(Module module);
    
    public SaavyBean getATLPBean(String id);
    
    public void subBeanUpdated(SaavyBean bean);
    
    public String getCannonicalID();
    
    public String getId();
    
    public boolean continuePoll(Poll poll);
}
