/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.platform;

import org.saavy.bean.SaavyBean;
import org.saavy.component.SaavyComponentInterface;
import org.saavy.dom.SaavyElement;
import org.saavy.platform.net.Packet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rgsaavedra
 */
public abstract class Module<E extends SaavyComponentInterface> {

    private String id;
    private Properties properties;
    private Properties xmlLocation;
    private SaavyContainer container;
    private HashMap<String, E> components;

    public Module() {
        components = new HashMap<String, E>();
    }

    public void invokePoll(String string) {
        getSaavyManager().getCommStackManager().getPoller().requestPoll(string);
    }

    public void removePolls(String cannonicalID) {
        if (getSaavyManager() != null) {
            getSaavyManager().removePolls(cannonicalID);
        }
    }
//    protected void processSendResponse(String to,SaavyElement element){
//        try {
//            if (to.contains("@component")) {
//                SaavyComponentInterface comp = getSaavyComponent(to.replaceAll("\\@component", ""));
//                if (comp != null) {
//                    comp.packetReceived(element);
//                }
//            } else if (to.contains("@commstack")) {
//                element.setAttribute("module", getId());
//                getSaavyManager().getCommStackManager().sendRequest(element);
//            } else if (to.contains("@bean")) {
//                SaavyBean bean = getSaavyBean(to.replaceAll("\\@bean", ""));
//                if (bean != null) {
//                    bean.packetReceived(element);
//                }
//            } else if (to.contains("@poll")) {
//                getSaavyManager().getCommStackManager().getPoller().packetReceived(element);
//            }
//        } catch (Exception e) {
//            Logger.getLogger(Module.class.toString()).log(Level.SEVERE,element.getXML(),e);
//        }
//    }

    
    
    public void send(final Packet packet) {
        String to = packet.getTo();
//        processSendResponse(to, element);
        try {
            if (to.matches(".*\\@?component$")) {
                SaavyComponentInterface comp = getSaavyComponent(to.replaceAll("\\@component", ""));
                if (comp != null) {
                    comp.packetReceived(packet);
                }
            } else if (to.matches(".*\\@?commstack$")) {
                if (!getProperties().get("HOST").toString().equalsIgnoreCase("debug")) {
                    packet.getPacketElement().setAttribute("module", getId());
                    getSaavyManager().getCommStackManager().sendRequest(packet);
                }
            } else if (to.matches(".*\\@?commstack.hybrid$")) {
                getSaavyManager().getCommStackManager().addToHybridRequest(packet);
            } else if (to.matches(".*\\@?bean$")) {
                SaavyBean bean = getSaavyBean(to.replaceAll("\\@bean", ""));
                if (bean != null) {
                    bean.packetReceived(packet);
                }
            } else if (to.matches(".*\\@?poll$")) {
                getSaavyManager().getCommStackManager().getPoller().packetReceived(packet);
            } else {
                Logger.getLogger(Module.class.toString()).log(Level.SEVERE, "Packet Receipient(" + to + ") not found:" + packet.getPacketElement().getXML());
            }
        } catch (Exception e) {
            Logger.getLogger(Module.class.toString()).log(Level.SEVERE, packet.getPacketElement().getName(), e);
        }
    }

    public Collection<E> getAllSaavyComponent() {
        return components.values();
    }

    public E getSaavyComponent(String id) {
        id = id.replaceAll("\\@component", "");
        try {
            if (id.contains(".")) {

                return (E) components.get(id.substring(0, id.indexOf("."))).getSubComponent(id.substring(id.indexOf(".") + 1));

            }
            return components.get(id);
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public void processSendResponse(Packet responsePacket, Packet requestPacket){
        send(responsePacket);
    }
            

    public void removeALLSaavyComponents() {
        for (SaavyComponentInterface compnt : components.values()) {
            compnt.destroy();
        }
        components.clear();
    }

    public void removeSaavyComponent(String id) {
        components.remove(id);
    }

    public void addSaavyComponent(String id, E comp) {
        comp.setModule(this);
        comp.setId(id);
        components.put(id, comp);
    }

    public SaavyContainer getContainer() {
        return container;
    }

    public void setContainer(SaavyContainer container) {
        this.container = container;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //public abstract SaavyElement getModuleConfig();
    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public abstract void init(SaavyElement element, SaavyContainer obj);

    public abstract void destroy();

    public Properties getModuleProperties() {
        return xmlLocation;
    }

    public void setModuleProperties(Properties xmlLocation) {
        this.xmlLocation = xmlLocation;
    }
    private SaavyManager manager;

    public void setSaavyManager(SaavyManager aThis) {
        this.manager = aThis;
    }

    public SaavyManager getSaavyManager() {
        return manager;
    }
    
    public SaavyBean getSaavyBean(String id) {
        id = id.replaceAll("\\@bean", "");
        Pattern pattern = Pattern.compile("(.*)\\.(\\w*)");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            String compID = matcher.group(1);
            String beanID = matcher.group(2);
            SaavyComponentInterface comp = getSaavyComponent(compID);
            if (comp != null) {
                return comp.getSaavyBean(beanID);
            }
        }
        return null;
    }
}
