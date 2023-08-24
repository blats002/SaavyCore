/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saavy.net.cli.comms;


/**
 *
 * @author rgsaavedra
 */
public class CLIResponse {
    private CLI.MODE mode;
    private StringBuffer commandLine;
    private StringBuffer response;
    private boolean error;
    
    private CLIResponse(){
        
    }
    
    public static CLIResponse createResponse(CLI.MODE mode, String commandLine, String response){
        CLIResponse cliResponse = new CLIResponse();
        cliResponse.setMode(mode);
        cliResponse.setCommandLine(commandLine);
        cliResponse.setResponse(response);
        cliResponse.setError(false);
        return cliResponse;
    }
    
    public static CLIResponse createResponse(CLI.MODE mode, String commandLine, String response, boolean error){
        CLIResponse cliResponse = new CLIResponse();
        cliResponse.setMode(mode);
        cliResponse.setCommandLine(commandLine);
        cliResponse.setResponse(response);
        cliResponse.setError(error);
        return cliResponse;
    }

    public CLI.MODE getMode() {
        return mode;
    }

    private void setMode(CLI.MODE mode) {
        this.mode = mode;
    }

    public String getCommandLine() {
        return commandLine.toString();
    }

    private void setCommandLine(String commandLine) {
        this.commandLine = new StringBuffer(commandLine);
    }

    public String getResponse() {
        return response.toString();
    }

    private void setResponse(String response) {
        this.response = new StringBuffer(response);
    }

    public boolean isError() {
        return error;
    }

    private void setError(boolean error) {
        this.error = error;
    }
}
