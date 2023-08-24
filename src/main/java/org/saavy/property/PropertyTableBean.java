package org.saavy.property;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;

import java.util.HashMap;

/**
 *
 * @author rgsaavedra
 */
public class PropertyTableBean extends DataBean {
    private String name;
    /*columns
     * propertyName varchar[100]
     * value varchar[100]
     */
    public PropertyTableBean(String name) {
        super(new BasicDynaClass("org.saavy.property.PropertyTableBean", null,
                new DynaProperty[]{
                    new DynaProperty("properties", HashMap.class)
                }
        ));
        this.name = name;
        
//        JavaDBPropertyTableHandler handler = new JavaDBPropertyTableHandler();
//        
//        handler.setTableName(name.toUpperCase()+"_PROP_TBL");
//        
//        setDatabaseBeanHandler(handler);
        
        HashMap<String,PropertyBean> beans = new HashMap<String,PropertyBean>();
        super.set("properties", beans);
    }

    public int size(){
        return ((HashMap)super.get("properties")).size();
    }

    public Object getProperty(String key) {
        Object obj = super.get("properties",key);
        if(obj != null){
            return ((PropertyBean)obj).getPropertyValue();
        }
        return null;
    }
    
     public void setProperty(String key,Object value){
        Object obj = super.get("properties",key);
        if(obj == null){
            obj = new PropertyBean(key);
            super.set("properties",key,obj);
        }
        if(!((PropertyBean)obj).equals(value)){
            setModifiedOrNew(true);
            if(value==null){
                super.remove("properties", key);
            }else{
                ((PropertyBean)obj).setPropertyValue(value);
            }
        }
        
    }
}
