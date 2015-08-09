package com.score.senz.exceptions;

/**
 * Exception that need to throw when invalid phone no
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class InvalidPhoneNoException extends Exception {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Invalid phone no";
    }
}