package org.saavy.utility.parser;

public class InvalidArgumentException extends Exception {

    public InvalidArgumentException(String msg) {
        super(msg);
    }

    public InvalidArgumentException(Exception e) {
        super(e);
    }

    public InvalidArgumentException(String msg, Exception e) {
        super(msg, e);
    }
}
