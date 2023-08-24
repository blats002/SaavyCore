/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.snmp;

import com.ireasoning.protocol.snmp.SnmpConst;
import org.saavy.dom.SaavyElement;
import org.saavy.platform.net.LoginDetails;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rgsaavedra
 */
public class SNMPDetails extends LoginDetails {
//    private SNMPEnum.Version version = Version.V1;
//    private SNMPEnum.AuthenticationProtocol authProtocol = AuthenticationProtocol.MD5;
//    private SNMPEnum.PrivacyProtocol privProtocol = PrivacyProtocol.DES;
//    private SNMPEnum.SecurityLevel securityLevel = SecurityLevel.NO_AUTH_NO_PRIV;
//    private
//
//    String v3UserName = "";
//    private String authPassword;
//    private String privPassword;
//    private
//
//    int retries = 2;
//    private int timeout = 5;
//    private
//
//    String writeCommunity = "private";
//    private String readCommunity = "public";
//    private
//
//    int portAgent = SnmpConst.DEFAULT_SNMP_AGENT_PORT;
//    private int portTraps = SnmpConst.DEFAULT_SNMP_MANAGER_PORT;
    
    ArrayList<String> mibFiles;
    
    public SNMPDetails(){
        super();
        mibFiles = new ArrayList<String>();
        set(SNMPEnum.VERSION, SNMPEnum.Version.V2C);
        set(SNMPEnum.AUTHENTICATION_PROTOCOL, SNMPEnum.AuthenticationProtocol.MD5);
        set(SNMPEnum.PRIVACY_PROTOCOL, SNMPEnum.PrivacyProtocol.DES);
        set(SNMPEnum.SECURITY_LEVEL, SNMPEnum.SecurityLevel.NO_AUTH_NO_PRIV);
        set(SNMPEnum.V3_USERNAME,"");
        set(SNMPEnum.RETRIES,2);
        set(SNMPEnum.TIMEOUT,5);
        set(SNMPEnum.SET_COMMUNITY,"private");
        set(SNMPEnum.GET_COMMUNITY,"public");
        set(SNMPEnum.PORT,SnmpConst.DEFAULT_SNMP_AGENT_PORT);
        set(SNMPEnum.PORT_TRAP,SnmpConst.DEFAULT_SNMP_MANAGER_PORT);
    }
    
    @Override
    public void set(String key,Object obj){
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public void set(String arg0, String arg1, Object arg2) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public void set(String arg0, int arg1, Object arg2) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public Object get(String key) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public Object get(String arg0, String arg1) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public Object get(String arg0, int arg1) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    void setData(SaavyElement element) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void set(Enum enm,Object obj){
        super.setProperty(enm.toString(), obj);
    }
    
    private Object get(Enum enm){
        return super.getProperty(enm.toString());
    }
    
    public boolean isV3(){
        return getVersion().equals(SNMPEnum.Version.V3);
    }
    
    public String getHost(){
        return (String) get(SNMPEnum.HOST);
    }
    
    public void setHost(String host){
        set(SNMPEnum.HOST,host);
    }

    public SNMPEnum.Version getVersion() {
        return (SNMPEnum.Version) get(SNMPEnum.VERSION);
    }

    public void setVersion(SNMPEnum.Version version) {
        if(version == null){
            version = SNMPEnum.Version.V1;
        }
        set(SNMPEnum.VERSION,version);
    }

    public SNMPEnum.AuthenticationProtocol getAuthProtocol() {
        return (SNMPEnum.AuthenticationProtocol) get(SNMPEnum.AUTHENTICATION_PROTOCOL);
    }

    public void setAuthProtocol(SNMPEnum.AuthenticationProtocol authProtocol) {
        if(authProtocol == null){
            authProtocol = SNMPEnum.AuthenticationProtocol.SHA;
        }
        set(SNMPEnum.AUTHENTICATION_PROTOCOL,authProtocol);
    }

    public SNMPEnum.PrivacyProtocol getPrivProtocol(){
        return (SNMPEnum.PrivacyProtocol) get(SNMPEnum.PRIVACY_PROTOCOL);
    }

    public void setPrivProtocol(SNMPEnum.PrivacyProtocol privProtocol) {
        if(privProtocol == null){
            privProtocol = SNMPEnum.PrivacyProtocol.DES;
        }
        set(SNMPEnum.PRIVACY_PROTOCOL,privProtocol);
    }

    public SNMPEnum.SecurityLevel getSecurityLevel() {
        return (SNMPEnum.SecurityLevel) get(SNMPEnum.SECURITY_LEVEL);
    }

    public void setSecurityLevel(SNMPEnum.SecurityLevel securityLevel) {
        if(securityLevel == null){
            securityLevel = SNMPEnum.SecurityLevel.NO_AUTH_NO_PRIV;
        }
        set(SNMPEnum.SECURITY_LEVEL,securityLevel);
    }

    public String getV3UserName() {
        return (String) get(SNMPEnum.V3_USERNAME);
    }

    public void setV3UserName(String v3UserName) {
        if(v3UserName == null){
            v3UserName = "";
        }
        set(SNMPEnum.V3_USERNAME,v3UserName);
    }

    public String getAuthPassword() {
        if(isV3()){
            return (String) get(SNMPEnum.AUTHENTICATION_PASSWORD);
        }
        return null;
    }

    public void setAuthPassword(String authPassword) {
        set(SNMPEnum.AUTHENTICATION_PASSWORD,authPassword);
    }

    public String getPrivPassword() {
        if(isV3()){
            return (String) get(SNMPEnum.PRIVACY_PASSWORD);
        }
        return null;
    }

    public void setPrivPassword(String privPassword) {
        set(SNMPEnum.PRIVACY_PASSWORD,privPassword);
    }

    public int getRetries() {
        return (Integer) get(SNMPEnum.RETRIES);
    }

    public void setRetries(int retries) {
        set(SNMPEnum.RETRIES,retries);
    }

    public int getTimeout() {
        return (Integer) get(SNMPEnum.TIMEOUT);
    }

    public void setTimeout(int timeout) {
        set(SNMPEnum.TIMEOUT,timeout);
    }

    public String getWriteCommunity() {
        return (String) get(SNMPEnum.SET_COMMUNITY);
    }

    public void setWriteCommunity(String writeCommunity) {
        set(SNMPEnum.SET_COMMUNITY,writeCommunity);
    }

    public String getReadCommunity() {
        return (String) get(SNMPEnum.GET_COMMUNITY);
    }

    public void setReadCommunity(String readCommunity) {
        set(SNMPEnum.GET_COMMUNITY,readCommunity);
    }

    public int getPortAgent() {
        return (Integer) get(SNMPEnum.PORT);
    }

    public void setPortAgent(int portAgent) {
        set(SNMPEnum.PORT,portAgent);
    }
    
    public void setPortAgent(String portAgent){
        try {
            setPortAgent(Integer.parseInt(portAgent));
        } catch (NumberFormatException numberFormatException) {
            set(SNMPEnum.PORT,SnmpConst.DEFAULT_SNMP_AGENT_PORT);
        }
    }

    public int getPortTraps() {
        return (Integer) get(SNMPEnum.PORT_TRAP);
    }

    public void setPortTraps(int portTraps) {
        set(SNMPEnum.PORT_TRAP,portTraps);
    }
    
    public List<String> getMibFiles(){
        return (List<String>) get(SNMPEnum.MIB_FILES);
    }
    
    public void setMibFiles(List list){
        set(SNMPEnum.MIB_FILES,list);
    }
}
