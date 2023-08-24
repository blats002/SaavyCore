/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli;

import org.saavy.net.cli.comms.CLI;
import org.saavy.platform.net.LoginDetails;

import java.util.concurrent.TimeUnit;

/**
 * @author rgsaavedra
 */
public class CLIDetails extends LoginDetails {

    public CLIDetails() {
        super();
        setProperty("username", "");
        setProperty("password", "");
        setProperty("retries", 2);
        setProperty("timeout", 60);
        setProperty("clitype", CLI.TYPE.TELNET);
        setProperty("port", 23);
    }

    @Override
    public void set(String key, Object obj) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public void set(String arg0, String arg1, Object arg2) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public void set(String arg0, int arg1, Object arg2) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public Object get(String key) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public Object get(String arg0, String arg1) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    @Override
    public Object get(String arg0, int arg1) {
        throw new UnsupportedOperationException("Method is not accessible.");
    }

    public String getUsername() {
        return (String) getProperty("username");
    }

    public void setUsername(String username) {
        setProperty("username", username);
    }

    public String getPassword() {
        return (String) getProperty("password");
    }

    public void setPassword(String password) {
        setProperty("password", password);
    }

    public int getRetries() {
        return (Integer) getProperty("retries");
    }

    public void setRetries(int retries) {
        setProperty("retries", retries);
    }

    public int getTimeout() {
        return (Integer) getProperty("timeout");
    }

    public void setTimeout(int timeout) {
        setProperty("timeout", timeout);
    }

    public long getTimeoutInMillis() {
        return TimeUnit.SECONDS.toMillis(getTimeout());
    }

    public int getPort() {
        return (Integer) getProperty("port");
    }

    public void setPort(int port) {
        setProperty("port", port);
    }

    public String getHost() {
        return (String) getProperty("host");
    }

    public void setHost(String host) {
        setProperty("host", host);
    }

    public CLI.TYPE getCLIType() {
        return (CLI.TYPE) getProperty("clitype");
    }

    public void setCLIType(CLI.TYPE type, int port) {
        setProperty("clitype", type);
        setPort(port);
    }

    public void setCLITypeUsingDefaultPorts(CLI.TYPE type) {
        setProperty("clitype", type);
        if (type.equals(CLI.TYPE.TELNET)) {
            setPort(23);
        } else if (type.equals(CLI.TYPE.SSH)) {
            setPort(22);
        }
    }
}
