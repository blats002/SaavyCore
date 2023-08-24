package org.saavy.main;

import org.saavy.net.cli.CLIDetails;
import org.saavy.net.cli.CLISession;
import org.saavy.net.cli.comms.CLI;
import org.saavy.net.cli.comms.CLICommandLine;
import org.saavy.net.cli.comms.CLIResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        CLIDetails details = new CLIDetails();
        details.setHost("ldap-apac.mabelbo.net");
        details.setUsername("rsaavedra");
        details.setPassword("Welcome@2");
        details.setCLITypeUsingDefaultPorts(CLI.TYPE.SSH);
        try {
            CLISession handler = new CLISession(details);
            handler.connect();
//            handler.terminal.addTelnetClientListener(new DumbTerminalListener() {
//
//                public void clientClosed() {
//                    System.out.println("Client Closed");
//                }
//
//                public void clientConnected() {
//                    System.out.println("clientConnected");
//                }
//
//                public void clientError(CLIClientException e) {
//                    e.printStackTrace();
//                }
//            });
            ArrayList<CLICommandLine> scripts = new ArrayList<CLICommandLine>();
            scripts.add(CLICommandLine.createCLICommandLine( "uptime", true));
            List<CLIResponse> response = handler.sendCommandLines(scripts);

            for (CLIResponse responses : response) {
                System.out.println("MODE:" + responses.getMode());
                System.out.println("CL:" + responses.getCommandLine());
                System.out.println("Response:" + responses.getResponse());
                System.out.println("Error:" + responses.isError());
            }

            handler.close();
        } catch (Exception ex) {
            Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.exit(0);
        }
//
//        details.setUsername("fifteen");
//        details.setPassword("15151515");
//        CLISession handler = new CLISession(details);
//        try {
//

//
//            ArrayList<CLICommandLine> scripts = new ArrayList<CLICommandLine>();
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.CONFIGURE, "crypto key generate hostkey dsa 1024", true));
////            scripts.add(CLICommandLine.createCLICommandLine("(.*\\n)*Do.*overwrite.*existing.*key.*\\(yes\\/no\\)\\?", "no", true));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "show license", false));
////            <fileactiondelete to="cli@commstack">
////            <cli mode="PRIVILEGE" command="del recursive "flash:/test"" haltonerror="true"/>
////            <cli regexbeforesend="Delete.*\(y\/n\)\[n\]\:" command="y" haltonerror="true"/>
////            <cli command="" haltonerror="true"/>
////            </fileactiondelete>
//
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "del recursive \"flash:/test\"", true));
////            scripts.add(CLICommandLine.createCLICommandLine("Delete.*\\(y\\/n\\)\\[n\\]\\:", "y", true));
////            scripts.add(CLICommandLine.createCLICommandLine("", true));
////            scripts.add(CLICommandLine.createCLICommandLine("show system", true));
////            scripts.add(CLICommandLine.createCLICommandLine("show spanning-tree", false));
////            scripts.add(CLICommandLine.createCLICommandLine("show spanning-tree", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.CONFIGURE, "stack management vlan 3", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.CONFIGURE, "stack management vlan 2", false));
////            scripts.add(CLICommandLine.createCLICommandLine("", false));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "show system", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "show spanning-tree", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "ping 10.10.20.99", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "ping 10.10.20.99", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "ping 10.10.20.99", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.USER, "ping 10.10.20.99", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "reboot", true));
//            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "n", true));
//            scripts.add(CLICommandLine.createCLICommandLine("", true));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "show log tail 250", false));
////            scripts.add(CLICommandLine.createCLICommandLine(MODE.PRIVILEGE, "reload", true));
////            scripts.add(CLICommandLine.createCLICommandLine("n", true));
//            List<CLIResponse> response = handler.sendCommandLines(scripts);
//            handler.close();
////            for (CLIResponse responses : response) {
////                System.out.println("MODE:" + responses.getMode());
////                System.out.println("CL:" + responses.getCommandLine());
////                System.out.println("Response:" + responses.getResponse());
////                System.out.println("Error:" + responses.isError());
////            }
//
////            response = handler.sendCommandLines(scripts);
//
//            for (CLIResponse responses : response) {
//                System.out.println("MODE:" + responses.getMode());
//                System.out.println("CL:" + responses.getCommandLine());
//                System.out.println("Response:" + responses.getResponse());
//                System.out.println("Error:" + responses.isError());
//            }
////            new BufferedReader(new InputStreamReader(System.in)).readLine();
//        //scripts.add(CLICommandLine.createScript("lo", true));
//        } catch (Exception ex) {
//            Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            System.exit(0);
//        }
    }
}