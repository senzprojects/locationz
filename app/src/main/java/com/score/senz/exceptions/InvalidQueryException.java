package com.score.senz.exceptions;

/**
 * Exception that need to throw when query is invalid
 *
 * erangaeb@gmail.com
 */
public class InvalidQueryException extends Exception {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Invalid Query";
    }
}
