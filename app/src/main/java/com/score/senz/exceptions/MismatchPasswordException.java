package com.score.senz.exceptions;

/**
 * Exception that need to throw when registration passwords are mismatching
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class MismatchPasswordException extends Exception {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Mis matching password";
    }
}
