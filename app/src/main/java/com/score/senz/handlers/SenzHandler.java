package com.score.senz.handlers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.score.senz.db.SenzorsDbSource;
import com.score.senz.listeners.LocationServiceListener;
import com.score.senz.pojos.Senz;
import com.score.senz.services.LocationService;
import com.score.senz.utils.SenzParser;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Handle All senz messages from here
 */
public class SenzHandler implements LocationServiceListener {
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
                    break;
                case GET:
                    handleGetSenz(context, senz);
                    break;
                case DATA:
                    handleDataSenz(context, senz);
                    break;
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
        Log.d("Tag", senz.getSender() + " : " + senz.getSenzType().toString());

        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.putExtra("PHONE", senz.getSender());
        
        context.startService(serviceIntent);
    }

    private void handleDataSenz(Context context, Senz senz) {
        Intent intent = new Intent("DATA");

        // we are broadcasting DATA sensors
        if (senz.getAttributes().containsKey("#msg")) {
            if (senz.getAttributes().get("#msg").equalsIgnoreCase("UserCreated")) {
                intent.putExtra("extra", true);
            } else {
                intent.putExtra("extra", false);
            }
        } else if (senz.getAttributes().containsKey("#lat")) {
            Log.d("TAG", "location response");
            intent.putExtra("extra", senz.getAttributes().get("#lat"));
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onPostReadLocation(Location location) {

    }
}
