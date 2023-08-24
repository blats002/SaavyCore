/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.cli;

import org.saavy.dom.SaavyElement;
import org.saavy.net.cli.comms.CLI;
import org.saavy.platform.net.Packet;

/**
 *
 * @author rgsaavedra
 */
public class CLIPacket extends Packet {

    public CLIPacket(String cliName,String to) {
        super(SaavyElement.createXML("<"+cliName+" to='"+to+"'/>"));
    }

    public CLIPacket(String cliName, String to, String password) {
        super(SaavyElement.createXML("<"+cliName+" to='"+to+"' secpass='"+password+"'/>"));
    }
    
    public SaavyElement addCLIWithRegexBeforeSend(String regexbeforesend, String command){
        return addCLIWithRegexBeforeSend(regexbeforesend, command, true);
    }
    
    public SaavyElement addCLIWithRegexBeforeSend(String regexbeforesend, String command, boolean haltonerror){
        SaavyElement cli = createCLIScript(CLI.MODE.CURRENT, command);
        cli.setAttribute("regexbeforesend", regexbeforesend);
        cli.setAttribute("haltonerror", haltonerror);
        return cli;
    }
    public SaavyElement addCLICurrent(String command){
        return addCLICurrent(command, true);
    }
    
    public SaavyElement addCLICurrent(String command, boolean haltonerror){
        SaavyElement cli = createCLIScript(CLI.MODE.CURRENT, command);
        cli.setAttribute("haltonerror", haltonerror);
        return cli;
    }
    public SaavyElement addCLICONFIGURE(String command){
        return addCLICONFIGURE(command, true);
    }
    
    public SaavyElement addCLICONFIGURE(String command, boolean haltonerror){
        SaavyElement cli = createCLIScript(CLI.MODE.CONFIGURE, command);
        cli.setAttribute("haltonerror", haltonerror);
        return cli;
    }
    
    public SaavyElement addCLIPRIVILEGE(String command){
        return addCLIPRIVILEGE(command, true);
    }
    
    public SaavyElement addCLIPRIVILEGE(String command, boolean haltonerror){
        SaavyElement cli = createCLIScript(CLI.MODE.PRIVILEGE, command);
        cli.setAttribute("haltonerror", haltonerror);
        return cli;
    }
    public SaavyElement addCLIUSER(String command){
        return addCLIUSER(command, true);
    }
    
    public SaavyElement addCLIUSER(String command, boolean haltonerror){
        SaavyElement cli = createCLIScript(CLI.MODE.USER, command);
        cli.setAttribute("haltonerror", haltonerror);
        return cli;
    }

//    if (character == '<') {
//        result.append("&lt;");
//      }
//      else if (character == '>') {
//        result.append("&gt;");
//      }
//      else if (character == '\"') {
//        result.append("&quot;");
//      }
//      else if (character == '\'') {
//        result.append("&#039;");
//      }
//      else if (character == '&') {
//         result.append("&amp;");
//      }

    
    private SaavyElement createCLIScript(CLI.MODE mode, String command){
        SaavyElement cli = null;
        command = command.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#039;");
        if(mode.equals(CLI.MODE.CURRENT)){
            cli = SaavyElement.createXML("<cli command='"+command+"'/>");
        }else{
            cli = SaavyElement.createXML("<cli mode='"+mode+"' command='"+command+"'/>");
        }
        getPacketElement().addChildren(cli);
        return cli;
    }
    
}
