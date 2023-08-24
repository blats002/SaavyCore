package org.saavy.platform.net;


import org.saavy.property.PropertyTableBean;

// <editor-fold defaultstate="collapsed" desc=" UML Marker ">
// #[regen=yes,id=DCE.64B13F79-9A6F-EBC6-9C15-23791A7A46D4]
// </editor-fold> 
public class LoginDetails extends PropertyTableBean {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.9E2BB825-E3AA-E2DA-C561-85ABD4269F2B]
    // </editor-fold> 
    public LoginDetails() {
        super("LoginDetails");
        setProperty("isloggedin",Boolean.FALSE);
    }

    public void close() {
        setProperty("isclossed",Boolean.TRUE);
    }
    
    public void setConnected(){
        setProperty("isclossed",Boolean.FALSE);
    }
    
    public boolean isClose(){
        return (Boolean) getProperty("isclossed");
    }
    
    public void setLoggedIn(boolean loggedin){
        setProperty("isloggedin", Boolean.valueOf(loggedin));
    }
    
    public boolean isLoggedIn(){
        return (Boolean) getProperty("isloggedin");
    }
}

