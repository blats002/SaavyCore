/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.platform;

import org.saavy.dom.SaavyElement;
import org.saavy.platform.net.CommStack;
import org.saavy.platform.net.CommStackManager;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class SaavyManager {

    private HashMap<String, Module> modules;
    private CommStackManager manager;

    public SaavyManager() {
        modules = new HashMap<String, Module>();
        manager = new CommStackManager(this);
    }

    public void addModule(String id, Module module) {
        module.setSaavyManager(this);
        module.setId(id);
        modules.put(id, module);
    }

    public Module getModule(String id) {
        return modules.get(id);
    }

    public void removeModule(String id) {
        modules.remove(id).destroy();
    }

    public void registerModule(SaavyElement element, Properties props, SaavyContainer obj) {
        String id = element.getAttribute("id");
        try {
            String clazz = element.getAttribute("class");
            Module module = (Module) Class.forName(clazz).newInstance();
            module.setId(id);
            module.setProperties(props);
            try {
                getCommStackManager().getPoller().setIntervalInMillis(Integer.parseInt(props.getProperty("polltime", "30")));
//                System.out.println("Poller Millis:"+getCommStackManager().getPoller().getIntervalInMillis());
            } catch (NumberFormatException numberFormatException) {
            }
            //register engines
            for (SaavyElement engine : element.getChild("commstacks").getChildren("commstack")){
                String engineId = engine.getAttribute("id");
                String engineClazz = engine.getAttribute("class");
                try {
                    getCommStackManager().addCommStack(engineId, (CommStack) Class.forName(engineClazz).newInstance());
                    if(element.hasChildren()){
                        for(SaavyElement child : engine.getChildren()){
                            getCommStackManager().getCommStack(engineId).init(child);
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SaavyManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SaavyManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(SaavyManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            addModule(id, module);

            module.init(element, obj);
        } catch (Exception ex) {
            Logger.getLogger(SaavyManager.class.getName()).log(Level.SEVERE, null, ex);
            removeModule(id);
        }

    }

    public void removeAllModule() {
        for (String key : modules.keySet()) {
            removeModule(key);
        }
    }

    public CommStackManager getCommStackManager() {
        return manager;
    }

    public void setCommStackManager(CommStackManager manager) {
        this.manager = manager;
    }

    void removePolls(String cannonicalID) {
        if(getCommStackManager()!=null){
            getCommStackManager().removePolls(cannonicalID);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if(getCommStackManager()!=null){
            getCommStackManager().close();
        }
        super.finalize();
    }


}
