/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.bean;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.propertysheet.DefaultProperty;
import org.saavy.component.SaavyComponentInterface;
import org.saavy.dom.SaavyElement;
import org.saavy.dom.SaavyHashMap;
import org.saavy.platform.Module;
import org.saavy.platform.net.Packet;

import javax.swing.table.TableCellRenderer;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.util.ArrayList;

/**
 *
 * @author rgsaavedra
 */
public abstract class SaavyBean<E extends SaavyComponentInterface, M extends Module> {

    private ArrayList<SaavyElement> registeredComponents;
    private M module;
    private E parent;
    private String id;
    //private Object beanObject;
    protected BaseBeanInfo info;
    private SaavyHashMap<String, DefaultProperty> properties;
    protected ArrayList<DefaultProperty> propertyList;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        registeredComponents.clear();
        module = null;
        parent = null;
        info = null;
        properties.clear();
        properties = null;
        propertyList.clear();
        propertyList = null;
    }

    public SaavyBean() {
        properties = new SaavyHashMap<String, DefaultProperty>();
        propertyList = new ArrayList<DefaultProperty>();
        registeredComponents = new ArrayList<SaavyElement>();

    }

    public void beanUserUpdate(String action, Object object) {
    }

    public void beanUserUpdate(String action) {
        beanUserUpdate(action, null);
    }

    public boolean hasError(String propName, Object value) {
        DefaultProperty prop = getProperty(propName);
        try {
            String regexValidation = getBeanInfo().getResources().getString(prop.getName() + ".validation");
            if (regexValidation != null && !regexValidation.isEmpty()) {
                if (!String.valueOf(value).matches(regexValidation)) {
                    return true;
                }
            }
        } catch (java.util.MissingResourceException ex) {
        }
        return false;
    }

    public void addRegisterComponent(SaavyElement regComp) {
        this.getRegisteredComponents().add(regComp);
    }

    public void removeRegisterComponent(String compID) {
        this.getRegisteredComponents().remove(compID);
    }

    public ArrayList<DefaultProperty> getProperties() {
        return propertyList;
    }

    public void setPropertyValue(String propName, Object value) {
        if (properties.containsKey(propName)) {
            properties.get(propName).setValue(value);
        }
    }

    public Object getPropertyValue(String propName) {
        Object obj = null;
        if (properties.containsKey(propName)) {
            obj = properties.get(propName).getValue();
        }
        return obj;
    }

    public void readFromOtherObject(Object obj) {
        for (DefaultProperty prop : properties.values()) {
            prop.readFromObject(obj);
        }
        write();
        fireBeanUpdate();
    }

    public void read() {
        for (DefaultProperty prop : properties.values()) {
            prop.readFromObject(this);
        }
        fireBeanUpdate();
    }

    public void send(Packet packet) {
        if (packet.getFrom().trim().isEmpty()) {
            packet.setFrom(getCanonicalID() + "@bean");
        }
        getModule().send(packet);
    }

    public PropertyEditor getPropertyEditor(int index) {
        return null;
    }

    public TableCellRenderer getCellRenderer(int index) {
        return null;
    }

    protected void fireBeanUpdate() {
        SaavyElement bean = SaavyElement.createXML("<beanupdate/>");

        for (SaavyElement reg : getRegisteredComponents()) {
            String idTemp = reg.getAttribute("id");
            Packet beanPacket = new Packet(bean);
            beanPacket.setTo(idTemp);
            send(beanPacket);
        }
        //TODO:Need to Move this code to AWPlusModule or something
        if (getParent() != null) {
            getParent().subBeanUpdated(this);
        }
    //TODO:Need to Move this code to AWPlusModule or something
    }

    public String getCanonicalID() {
        if (parent != null) {
            return parent.getCannonicalID() + "." + getId();
        }
        return getId();
    }

    public void write() {
        for (DefaultProperty prop : properties.values()) {
            prop.writeToObject(this);
        }

    }

    public BaseBeanInfo getBeanInfo() {
        return info;
    }

    public class SaavyBeanDefaultProperty extends DefaultProperty {
        @Override
        public String toString() {
            return String.valueOf(getValue());
        }

//        @Override
//        public void setValue(Object value) {
//            if(value instanceof String){
//                ZipString zipString = new ZipString();
//                zipString.setString((String)value);
//                super.setValue(zipString);
//            }else{
//                super.setValue(value);
//            }
//        }
//
//        @Override
//        public Object getValue() {
//            Object obj = super.getValue();
//            if(obj instanceof ZipString){
//                obj = ((ZipString)obj).toString();
//            }
//            return obj;
//        }

    }

    protected void setBeanInfo(BaseBeanInfo info) {
        this.info = info;
        for (PropertyDescriptor propDesc : this.info.getPropertyDescriptors()) {
            SaavyBeanDefaultProperty prop = new SaavyBeanDefaultProperty();
            prop.setName(propDesc.getName());
            prop.setDisplayName(propDesc.getDisplayName());
            prop.setCategory(((ExtendedPropertyDescriptor) propDesc).getCategory());
            prop.setShortDescription(propDesc.getShortDescription());
            prop.setType(propDesc.getPropertyType());
            properties.put(propDesc.getName(), prop);
            propertyList.add(prop);
        }
        read();
    }

    public void packetReceived(Packet obj) {
    }

    public M getModule() {
        return module;
    }

    public void setModule(M module) {
        this.module = module;
    }

    public E getParent() {
        return parent;
    }

    public void setParent(E parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<SaavyElement> getRegisteredComponents() {
        return registeredComponents;
    }

    public DefaultProperty getProperty(String propName) {
        DefaultProperty obj = null;
        if (properties.containsKey(propName)) {
            obj = properties.get(propName);
        }
        return obj;
    }

    public boolean hasErrors() {
        for (String propName : properties.keySet()) {
            if (hasError(propName, getPropertyValue(propName))) {
                return true;
            }
        }
        return false;
    }
    private boolean editable = false;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean edit) {
        this.editable = edit;
    }

    public boolean isPropertyEditable(int index) {
        return false;
    }
}
