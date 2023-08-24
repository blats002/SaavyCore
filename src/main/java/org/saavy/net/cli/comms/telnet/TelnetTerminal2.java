/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli.comms.telnet;

import org.apache.commons.net.io.Util;
import org.apache.commons.net.telnet.TelnetClient;
import org.saavy.net.cli.comms.CLIClientException;
import org.saavy.net.cli.comms.DumbTerminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

/**
 *
 * @author rgsaavedra
 */
public class TelnetTerminal2 extends DumbTerminal {

//    TelnetWrapper wrapper;
    TelnetClient client;
    private boolean connected = false;

    public TelnetTerminal2(InputStream inFrom, OutputStream outTo, final int timeout) {
        super(inFrom, outTo);
        initializeStreams();
        client = new TelnetClient();
//        wrapper = new TelnetWrapper() {
//
//            @Override
//            public void connect(String host, int port) throws IOException {
//                super.connect(host, port);
//                socket.setSoTimeout(timeout);
//            }
//        };
    }

    private void initializeStreams() {
        writer = new Thread("Telnet Writer") {

            @Override
            public void run() {
                int b;
                try {
                    while (isConnected() && (b = inFrom.read()) != -1) {
                        send(b);
                    }
                } catch (Exception ex) {
                } finally {
                    close();
                }
                //System.out.println("Writer Thread DEAD");
            }
        };

//        writer = new Thread("Telnet Writer")
//                 {
//                     public void run()
//                     {
//                         try
//                         {
//                             Util.copyStream(inFrom, client.getOutputStream());
//                         }
//                         catch (IOException e)
//                         {
//                             e.printStackTrace();
//                             System.exit(1);
//                         }
//                     }
//                 };

        reader = new Thread("Telnet Reader") {

            @Override
            public void run() {
                try {
                    Util.copyStream(client.getInputStream(), outTo);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    close();
                }
//                try {
//                    while (isConnected()) {
//                        byte[] b = new byte[80];
////                         wrapper.read(b);
//                        int size =client.getInputStream().read(b);
//                        System.out.print(new String(b));
//
//                        outTo.write(b, 0, size);
//                        outTo.flush();
//                        Util
//                    }
//                } catch (Exception ex) {
//                    try {
//                        if (outTo != null) {
//                            try {
//                                outTo.flush();
//                                outTo.write(-1);
//                            } catch (IOException ex1) {
//                                Logger.getLogger(TelnetTerminal.class.getName()).log(Level.SEVERE, null, ex1);
//                            }
//                        }
//                    } finally {
//                        close();
//                    }
//                }
            }
        };

    }

    public void setTimeout(int duration) throws SocketException {
        //socket.setSoTimeout(duration);
    }

    public void connect(String host, int port) throws IOException {
        try {
            connected = true;
//            wrapper.connect(host, port);
            client.connect(host, port);
            startHandlers();
            fireClientConnected();
        } catch (IOException iOException) {
            close();
            throw iOException;
        }
    }

    private void send(int b) throws IOException {
        byte[] bb = new byte[1];
        bb[0] = (byte) b;

        client.getOutputStream().write(bb);
        client.getOutputStream().flush();
    }

    public void close() {
        if (isConnected()) {
            try {
//                wrapper.disconnect();
                if(client.isConnected()){
                    client.disconnect();
                }
            } catch (Exception ex) {
//                Logger.getLogger(TelnetTerminal2.class.getName()).log(Level.SEVERE, null, ex);
                fireClientError(new CLIClientException(ex.getMessage(), ex));
            } finally {
                connected = false;
                fireClientClosed();
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

//    public static void main(String a[]) {
//        try {
//            TelnetTerminal2 client = new TelnetTerminal2(System.in, (OutputStream) System.out, 60000);
//            client.connect("192.168.100.22", 23);
//            client.addTelnetClientListener(new DumbTerminalListener() {
//
//                public void clientClosed() {
//                    System.exit(1);
//                }
//
//                public void clientConnected() {
//                }
//
//                public void clientError(CLIClientException e) {
//                }
//            });
//        } catch (Exception ex) {
//            Logger.getLogger(TelnetTerminal2.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
