package com.score.senz.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.score.senz.R;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.services.LocationAddressReceiver;
import com.score.senz.services.LocationService;
import com.score.senz.utils.NotificationUtils;
import com.score.senz.utils.PhoneBookUtils;
import com.score.senz.utils.SenzParser;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Handle All senz messages from here
 */
public class SenzHandler {
    private static final String TAG = SenzHandler.class.getName();
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
                case PING:
                    Log.d(TAG, "PING received");
                    break;
                case SHARE:
                    Log.d(TAG, "SHARE received");
                    handleShareSenz(context, senz);
                    break;
                case GET:
                    Log.d(TAG, "GET received");
                    handleGetSenz(context, senz);
                    break;
                case DATA:
                    Log.d(TAG, "DATA received");
                    handleDataSenz(context, senz);
                    break;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    private static void verifySenz(Context context, Senz senz) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        senz.getSender();

        // TODO get public key of sender
        // TODO verify signature of the senz
        //RSAUtils.verifyDigitalSignature(senz.getPayload(), senz.getSignature(), null);
    }

    private void handleShareSenz(Context context, Senz senz) {
        // create senz
        SenzorsDbSource dbSource = new SenzorsDbSource(context);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);
        dbSource.createSenz(senz);

        // display notification
        NotificationUtils.showNotification(context, context.getString(R.string.new_senz), "SenZ received from @" + PhoneBookUtils.getContactName(context, senz.getSender().getUsername()));
    }

    private void handleGetSenz(Context context, Senz senz) {
        // TODO Start Location service here, rest of the operation handles by Location Service
        Log.d("Tag", senz.getSender() + " : " + senz.getSenzType().toString());

        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.putExtra("USER", senz.getSender());

        context.startService(serviceIntent);
    }

    private void handleDataSenz(Context context, Senz senz) {
        // sync data with db data
        SenzorsDbSource dbSource = new SenzorsDbSource(context);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);

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

            // create lat LatLan object and broadcast it
            double lat = Double.parseDouble(senz.getAttributes().get("#lat"));
            double lan = Double.parseDouble(senz.getAttributes().get("#lon"));
            LatLng latLng = new LatLng(lat, lan);
            intent.putExtra("extra", latLng);

            // start background worker to get address and save in database
            new LocationAddressReceiver(context, latLng, senz.getSender()).execute("PARAM");
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
