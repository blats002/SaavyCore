/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.platform.net;

import org.saavy.dom.SaavyElement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rgsaavedra
 */
public class Poll {

    private String moduleID;
    private SaavyElement pollElement;
    private ArrayList<String> registered;
    private boolean polling = false;

    public Poll() {
        registered = new ArrayList<String>();
    }

    public void register(String id) {
        if (!registered.contains(id)) {
            registered.add(id);
        }
    }

    public List<String> getRegistered() {
        return registered;
    }

    public SaavyElement getPollElement() {
        return pollElement;
    }
    private boolean pause = false;
    private boolean force = false;
    
    public void setForcePoller(boolean force) {
        this.force = force;
    }
    
    public boolean isForcePoller(){
        return this.force;
    }
    

    public void setPollElement(SaavyElement pollElement) {
        this.pollElement = pollElement;
//        if(pollElement.getAttribute("commstack","false").equalsIgnoreCase("true")){
//            for(SaavyElement subPoll:this.pollElement.getChild("request").getChildren()){
//                subPoll.setAttribute("commstack", "true");
//            }
//        }
        for (SaavyElement reg : pollElement.getChildren("listen")) {
            if (reg.hasAttribute("id")) {
                //System.out.println("Registerring:"+reg.getAttribute("id", ""));
                register(reg.getAttribute("id", ""));
            }
        }
        
        for (SaavyElement reg : pollElement.getChildren("show")) {
            if(reg.getAttribute("id","").endsWith("@component")){
                components.add(reg.getAttribute("id"));
            }
        }
        
    }
    
    public ArrayList<String> continuePoll(){
        return components;
    }
    
    private ArrayList<String> components = new ArrayList<String>();
    
    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public boolean isPolling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
    }
    private String id;
    public void setID(String id) {
        this.id = id;
    }
    public String getID(){
        return id;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }
    private long lastSend;
    void updateLastSend() {
        lastSend = System.currentTimeMillis();
    }

    long getLastTimeSend(){
        return lastSend;
    }
}
