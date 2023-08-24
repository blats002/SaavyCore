package org.saavy.property;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class PropertyBean implements Serializable {

    private BasicDynaBean currentBean;
    private BasicDynaBean backup;

    public PropertyBean() {
        BasicDynaClass clss = new BasicDynaClass("property", null,
                new DynaProperty[]{
            new DynaProperty("propertyName", String.class),
            new DynaProperty("value", Object.class)
        });
        currentBean = new BasicDynaBean(clss);
    }
    public PropertyBean(String name) {
        this();
        this.setPropertyName(name);
    }

    public boolean isModifiedOrNew() {
        boolean retVal = false;
        if (backup != null) {
            for (DynaProperty property : currentBean.getDynaClass().getDynaProperties()) {
                Object current = currentBean.get(property.getName());
                Object backp = backup.get(property.getName());
                retVal = !current.equals(backp);
                if (retVal) {
                    break;
                }
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BasicDynaBean backupTemp = new BasicDynaBean(currentBean.getDynaClass());
        try {
            for (DynaProperty property : currentBean.getDynaClass().getDynaProperties()) {
                backupTemp.set(property.getName(), currentBean.get(property.getName()));
            }
        } catch (SecurityException ex) {
            Logger.getLogger(DataBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DataBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return backupTemp;
    }

    protected void backup() {
        try {
            backup = (BasicDynaBean) this.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(PropertyBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPropertyName(String name) {
        currentBean.set("propertyName", name);
    }

    public void setPropertyValue(Object obj) {
        currentBean.set("value", obj);
    }

    public String getPropertyName() {
        return (String) currentBean.get("propertyName");
    }

    public Object getPropertyValue() {
        return currentBean.get("value");
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PropertyBean)){
            return false;
        }
        PropertyBean bean = (PropertyBean)obj;
        return getPropertyName().equalsIgnoreCase(bean.getPropertyName()) && getPropertyValue().equals(bean.getPropertyValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.currentBean != null ? this.currentBean.hashCode() : 0);
        hash = 37 * hash + (this.backup != null ? this.backup.hashCode() : 0);
        return hash;
    }
}
