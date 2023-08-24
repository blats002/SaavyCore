/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.snmp;

import org.saavy.dom.SaavyElement;
import org.saavy.platform.net.Packet;

/**
 *
 * @author rgsaavedra
 */
public class SNMPPacket extends Packet {

    public SNMPPacket(String name,String to) {
        super(new SaavyElement(name).addAttribute("to", to));
    }

    public SNMPPacket(String name,String to,boolean prioritize) {
        super(SaavyElement.createXML("<"+name+" to='"+to+"' prioritize='"+prioritize+"'/>"));
    }
    
    public SaavyElement addGetPDU(String oid){
        SaavyElement pdu = addPDU(oid, "get");
        return pdu;
    }
    
    public SaavyElement addGetTablePDU(String oid){
        SaavyElement pdu = addPDU(oid, "gettable");
        return pdu;
    }
    
    public SaavyElement addGetTablePDU(String oid, String indexlookup){
        SaavyElement pdu = addGetTablePDU(oid);
        pdu.setAttribute("indexlookup", indexlookup);
        return pdu;
    }
    
    public SaavyElement addGetTreePDU(String oid){
        SaavyElement pdu = addPDU(oid, "tree");
        return pdu;
    }
    
    public SaavyElement addGetTreePDU(String oid, String valuelookup){
        SaavyElement pdu = addGetTreePDU(oid);
        pdu.setAttribute("valuelookup", valuelookup);
        return pdu;
    }
    
    public SaavyElement addSetPDU(String oid, Object value){
        SaavyElement pdu = addPDU(oid, "set");
        pdu.setAttribute("value", value);
        return pdu;
    }
    
    public enum SNMPSETTYPE{
        oid{
            @Override
            public String toString(){
                return "oid";
            }
        },
        hex{
            @Override
            public String toString(){
                return "hex";
            }
        },
        ipaddress{
            @Override
            public String toString(){
                return "ipaddress";
            }
        },
        none{
            @Override
            public String toString(){
                return "";
            }
        };
    }
    
    public SaavyElement addSetPDU(String oid, Object value, SNMPSETTYPE type){
        SaavyElement pdu = addSetPDU(oid,value);
        pdu.setAttribute("type", type.toString());
        return pdu;
    }
    
    public SaavyElement setProcessPriorIfPDU(SaavyElement snmp, boolean processif){
        snmp.setAttribute("processif", processif);
        return snmp;
    }

    public SaavyElement setProcessPriorIfPDU(SaavyElement snmpSuccess, SaavyElement snmpFailed){
        SaavyElement pdu = SaavyElement.createXML("<pdu action='choiceset'/>");
        pdu.addChildren(snmpSuccess);
        pdu.addChildren(snmpFailed);
        return pdu;
    }
    
    public SaavyElement addSetPDUS(SNMPPacket setPacket){
        SaavyElement pdu = SaavyElement.createXML("<pdu action='multipleset'/>");
        pdu.setAttribute("grouppacket", setPacket);
        getPacketElement().addChildren(pdu);
        return pdu;
    }
    
    private SaavyElement addPDU(String oid, String action){
        SaavyElement pdu = SaavyElement.createXML("<pdu oid='"+oid+"' action='"+action+"'/>");
        getPacketElement().addChildren(pdu);
        return pdu;
    }
}
