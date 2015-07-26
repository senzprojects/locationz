package com.score.senzors.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.utils.NetworkUtil;


/**
 * Service that send ping message to server periodically
 * We need to send ping message to server in order to keep
 * web socket connection alive
 */
public class PingService extends Service {

    private static final String TAG = PingService.class.getName();

    private SenzorApplication application;
    private Handler handler = new Handler();
    public static final int ONE_MINUTE = 60000 * 3;
    private Runnable pingSender = new Runnable() {
        public void run() {
            Log.d(TAG, "PeriodicTask");
            sendPing();
            handler.postDelayed(pingSender, ONE_MINUTE);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "OnCreate");
        application = (SenzorApplication)getApplication();
        handler.postDelayed(pingSender, ONE_MINUTE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");

        handler.removeCallbacks(pingSender);
    }

    /**
     * Send PING message to server via web socket
     */
    private void sendPing() {
        if(NetworkUtil.isAvailableNetwork(this)) {
            // construct query and send to server via web socket
            if(application.getWebSocketConnection().isConnected()) {
                Log.w(TAG, "SendPing: sending ping to server");
                // application.getWebSocketConnection().sendTextMessage(query);
                application.getWebSocketConnection().sendBinaryMessage(new byte[0x9]);
            } else {
                Log.w(TAG, "SendPing: not connected to web socket");
            }
        } else {
            Log.w(TAG, "SendPing: no network connection");
        }
    }
}
