package com.score.senz.exceptions;

/**
 * Exception that need to throw when form input fields are invalid
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class InvalidInputFieldsException extends Exception {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Invalid input fields";
    }
}

