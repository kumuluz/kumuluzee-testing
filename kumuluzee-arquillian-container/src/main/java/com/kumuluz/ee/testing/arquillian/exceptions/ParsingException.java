package com.kumuluz.ee.testing.arquillian.exceptions;

public class ParsingException extends Exception {

    public ParsingException(String msg) {
        super(msg);
    }

    public ParsingException(String msg, Throwable e) {
        super(msg, e);
    }
}
