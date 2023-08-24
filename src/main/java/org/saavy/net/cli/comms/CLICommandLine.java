/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.net.cli.comms;

import org.saavy.net.cli.comms.CLI.MODE;


/**
 *
 * @author rgsaavedra
 */
public class CLICommandLine {

    private MODE mode;
    private String commandLine;
    private boolean haltOnError;
    private String regexBeforeSend;
    private String securityPass;

    public CLICommandLine(MODE mode, String regexBeforeSend, String commandLine, boolean haltOnError) {
        this(mode, regexBeforeSend, commandLine, haltOnError,null);
    }
    
    public CLICommandLine(MODE mode, String regexBeforeSend, String commandLine, boolean haltOnError,String securityPass) {

        this.mode = mode;
        this.commandLine = commandLine;
        this.haltOnError = haltOnError;
        this.regexBeforeSend = regexBeforeSend;
        this.securityPass = securityPass;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public boolean isHaltOnError() {
        return haltOnError;
    }

    public void setHaltOnError(boolean haltOnError) {
        this.haltOnError = haltOnError;
    }

    public String getRegexBeforeSend() {
        return regexBeforeSend;
    }

    public void setRegexBeforeSend(String regexBeforeSend) {
        this.regexBeforeSend = regexBeforeSend;
    }

    public static CLICommandLine createCLICommandLine(String commandLine, boolean haltOnError) {
        return createCLICommandLine(null, null, commandLine, haltOnError);
    }

    public static CLICommandLine createCLICommandLine(MODE mode, String commandLine, boolean haltOnError) {
        return createCLICommandLine(mode, null, commandLine, haltOnError);
    }

    public static CLICommandLine createCLICommandLine(String regexBeforeSend, String commandLine, boolean haltOnError) {
        return createCLICommandLine(null, regexBeforeSend, commandLine, haltOnError);
    }

    public static CLICommandLine createCLICommandLine(MODE mode, String regexBeforeSend, String commandLine, boolean haltOnError) {
        return new CLICommandLine(mode, regexBeforeSend, commandLine, haltOnError);
    }
    

    /**
     * @return the securityPass
     */
    public String getSecurityPass() {
        return securityPass;
    }

    /**
     * @param securityPass the securityPass to set
     */
    public void setSecurityPass(String securityPass) {
        this.securityPass = securityPass;
    }
}
