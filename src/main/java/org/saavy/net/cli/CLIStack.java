/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli;

import org.saavy.dom.SaavyElement;
import org.saavy.net.cli.comms.CLI;
import org.saavy.net.cli.comms.CLIClientException;
import org.saavy.net.cli.comms.CLICommandLine;
import org.saavy.net.cli.comms.CLIResponse;
import org.saavy.platform.Module;
import org.saavy.platform.net.*;
import org.saavy.utility.parser.RegexParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class CLIStack extends CommStack<CLIHandler> {

    private boolean connected = false;

    public CLIStack() {
        super(new CLIHandler());
    }

    @Override
    public void init(SaavyElement init) {
        if (init.getName().equalsIgnoreCase("scripts")) {
            for (SaavyElement script : init.getChildren("script")) {
                if (script.hasAttribute("path") && script.hasAttribute("id")) {
                    RegexParser.getInstance().addScriptPath(script.getAttribute("id"), script.getAttribute("path"));
                }
            }
        }
    }

    @Override
    public void start() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop() {
        connected = false;
        if (cliProcessor != null) {
            cliProcessor.stopProcessing();
        }


        if (session != null) {
            session.close();
        }
        session = null;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
    private ArrayBlockingQueue<CLIQueue> cliQueues = new ArrayBlockingQueue<CLIQueue>(100);

    private class CLIQueue {

        private Module module;
        private Packet packet;

        public CLIQueue(Module module, Packet packet) {
            this.module = module;
            this.packet = packet;
        }

        public void processQueue() {
            processCLIPacket(module, packet);
        }
    }

    @Override
    public void doAction(Module module, Packet packet) {
        try {
            cliQueues.put(new CLIQueue(module, packet));
            if (cliProcessor == null) {
                cliProcessor = new CLIProcessor();
                cliProcessor.start();
            } else if (cliProcessor.waiting) {
                synchronized (cliProcessor) {
                    cliProcessor.notifyAll();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CLIStack.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    CLIProcessor cliProcessor;

    public class CLIProcessor extends Thread {

        public CLIProcessor() {
            super("CLI Processor");

        }
        private boolean running = true;
        private boolean waiting = false;

        @Override
        public void run() {
            while (running) {
                try {
                    synchronized (this) {
                        if (cliQueues.isEmpty()) {
                            waiting = true;
                            while(cliQueues.isEmpty()){
                                wait(1000 * 30);
                            }
                        } else {
                            wait(500);
                        }
                    }
                    waiting = false;
                    CLIQueue current = cliQueues.poll();
                    if (current == null || current.module == null) {
                        continue;
                    }
                    ArrayList<CLIQueue> tempList = new ArrayList<CLIQueue>();
                    cliQueues.drainTo(tempList);
                    tempList.add(current);
                    for (CLIQueue q : tempList) {
                        q.processQueue(); //breakpoint
                    }
                } catch (InterruptedException ex) {
//                    Logger.getLogger(CLIStack.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (session != null) {
                session.close();
            }
//            System.out.println("CLI Processor Done");
        }

        private void stopProcessing() {
            running = false;
            cliQueues.clear();
            cliQueues.add(new CLIQueue(null, null));
            synchronized(this){
                notifyAll();
            }
        }
    }
    private CLISession session;

    private void processCLIPacket(Module module, Packet packet) {

        SaavyElement req = packet.getPacketElement();
//        System.out.println("CLI Start:" + packet.getFrom());
        SaavyElement response = new SaavyElement();
        response.setName(req.getName());

        req.setAttribute("clitype", getHandler().getLoginDetails().getCLIType());

        try {
//            requestStarted = true;
            //throw new UnsupportedOperationException("Not supported yet.");
//            System.out.println("Connect:" + packet.getFrom());

            if (module != null && module.getATLPManager().getCommStackManager().getPoller().isForcePollersOnly()) {
                if (/*packet.getPacketElement().hasAttribute("force") && */packet.getPacketElement().getAttribute("force", "false").equalsIgnoreCase("false")) {
                    throw new CommStackException("CLI Engine Currently down");
                }
            }

            if (session == null) {
                session = getHandler().createCLISession();

            }

            if (!session.isConnected()) {
//                System.out.println("Recreate Session");
                session.connect();
            }

//            System.out.println("Connected:" + packet.getFrom());
            if (!isLoggedIn()) {
                setLoggedIn(true);
            }

            connected = true;
            ArrayList<CLICommandLine> scripts = new ArrayList<CLICommandLine>();

            for (SaavyElement cliCommand : req.getChildren("cli")) {
                CLI.MODE mode = CLI.MODE.valueOf(cliCommand.getAttribute("mode", "current").toUpperCase());
                String commandLine = cliCommand.getAttribute("command");
                boolean haltOnError = cliCommand.getAttribute("haltonerror", "true").equalsIgnoreCase("true");
                String regexBeforeSend = cliCommand.getAttribute("regexbeforesend", null);
                CLICommandLine line = null;
                if (mode.equals(CLI.MODE.CURRENT) || regexBeforeSend != null) {
                    if (regexBeforeSend == null || regexBeforeSend.isEmpty()) {
                        line = CLICommandLine.createCLICommandLine(commandLine, haltOnError);
                    } else {
                        line = CLICommandLine.createCLICommandLine(regexBeforeSend, commandLine, haltOnError);
                    }
                } else {
                    line = CLICommandLine.createCLICommandLine(mode, commandLine, haltOnError);
                }
                String securityPass = req.getAttribute("secpass",null);
                if(securityPass != null){
                    line.setSecurityPass(securityPass);
                }
                scripts.add(line);
            }

//            System.out.println("Request:" + packet.getFrom());
            List<CLIResponse> responses = session.sendCommandLines(scripts);
//            System.out.println("Done Request:" + packet.getFrom());
            for (CLIResponse cli : responses) {
                SaavyElement cliElement = new SaavyElement();
                cliElement.setName("cli");
                cliElement.setAttribute("mode", cli.getMode());
                cliElement.setAttribute("command", cli.getCommandLine());
                cliElement.setAttribute("error", cli.isError());
                cliElement.setText(cli.getResponse());
                response.addChildren(cliElement);
            }
        } catch (CLIClientException ex) {
            if (session != null) {
                session.close();
            }
            session = null;
//            Logger.getLogger(CLIStack.class.getName()).log(Level.SEVERE, null, ex);
            connected = false;
            response.setAttribute("error", "true");
            if (ex.getMessage().equalsIgnoreCase("Login Incorrect")) {
                response.setAttribute("exception", new LoginFailedException(ex.getMessage(), ex));
            } else {
                response.setAttribute("exception", new ConnectionLostException(ex.getMessage(), ex));
            }

        } catch (Exception ex) {
            if (session != null) {
                session.close(); //breakpoint
            }
            session = null;
            response.setAttribute("error", "true");
            response.setAttribute("exception", new ConnectionFailedException(ex.getMessage(), ex));
//            Logger.getLogger(CLIStack.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (response != null) {
                if (response.getAttribute("error").equalsIgnoreCase("true")) {
                    response.setName(packet.getPacketElement().getName());
                    response.addChildren(packet.getPacketElement());
                }
                response.setAttribute("to", packet.getFrom());
                response.setAttribute("from", packet.getTo());
                if (module != null) {
                    module.processSendResponse(Packet.createPacket(response), packet);
                }
//                module.processSendResponse(Packet.createPacket(response));
            }
        }
    }
}
