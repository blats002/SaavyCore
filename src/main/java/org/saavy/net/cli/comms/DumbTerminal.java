/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli.comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 *
 * @author rgsaavedra
 */
public abstract class DumbTerminal {

    protected InputStream inFrom;
    protected OutputStream outTo;
    private String username;
    private String password;

    public DumbTerminal(InputStream inFrom, OutputStream outTo) {
        this.inFrom = inFrom;
        this.outTo = outTo;
    }

    public abstract void connect(String host, int port) throws IOException;

    public abstract void close() throws IOException;

    public abstract boolean isConnected();
    private boolean streaming = false;

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean s) {
        this.streaming = s;
    }
    private Vector<DumbTerminalListener> listeners;

    public void addTelnetClientListener(DumbTerminalListener l) {
        if (listeners == null) {
            listeners = new Vector<DumbTerminalListener>();
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeTelnetClientListener(DumbTerminalListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    protected void fireClientConnected() {
        if (listeners != null) {
            new Thread() {

                @Override
                public void run() {
                    for (DumbTerminalListener l : listeners) {
                        l.clientConnected();
                    }
                }
            }.start();

        }
    }

    protected void fireClientClosed() {
        if (listeners != null) {
            for (DumbTerminalListener l : listeners) {
                l.clientClosed();
            }
        }
    }

    protected void fireClientError(CLIClientException e) {
        if (listeners != null) {
            for (DumbTerminalListener l : listeners) {
                l.clientError(e);
            }
        }
    }
    protected Thread reader,  writer;

    protected void startHandlers() {
        new Thread() {

            @Override
            public void run() {
                writer.start();
                reader.setDaemon(true);
                reader.start();
            }
        }.start();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
