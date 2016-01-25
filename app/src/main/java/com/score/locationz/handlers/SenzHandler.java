package com.score.locationz.handlers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.score.locationz.db.SenzorsDbSource;
import com.score.locationz.services.LocationService;
import com.score.locationz.services.SenzServiceConnection;
import com.score.senz.ISenzService;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

/**
 * Handle All senz messages from here
 */
public class SenzHandler {
    private static final String TAG = SenzHandler.class.getName();

    private static Context context;

    private static SenzHandler instance;

    private static SenzServiceConnection serviceConnection;

    private static ISenzService senzService;

    private SenzHandler() {
    }

    public static SenzHandler getInstance(Context context) {
        if (instance == null) {
            instance = new SenzHandler();
            SenzHandler.context = context;

            serviceConnection = new SenzServiceConnection(context);

            // bind to senz service
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        return instance;
    }

    public void handleSenz(Senz senz) {
        switch (senz.getSenzType()) {
            case PING:
                Log.d(TAG, "PING received");
                break;
            case SHARE:
                Log.d(TAG, "SHARE received");
                handleShareSenz(senz);
                break;
            case GET:
                Log.d(TAG, "GET received");
                handleGetSenz(senz);
                break;
            case DATA:
                Log.d(TAG, "DATA received");
                handleDataSenz(senz);
                break;
        }
    }

    private void handleShareSenz(Senz senz) {
        Log.d("Tag", senz.getSender() + " : " + senz.getSenzType().toString());

        //Intent serviceIntent = new Intent(context, ShareHandlerService.class);
        //serviceIntent.putExtra("SENZ", senz);

        //context.startService(serviceIntent);

        //call back after service bind
        serviceConnection.executeAfterServiceConnected(new Runnable() {
            @Override
            public void run() {
                senzService = serviceConnection.getInterface();
            }
        });

    }

    private void handleGetSenz(Senz senz) {
        Log.d("Tag", senz.getSender() + " : " + senz.getSenzType().toString());

        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.putExtra("USER", senz.getSender());

        context.startService(serviceIntent);
    }

    private void handleDataSenz(Senz senz) {
        // sync data with db data
        SenzorsDbSource dbSource = new SenzorsDbSource(context);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);

        // we broadcast data senz
        Intent intent = new Intent("DATA");
        intent.putExtra("SENZ", senz);
        context.sendBroadcast(intent);

        // broadcast received senz
        Intent newSenzIntent = new Intent("com.score.senz.NEW_SENZ");
        newSenzIntent.putExtra("SENZ", senz);
        context.sendBroadcast(newSenzIntent);
    }
}
