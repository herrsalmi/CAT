package com.csc;

/**
 * Project checkStatusConcordance
 * Created by ayyoub on 6/6/17.
 */
public class ReadGroupException extends RuntimeException {

    public ReadGroupException() {
        super("Read Group information not present !");
    }
}
