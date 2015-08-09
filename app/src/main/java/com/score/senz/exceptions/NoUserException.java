package com.score.senz.exceptions;

/**
 * Exception that raise when no logged in user
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class NoUserException extends Exception {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "No logged in user";
    }
}
