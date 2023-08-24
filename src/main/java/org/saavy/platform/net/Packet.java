/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.platform.net;

import org.saavy.dom.SaavyElement;

/**
 *
 * @author rgsaavedra
 */
public class Packet {

    public static Packet createXML(String string) {
        return Packet.createPacket(SaavyElement.createXML(string));
    }
    
    protected String to = "";
    protected String from = "";
    protected SaavyElement packetElement;

    public Packet(SaavyElement packetElement) {
        setPacketElement(packetElement);
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
        packetElement.setAttribute("to", to);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
        packetElement.setAttribute("from", from);
    }

    public SaavyElement getPacketElement() {
        return packetElement;
    }

    public void setPacketElement(SaavyElement packetElement) {
        this.to = packetElement.getAttribute("to");
        this.from = packetElement.getAttribute("from");
        this.packetElement = packetElement.copyElement();
    }
    
//    private static SaavyElement copyElement(SaavyElement element){
////        SaavyElement copy = new SaavyElement();
////        copy.setName(element.getName());
////        copy.copyAttributes(element);
////        copy.setText(element.getText());
////        for(SaavyElement child:element.getChildren()){
////            copy.addChildren(copyElement(child));
////        }
//        return element.copyElement();
//    }
    
    public static Packet createPacket(SaavyElement element){
        return new Packet(element);
    }
    
    public static Packet createResponsePacket(SaavyElement element){
        String to = element.getAttribute("from");
        String from = element.getAttribute("to");
        Packet reply = new Packet(element);
        reply.setTo(to);
        reply.setFrom(from);
        return reply;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        packetElement = null;
    }
    
}
