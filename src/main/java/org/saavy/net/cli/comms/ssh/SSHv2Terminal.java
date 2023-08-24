/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli.comms.ssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionMonitor;
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.Session;
import org.saavy.net.cli.comms.CLIClientException;
import org.saavy.net.cli.comms.DumbTerminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rgsaavedra
 */
public class SSHv2Terminal extends DumbTerminal {

    private Connection conn;
    private Session session;
    private int timeout;

    public SSHv2Terminal(InputStream in, OutputStream out, int timeout) {
        super(in, out);
        this.timeout = timeout;
        
    }

    
    private boolean connected = false;
    private KnownHosts knownHosts = new KnownHosts();

    @Override
    public void connect(String host, int port) throws IOException {
        if (!isConnected()) {
            conn = new Connection(host, port);

            conn.addConnectionMonitor(new ConnectionMonitor() {
                public void connectionLost(Throwable reason) {
//                    reason.printStackTrace();
                    close();
                }
            });
        }
        conn.connect(new SimpleVerifier(knownHosts));

        boolean isAuthenticated = false;
        int tryCount = 0;

        while(!isAuthenticated && tryCount < 2){
            isAuthenticated = conn.authenticateWithPassword(getUsername(), getPassword());
        }
//        System.out.println("isAuthenticated:"+isAuthenticated);
        if (!isAuthenticated) {
            throw new IOException("Authentication failed.");
        }
        session = conn.openSession();

        session.requestDumbPTY();
//        session.requestPTY("dumb", 80, 25, 0, 0, null);
        session.startShell();
        connected = true;
        initializeStreams();
        startHandlers();
        fireClientConnected();
    }

    public void close() {
        if (isConnected()) {
            connected = false;
            try {
                session.close();
                conn.close();
                this.inFrom.close();
                this.outTo.close();
                fireClientClosed();
            } catch (IOException ex) {
                Logger.getLogger(SSHv2Terminal.class.getName()).log(Level.SEVERE, null, ex);
                fireClientError(new CLIClientException(ex.getMessage(), ex));
            } finally {
                connected = false;
            }
        }
    }

    private void initializeStreams() {
        writer = new Thread("SSH Writer") {

            @Override
            public void run() {
                int b;
                try {
                    OutputStream out = session.getStdin();
                    while (isConnected() && (b = inFrom.read()) != -1) {
                        out.write(b);
                    }
                } catch (Exception ex) {
//                    ex.printStackTrace();
                } finally {
                    close();
                }
            }
        };

        reader = new Thread("SSH Reader") {

            @Override
            public void run() {

                try {
                    byte[] buffer = new byte[8192];
                    InputStream stdout = session.getStdout();
                    while (isConnected()) {
                        byte[] b = new byte[256];
                        int size = stdout.read(b);
                        if(size > 0){
                            outTo.write(b, 0, size);
                            outTo.flush();
                        }
                    }
                } catch (Exception ex) {
//                    ex.printStackTrace();
                    if (outTo != null) {
                        try {
                            outTo.write(-1);
                        } catch (IOException ex1) {
//                            Logger.getLogger(SSHv2.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    close();
                }
                //System.out.println("Reader Thread DEAD");
            }
        };
    }

    @Override
    public boolean isConnected() {
        return session != null && connected;
    }
}
