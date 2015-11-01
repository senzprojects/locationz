package com.score.senz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.score.senzc.pojos.Senz;


/**
 * Broadcast receiver to receive senz messages which broadcast from SenzService
 *
 * @author eranga bandara(erangaeb@gmail.com)
 */
public class SenzReceiver extends BroadcastReceiver {

    private static final String TAG = SenzReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // extract broadcasting senz
        String action = intent.getAction();
        Senz senz = intent.getExtras().getParcelable("SENZ");

        // handler senz from here

        Log.d(TAG, "Senz received " + action);
    }
}
