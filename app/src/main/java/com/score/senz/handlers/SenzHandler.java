package com.score.senz.handlers;

import com.score.senz.pojos.Senz;
import com.score.senz.utils.SenzParser;

/**
 * Handle All senz messages from here
 */
public class SenzHandler {
    private static SenzHandler instance;

    private SenzHandler() {
    }

    public static SenzHandler getInstance() {
        if (instance == null) {
            instance = new SenzHandler();
        }
        return instance;
    }

    public void handleSenz(String senzMessage) {
        Senz senz = SenzParser.parseSenz(senzMessage);

        switch (senz.getSenzType()) {
            case SHARE:
                handleShareSenz(senz);
            case GET:
                handleGetSenz(senz);
            case DATA:
                handleDataSenz(senz);
        }
    }

    private void handleShareSenz(Senz senz) {
        //
    }

    private void handleGetSenz(Senz senz) {
        //
    }

    private void handleDataSenz(Senz senz) {
        //
    }
}
