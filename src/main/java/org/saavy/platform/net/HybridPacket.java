/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

import org.saavy.dom.SaavyElement;

import java.util.ArrayList;

/**
 *
 * @author rgsaavedra
 */
public class HybridPacket extends Packet{
    private ArrayList<Packet> packets = new ArrayList<Packet>();

    public HybridPacket(SaavyElement packetElement) {
        super(packetElement);
    }
    
    @Override
    public void setPacketElement(SaavyElement packetElement) {
        this.to = packetElement.getAttribute("to");
        this.from = packetElement.getAttribute("from");
        this.packetElement = packetElement.copyElement();
    }
    
    public ArrayList<Packet> getPackets(){
        return packets;
    }
    public void addPacket(int index,Packet packet){
        getPacketElement().addChildren(index,packet.getPacketElement().copyElement());
        packets.add(index, packet);
    }
    public void addPacket(Packet packet) {
        getPacketElement().addChildren(packet.getPacketElement().copyElement());
        packets.add(packet);
    }
}
