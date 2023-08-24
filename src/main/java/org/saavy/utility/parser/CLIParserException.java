package org.saavy.utility.parser;

public class CLIParserException extends Exception {

    public CLIParserException(String msg) {
        super(msg);
    }

    public CLIParserException(Exception e) {
        super(e);
    }

    public CLIParserException(String msg, Exception e) {
        super(msg, e);
    }
}
