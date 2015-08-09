package com.score.senz.exceptions;

/**
 * Created by eranga on 6/9/14.
 */
public class RsaKeyException extends Exception {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "Cannot encrypt message";
    }
}
