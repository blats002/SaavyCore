/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.snmp;


/**
 *
 * @author rgsaavedra
 */
public enum SNMPEnum {
    HOST,
    RETRIES,
    TIMEOUT,
    VERSION,
    PORT,
    PORT_TRAP,
    GET_COMMUNITY,
    SET_COMMUNITY,
    V3_USERNAME,
    SECURITY_LEVEL,
    AUTHENTICATION_PROTOCOL,
    AUTHENTICATION_PASSWORD,
    PRIVACY_PROTOCOL,
    PRIVACY_PASSWORD,
    MIB_FILES;
    
    public enum PrivacyProtocol{
        DES,
        AES;
    }
    
    public enum AuthenticationProtocol{
        SHA,
        MD5;
    }
    
    public enum Version{
        V1,
        V2C,
        V3;
        @Override
        public String toString() {
            return this.equals(V1)?"v1":this.equals(V2C)?"v2c":this.equals(V3)?"v3":"v1";
        }
    }
    
    public enum SecurityLevel{
        NO_AUTH_NO_PRIV,
        AUTH_NO_PRIV,
        AUTH_PRIV;
        
        @Override
        public String toString() {
            if(this.equals(NO_AUTH_NO_PRIV)){
                return "No Auth / No Priv";
            }else if(this.equals(AUTH_NO_PRIV)){
                return "Auth / No Priv";
            }else if(this.equals(AUTH_PRIV)){
                return "Auth / Priv";
            }
            return super.toString();
        }
    }
}
