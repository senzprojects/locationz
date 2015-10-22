package com.score.senz.handlers;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.score.senz.R;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.services.LocationAddressReceiver;
import com.score.senz.services.LocationService;
import com.score.senz.services.SenzService;
import com.score.senz.utils.NotificationUtils;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.RSAUtils;
import com.score.senz.utils.SenzParser;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

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

    public void handleSenz(SenzService senzService, String senzMessage) {
        try {
            // parse and verify senz
            Senz senz = SenzParser.parse(senzMessage);
            verifySenz(senzService, senz);
            switch (senz.getSenzType()) {
                case PING:
                    Log.d(TAG, "PING received");
                    break;
                case SHARE:
                    Log.d(TAG, "SHARE received");
                    handleShareSenz(senzService, senz);
                    break;
                case GET:
                    Log.d(TAG, "GET received");
                    handleGetSenz(senzService, senz);
                    break;
                case DATA:
                    Log.d(TAG, "DATA received");
                    handleDataSenz(senzService, senz);
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

    private void handleShareSenz(SenzService senzService, Senz senz) {
        // create senz
        SenzorsDbSource dbSource = new SenzorsDbSource(senzService);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);

        // if senz already exists in the db, SQLiteConstraintException should throw
        try {
            dbSource.createSenz(senz);
            sendShareResponse(sender, senzService, true);

            NotificationUtils.showNotification(senzService, senzService.getString(R.string.new_senz), "SenZ received from @" + senz.getSender().getUsername());
        } catch (SQLiteConstraintException e) {
            sendShareResponse(sender, senzService, false);
            Log.e(TAG, e.toString());
        }
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
        if (senz.getAttributes().containsKey("msg")) {
            String msg = senz.getAttributes().get("msg");
            if (msg.equalsIgnoreCase("UserCreated") || msg.equalsIgnoreCase("ShareDone") || msg.equalsIgnoreCase("PutDone")) {
                intent.putExtra("extra", true);
            } else {
                intent.putExtra("extra", false);
            }
        } else if (senz.getAttributes().containsKey("lat")) {
            Log.d("TAG", "location response");

            // create lat LatLan object and broadcast it
            double lat = Double.parseDouble(senz.getAttributes().get("lat"));
            double lan = Double.parseDouble(senz.getAttributes().get("lon"));
            LatLng latLng = new LatLng(lat, lan);
            intent.putExtra("extra", latLng);

            // start background worker to get address and save in database
            new LocationAddressReceiver(context, latLng, senz.getSender()).execute("PARAM");
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendShareResponse(User receiver, SenzService senzService, boolean isDone) {
        try {
            // create key pair
            PrivateKey privateKey = RSAUtils.getPrivateKey(senzService);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) senzAttributes.put("msg", "ShareDone");
            else senzAttributes.put("msg", "ShareFail");

            User user = PreferenceUtils.getUser(senzService);

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.DATA);
            senz.setReceiver(receiver);
            senz.setSender(user);
            senz.setAttributes(senzAttributes);

            // get digital signature of the senz
            String senzPayload = SenzParser.getSenzPayload(senz);
            String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
            String senzMessage = SenzParser.getSenzMessage(senzPayload, senzSignature);

            senzService.sendSenz(senzMessage);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | NoUserException e) {
            e.printStackTrace();
        }
    }

}
