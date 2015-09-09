package com.score.senz.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.score.senz.db.SenzorsDbSource;
import com.score.senz.pojos.Senz;
import com.score.senz.utils.SenzParser;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

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

    public void handleSenz(Context context, String senzMessage) {
        try {
            // parse and verify senz
            Senz senz = SenzParser.parse(senzMessage);
            verifySenz(context, senz);
            switch (senz.getSenzType()) {
                case SHARE:
                    handleShareSenz(context, senz);
                case GET:
                    handleGetSenz(context, senz);
                case DATA:
                    handleDataSenz(context, senz);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    private static void verifySenz(Context context, Senz senz) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // TODO get public key of sender
        senz.getSender();

        // TODO verify signature of the senz
        //RSAUtils.verifyDigitalSignature(senz.getPayload(), senz.getSignature(), null);
    }

    private void handleShareSenz(Context context, Senz senz) {
        // create senz
        new SenzorsDbSource(context).createSenz(senz);
    }

    private void handleGetSenz(Context context, Senz senz) {
        // TODO Start Location service here, rest of the operation handles by Location Service
    }

    private void handleDataSenz(Context context, Senz senz) {
        Intent intent = new Intent("DATA");

        // we are broadcasting DATA sensors
        if (senz.getAttributes().get("#msg").equalsIgnoreCase("UserCreated")) {
            intent.putExtra("extra", true);
        } else {
            intent.putExtra("extra", false);
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
