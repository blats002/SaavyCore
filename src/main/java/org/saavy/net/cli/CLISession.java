/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli;

import org.saavy.net.cli.comms.*;
import org.saavy.net.cli.comms.ssh.SSHv2Terminal;
import org.saavy.net.cli.comms.telnet.TelnetTerminal2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rgsaavedra
 */
public class CLISession implements DumbTerminalListener {

    private CLI.MODE mode = CLI.MODE.USER;
    private CLI.MODE modeTo = CLI.MODE.USER;
    private STATE state = STATE.IDLE;
    private CLIDetails loginDetails;
    private DumbTerminal terminal;
    private InputStream in;
    private OutputStream out;
    private CLI.TYPE cliMode = CLI.TYPE.TELNET;
    //private StringBuffer response;

    public CLISession(CLIDetails login) {
        this.cliMode = login.getCLIType();
        this.loginDetails = login;
        //response = new StringBuffer();
        in = new InputStream() {

            @Override
            public int read() throws IOException {
                try {
                    int i = sendQueue.take();
//                    System.out.print((char)i);
                    return i;
                } catch (InterruptedException ex) {
                    return -1;
                }
            }

            @Override
            public void close() throws IOException {
                if (sendQueue != null && in != null) {
                    sendQueue.add(-1);
                    try {
                        synchronized (in) {
                            in.notify();
                        }
                    } catch (Exception e) {
                    }
                    in = null;
                }
            }
        };
        out = new OutputStream() {

            StringBuffer buffer = new StringBuffer();

            public void refreshBuffer() {
                buffer = new StringBuffer();
            }

            @Override
            public void write(int b) throws IOException {
                if (b == -1) {
                    recvQueue.clear();
                    recvQueue.add("\\r");
                    flush();
                } else {
                    char c = (char) b;
                    if (String.valueOf(c).matches("[\\t|\\n|\\x20-\\x7F]")) {
                        buffer.append(c);
                        available++;
                    }
                }
            }

            @Override
            public void flush() throws IOException {
                try {
                    available = 0;
//                    if (recvQueue.size() == 1000) {
//                        System.out.println("Full");
//                    }
                    recvQueue.put(buffer.toString());
                    processPollLine();
                    refreshBuffer();
                } catch (InterruptedException ex) {
                    Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
    int available = 0;

    public void processPollLine() {
        if (reader != null && reader.waiting) {
            synchronized (reader) {
                reader.notify();
            }
        }
    }

    public void connect() throws IOException, CLIClientException, Exception {
        if (cliMode.equals(CLI.TYPE.TELNET)) {
            terminal = new TelnetTerminal2(in, out, this.loginDetails.getTimeout() * 1000);
        } else {
            terminal = new SSHv2Terminal(in, out, this.loginDetails.getTimeout() * 1000);
        }

        terminal.addTelnetClientListener(this);

        terminal.setUsername(loginDetails.getUsername());
        terminal.setPassword(loginDetails.getPassword());

        terminal.connect(loginDetails.getHost(), loginDetails.getPort());


        state = STATE.LOGIN;
        while (!state.equals(STATE.IDLE) && !state.equals(STATE.CLOSED) && !state.equals(STATE.ERROR)) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
//                    Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (!isLoggedIn()) {
            throw new CLIClientException("Login Incorrect");
        } else if (state.equals(STATE.CLOSED)) {
            throw new CLIClientException("Stream Closed");
        } else if (state.equals(STATE.ERROR)) {
            throw new CLIClientException("Error");
        }
//        sendCommandLine("terminal length 0");
//        System.out.println("LoggedIn");
    }
//    public void login(String user, String pass) {
//        
//        sendCommandLineToDevice(pass);
//    }
    private String securityPass ="";
    private void changeMode(CLI.MODE mode, String securityPass) throws CLIClientException {
        if (!this.mode.equals(mode)) {
            modeTo = mode;
            state = STATE.CHANGEMODE;
            if(securityPass != null){
                this.securityPass = securityPass;
            }else{
                this.securityPass = "";
            }
            
            sendInteractionChar('\n');
            while (!state.equals(STATE.IDLE) && !state.equals(STATE.CLOSED)) {
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (state.equals(STATE.CLOSED)) {
                throw new CLIClientException("Stream Closed");
            }
//            if (!modeTo.equals(this.mode)) {
////                try {
////                    throw new CLIClientException("Cant change mode to " + modeTo.toString());
////                } finally {
////                    modeTo = this.mode;
////                }
//                modeTo = this.mode;
//            }
        }
    }

    public synchronized List<CLIResponse> sendCommandLines(Collection<CLICommandLine> scripts) throws CLIClientException {
        ArrayList<CLIResponse> list = new ArrayList<CLIResponse>();
        String lastResponse = "";
        for (CLICommandLine script : scripts) {
            try {
                
                if (script.getRegexBeforeSend() == null || lastResponse.matches(script.getRegexBeforeSend())) {
                    CLIResponse resp = sendCommandLine(script.getMode(), script.getCommandLine(),script.getSecurityPass());
                    lastResponse = resp.getResponse().trim();
//					System.out.println("Resp:"+lastResponse);
                    list.add(resp);
                    if (resp.isError() && script.isHaltOnError()) {
                        break;
                    }
                }
            } catch (CLIClientException ex) {
                throw new CLIClientException("Error getting Data!", ex);
            }
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return list;
    }

    public synchronized CLIResponse sendCommandLine(String line) throws CLIClientException {
        return sendCommandLine(null, line,"");
    }

    private synchronized CLIResponse sendCommandLine(CLI.MODE mode, String line, String securityPass) throws CLIClientException {
        while (!state.equals(STATE.IDLE) && !state.equals(STATE.CLOSED) && !state.equals(STATE.ERROR)) {
//            System.out.println("(" + state + ")");
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (state.equals(STATE.CLOSED)) {
            throw new CLIClientException("Stream Closed");
        } else if (state.equals(STATE.ERROR)) {
            throw new CLIClientException("Error");
        }

        if (mode != null) {
            changeMode(mode,securityPass);
        }
        
        
        state = STATE.SENDING;
//        System.out.println(Calendar.getInstance().getTime() + " Sending Line:" + line);
        sendCommandLineToDevice(line);

        state = STATE.RECEIVING;

        while (!state.equals(STATE.READY) && !state.equals(STATE.ERROR) && !state.equals(STATE.CLOSED)) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (state.equals(STATE.CLOSED)) {
            throw new CLIClientException("Stream Closed");
        } else if (state.equals(STATE.ERROR)) {
            throw new CLIClientException("Error");
        }


//      System.out.println(Calendar.getInstance().getTime()+" Recv:" + line);

        StringBuffer resp = new StringBuffer(reader.getResponse());
//		System.out.println(Calendar.getInstance().getTime()+" Recv:" + resp.toString());
//		System.out.println("Matches:"+resp.toString().matches("(?s).*\\n[\\w|\\d].*[>|#]"));
        boolean error = false;
        if (resp.toString().matches("(?s).*\\n[\\w|\\d].*[>|#]")) {
            Pattern pattern = Pattern.compile("(%\\s\\S.*)\\n*[\\w|\\d].*[>|#]", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(resp);

            while (matcher.find()) {
                for (int x = 0; x < matcher.groupCount(); x++) {
                    String match = matcher.group(x);
//					System.out.println("Match:"+match);
                    if (match.startsWith("% Warning:")) {
                    } else if (match.startsWith("%")) {
                        error = true;
                        break;
                    }
                }
            }
        } else {
//            System.out.println(Calendar.getInstance().getTime() + " Recv:" + resp.toString());
        }
//        System.out.println(Calendar.getInstance().getTime() + " Done Line:" + line);
        //retBuff.append("<").append(line).append(">\n").append("<--START-->").append(resp.substring(line.length())).append("\n<--END-->");
        if(resp.toString().startsWith(line)){
            return CLIResponse.createResponse(this.mode, line, resp.substring(line.length() + 1), error);
        }else{
            return CLIResponse.createResponse(this.mode, line, resp.toString(), error);
        }
    }
    private String currentLine;

    private synchronized void sendCommandLineToDevice(String line) {
        currentLine = line;
        for (byte c : line.getBytes()) {
            send(c);
        }
        sendEnter();
    }

    public synchronized void sendEnter() {
        send('\n');
    }

    public synchronized void sendSpace() {
        sendInteractionChar(' ');
    }

    public synchronized void sendInteractionChar(char c) {
        sendQueue.add((int) c);
    }

    private void forceCommandLine(String line) {
        for (byte c : line.getBytes()) {
            sendQueue.add((int) c);
        }
        sendQueue.add((int) '\n');
    }
    private ArrayBlockingQueue<Integer> sendQueue = new ArrayBlockingQueue<Integer>(500);
    private ArrayBlockingQueue<String> recvQueue = new ArrayBlockingQueue<String>(1000);

    private synchronized void send(int b) {


        sendQueue.add(b);
        if (sendQueue.size() > 0) {
            state = STATE.SENDING;
        }
//        if (!state.equals(STATE.RECEIVING)) {
//            if (sendQueueTemp.size() > 0) {
//                sendQueueTemp.add(b);
//                while (sendQueueTemp.size() != 0) {
//                    try {
//                        sendQueue.add(sendQueueTemp.take());
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            } else {
//                
//            }
//            
//        } else {
//            sendQueueTemp.add(b);
//        }
    }

    public void close() {
        clientClosed();
    }

    public boolean isConnected() {
        return terminal != null && terminal.isConnected() && !state.equals(STATE.CLOSED) && !state.equals(STATE.ERROR);
    }

    public enum STATE {

        IDLE,
        SENDING,
        RECEIVING,
        LOGIN,
        CHANGEMODE,
        READY,
        ERROR,
        CLOSED,
        WAITING;
    }

    public void clientClosed() {
        state = STATE.CLOSED;
//        System.out.println("Closing AWPlus CLI Session");
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        in = null;
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        out = null;
        if (recvQueue != null) {
            recvQueue.add("");
            processPollLine();
        }
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException ex) {
                Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        terminal = null;
    }
    private boolean loggedIn = false;

    public boolean isLoggedIn() {
        return loggedIn;
    }
    private ThreadReader reader;

    public void clientConnected() {
        //login();
        reader = new ThreadReader();
//        System.out.println("Started New Thread Reader");
        reader.start();
    }

    public void clientError(CLIClientException e) {
        state = STATE.ERROR;
        Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, e);
    }

    public class ThreadReader extends Thread {

        StringBuffer responseBuff;

        public ThreadReader() {
            super("ThreadReader");
        }

        public void clearResponseBuffer() {
            responseBuff = new StringBuffer();
        }

        public String getResponse() {
            String line = responseBuff.toString()/*.replaceAll("\\n.*\\-\\-More\\-\\-.*?\\n", "\n\n")*/;
            clearResponseBuffer();
            state = STATE.IDLE;
            return line;
        }
        private boolean waiting = false;
        
        private boolean startChangeMode = false;
        
        @Override
        public void run() {
//            System.out.println("Thread Reader Started");
            clearResponseBuffer();
//			String currentBody;
            while (isConnected()) {
                try {
                    synchronized (this) {
                        if (recvQueue.isEmpty()) {
                            waiting = true;
                            wait(1000 * 60 * 60);
                        }
                    }
                    waiting = false;
                    String line = recvQueue.poll();

                    if (line == null || line.isEmpty()) {
                        continue;
                    }

                    if (line.equals("\\r")) {
                        break;
                    }

                    responseBuff.append(line);
//                    System.out.print(line);

                    if (state.equals(state.LOGIN)) {
                        if (cliMode.equals(CLI.TYPE.TELNET)) {
//                            System.out.println("Login:"+responseBuff.toString());
//                            System.out.println("Login:"+responseBuff.toString().matches("(?s)^.*login\\:\\s*$"));
                            if (responseBuff.toString().matches("(?s)^.*login\\:\\s*$")) {

                                forceCommandLine(loginDetails.getUsername());
                                clearResponseBuffer();
//                            } else if (responseBuff.toString().matches(Pattern.quote(loginDetails.getUsername()) + "\\n")) {
//                                clearResponseBuffer();
                            } else if (responseBuff.toString().matches("(?s)^.*Password:\\s*$")) {

                                forceCommandLine(loginDetails.getPassword());
                                clearResponseBuffer();
                                state = STATE.RECEIVING;
                            }
                        } else if (cliMode.equals(CLI.TYPE.SSH)) {
                            //System.out.println("(" + state + ")->:" + responseBuff.toString().trim());
                            clearResponseBuffer();
                            state = STATE.RECEIVING;
                        }
                    }else if (state.equals(state.RECEIVING) && responseBuff.toString().trim().matches("(.*\\n)*.*Password\\:")){
                        if (cliMode.equals(CLI.TYPE.SSH)) {
                            forceCommandLine(securityPass + "\r");
                        } else {
                            forceCommandLine(securityPass);
                        }
//                        clearResponseBuffer();
                    } else if (state.equals(state.SENDING) || state.equals(state.IDLE)) {
                        //TODO Notification
                        if (responseBuff.toString().trim().matches("(.*\\n)*.*[>|#|\\$]")) {
//							System.out.println("(" + state + ")->:" + responseBuff.toString().trim());
                            clearResponseBuffer();
                        }
                    } else if (state.equals(state.CHANGEMODE)) {
                        //TODO change mode logic
                        if (responseBuff.toString().trim().matches("(.*\\n)*.*Password\\:")) {
                            if(cliMode.equals(CLI.TYPE.SSH)){
                                forceCommandLine(securityPass+"\r");
                            }else{
                                forceCommandLine(securityPass);
                            }
                            
                            clearResponseBuffer();
                        }else if (responseBuff.toString().trim().matches("(.*\\n)*.*[>|#|\\$]")) {
                            //System.out.println("(" + state + ")->:" + responseBuff.toString().trim());
                            if (responseBuff.toString().trim().matches("(.*\\n)*.*>")) {
                                mode = CLI.MODE.USER;
                            } else if (responseBuff.toString().trim().matches("(.*\\n)*.*\\(config.*\\)#")) {
                                mode = CLI.MODE.CONFIGURE;
                            } else if (responseBuff.toString().trim().matches("(.*\\n)*.*#")) {
                                mode = CLI.MODE.PRIVILEGE;
                            }
                            String resp = responseBuff.toString();
                            if (resp.matches("(.*\\n)*(\\%.*)\\n(.*\\n)*.*[>|#|\\$]")) {
                                startChangeMode = false;
                                state = STATE.IDLE;
                            } else {
                                if(startChangeMode && modeTo != CLI.MODE.USER && mode == CLI.MODE.USER){
                                    state = STATE.IDLE;
                                    startChangeMode = false;
                                }else if (mode.compareTo(modeTo) < 0) {
                                    if (mode.equals(CLI.MODE.USER)) {
                                        forceCommandLine("enable");
                                    } else if (mode.equals(CLI.MODE.PRIVILEGE)) {
                                        forceCommandLine("configure terminal");
                                    }
                                } else if (mode.compareTo(modeTo) > 0) {
                                    if (mode.equals(CLI.MODE.CONFIGURE)) {
                                        forceCommandLine("end");
                                    } else if (mode.equals(CLI.MODE.PRIVILEGE)) {
                                        forceCommandLine("disable");
                                    }
                                } else {
                                    state = STATE.IDLE;
                                    startChangeMode = false;
                                }
                                
                                if(state.equals(state.CHANGEMODE) && !startChangeMode){
                                    startChangeMode = true;
                                }
                            }
                            clearResponseBuffer();
                        }

                    }  else {
                        //TODO receive logic
//						Matcher match = Pattern.compile("^.*[>|#|\\:|\\?]",Pattern.DOTALL)
//                        if (currentLine == null || currentLine.isEmpty()) {
//                            System.out.println("Match No Command:" + responseBuff.toString().trim().matches("(?s).*[\\w|\\d].*[>|#|\\:|\\?]"));
////                            System.out.println("Body:" + responseBuff.toString());
//                        }

                        if (!loggedIn && cliMode.equals(CLI.TYPE.TELNET) && responseBuff.toString().trim().matches("(?s).*login\\:")) {
                            clearResponseBuffer();
                            state = STATE.IDLE;
                        } else if (!loggedIn && cliMode.equals(CLI.TYPE.SSH) && responseBuff.toString().trim().matches("(?s).*Login\\s\\&\\spassword\\snot\\saccepted")) {
                            clearResponseBuffer();
                            state = STATE.IDLE;
                        } else if (available == 0 && recvQueue.size() == 0) {
//
                            if (!responseBuff.toString().trim().matches("(?s).*[>|#|\\:|\\?|\\$]")) {
                                continue;
                            } else {
                                Thread.sleep(100);
                                if (available > 0 || recvQueue.size() > 0) {
                                    continue;
                                }
                            }
//                            }
//                            
//                            System.out.println("resposeBuff:"+responseBuff.toString());
                            if (!loggedIn) {
                                loggedIn = true;
                                clearResponseBuffer();
                                state = STATE.IDLE;
                            } else {
                                state = STATE.READY;
                                while (state.equals(STATE.READY)) {
                                    //System.out.println("STATE:"+state);
                                    synchronized (this) {
                                        try {
                                            wait(100);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }
//                            System.out.println("(" + state + ")->:" + responseBuff.toString().trim());

                        }
//						else if (responseBuff.toString().trim().matches("(.*\\n)*.*\\-\\-More\\-\\-\\S\\S"))
//						{
//							sendSpace();
//						}

//                        System.out.println(responseBuff.toString());
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(CLISession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            reader = null;
//            System.out.println("Thread Reader Ended");
        }
    }

//    public static void main(String a[]) {
//        CLIDetails details = new CLIDetails();
//        details.setHost("192.168.100.2");
//        details.setCLITypeUsingDefaultPorts(TYPE.TELNET);
//
//        details.setUsername("fifteen");
//        details.setPassword("15151515");
//        CLISession handler = new CLISession(details);
//        try {
//
//            handler.connect();
////            handler.terminal.addTelnetClientListener(new DumbTerminalListener() {
////
////                public void clientClosed() {
////                    System.out.println("Client Closed");
////                }
////
////                public void clientConnected() {
////                    System.out.println("clientConnected");
////                }
////
////                public void clientError(CLIClientException e) {
////                    e.printStackTrace();
////                }
////            });
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
//    }
}
