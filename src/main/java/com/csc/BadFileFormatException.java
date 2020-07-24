package com.csc;

/**
 * project checkStatusConcordance
 * Created by ayyoub on 6/8/17.
 */
public class BadFileFormatException extends RuntimeException {

    public BadFileFormatException() {
        super("Bad file format !");
    }

    public BadFileFormatException(String extension) {
        super("Bad " + extension + " file format !");
    }
}
