/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.tftp;

import org.saavy.dom.SaavyElement;
import org.saavy.platform.net.Packet;

import java.io.File;
import java.util.ArrayList;



/**
 *
 * @author rgsaavedra
 */
public class TFTPPacket extends Packet {
    
    private ArrayList<Packet> packetsOk = new ArrayList<Packet>(); 
    private ArrayList<Packet> packetsFailed = new ArrayList<Packet>(); 
    
    public TFTPPacket(String name, String to) {
        super(SaavyElement.createXML("<"+name+" to='"+to+"'/>"));
    }
    
    public SaavyElement addFile(File file, String address, TFTP.TYPE type){
        SaavyElement tftp = new SaavyElement();
        tftp.setName("tftp");
        tftp.setAttribute("file", file);
        tftp.setAttribute("address", address);
        tftp.setAttribute("type", type);
        getPacketElement().addChildren(tftp);
        return tftp;
    }
    
    public void addPacketsOk(Packet packet){
        packetsOk.add(packet);
    }

    public ArrayList<Packet> getPacketsOk() {
        return packetsOk;
    }

    public void addPacketsOk(ArrayList<Packet> packetsOk) {
        this.packetsOk = packetsOk;
    }
    
//    public void addPacketsFailed(ArrayList<Packet> packetsOk) {
//        this.packetsFailed = packetsOk;
//    }
//
//    public ArrayList<Packet> getPacketsFailed() {
//        return packetsFailed;
//    }
//
//    public void setPacketsFailed(ArrayList<Packet> packetsFailed) {
//        this.packetsFailed = packetsFailed;
//    }
    
    
    
}
