package com.score.senzors.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NotificationUtils;
import com.score.senzors.utils.PreferenceUtils;
import com.score.senzors.utils.QueryHandler;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Service for listen to a web socket
 * On login to application this service need tobe start
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class WebSocketService extends Service implements Handler.Callback {

    private static final String TAG = WebSocketService.class.getName();
    private SenzorApplication application;
    private boolean isRunning;

    public static final String WEB_SOCKET_CONNECTED = "WEB_SOCKET_CONNECTED";
    public static final String WEB_SOCKET_DISCONNECTED = "WEB_SOCKET_DISCONNECTED";

    // Keep track with how many times we tried to connect to web socket
    // maximum try 10 times
    private static int RECONNECT_COUNT = 0;
    private static int MAX_RECONNECT_COUNT = 14;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        application = (SenzorApplication) getApplication();
        isRunning = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectToWebSocket(application);
        isRunning = true;

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // here we
        //  1. cancel/update all notifications
        //  2. delete all sensors in my sensor list
        //  3. send broadcast message about service disconnecting
        stopForeground(true);
        isRunning = false;

        application.emptyMySensors();
        Intent disconnectMessage = new Intent(WebSocketService.WEB_SOCKET_DISCONNECTED);
        sendBroadcast(disconnectMessage);
        NotificationUtils.cancelNotification(this);

        if (application.getWebSocketConnection().isConnected())
            application.getWebSocketConnection().disconnect();
        Log.d(TAG, "OnDestroy: service destroyed");
    }

    /**
     * Connect to web socket
     * when connecting we need to send username and password of current user
     * in order to continue communication
     *
     * @param application application object
     */
    public void connectToWebSocket(final SenzorApplication application) {
        try {
            application.getWebSocketConnection().connect(SenzorApplication.WEB_SOCKET_URI, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    // connected to web socket so notify it to activity
                    Log.d(TAG, "ConnectToWebSocket: open web socket");
                    // only display notification when user login
                    WebSocketService.RECONNECT_COUNT = 0;
                    Notification notification = NotificationUtils.getNotification(WebSocketService.this, R.drawable.logo_green,
                            getString(R.string.app_name), getString(R.string.launch_senzors));
                    notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
                    startForeground(NotificationUtils.SERVICE_NOTIFICATION_ID, notification);
                }

                @Override
                public void onTextMessage(String payload) {
                    // delegate to handleMessage
                    Log.d(TAG, "ConnectToWebSocket: receive message from server");
                    QueryHandler.handleQuery(application, payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "ConnectToWebSocket: web socket closed");
                    Log.d(TAG, "ConnectToWebSocket: code - " + code);
                    Log.d(TAG, "ConnectToWebSocket: reason - " + reason);

                    // we only reconnect if service is running
                    if (isRunning) {
                        if (code < 4000) new WebSocketReConnector().execute();
                    } else {
                        Log.d(TAG, "ConnectToWebSocket: Service not running, so no reconnect");
                    }
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, "ConnectToWebSocket: error connecting to web socket", e);
        }
    }

    /**
     * Reconnect to web socket when connection drops
     * We maximum try 10 times, after that ignore connecting
     */
    private void reconnectToWebSocket() {
        application.setCallback(this);
        if(WebSocketService.RECONNECT_COUNT <= WebSocketService.MAX_RECONNECT_COUNT) {
            if(application.getWebSocketConnection().isConnected()) {
                Log.e(TAG, "ReconnectToWebSocket: web socket already connected");
            } else {
                Log.e(TAG, "ReconnectToWebSocket: trying to re-connect " + (WebSocketService.RECONNECT_COUNT+1) + " times");
                connectToWebSocket(application);
                WebSocketService.RECONNECT_COUNT++;
            }
        } else {
            stopService(new Intent(getApplicationContext(), WebSocketService.class));
            Log.d(TAG, "ReconnectToWebSocket: maximum re-connect count exceed");
        }
    }

    /**
     * Async task use to reconnect web socket
     * When web socket is disconnected we are trying to reconnect it via this task
     */
    private class WebSocketReConnector extends AsyncTask<Void, Void, Void> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "WebSocketReConnector: reconnecting via async task");
            try {
                // sleep for a while before reconnect
                // sleep for random time interval
                if(WebSocketService.RECONNECT_COUNT <= WebSocketService.MAX_RECONNECT_COUNT/2)
                    Thread.sleep(5000);
                else
                    Thread.sleep(10000);
                reconnectToWebSocket();
            } catch (InterruptedException e) {
                Log.e(TAG, "WebSocketReConnector: error sleeping thread", e);
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        ActivityUtils.cancelProgressDialog();

        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            if (payLoad.equalsIgnoreCase("SERVER_KEY_EXTRACTION_SUCCESS")) {
                Log.d(TAG, "HandleMessage: server key extracted");

                // server key extraction success
                // so send PUT query to create user
                try {
                    if(application.getWebSocketConnection().isConnected()) {
                        User loginUser = PreferenceUtils.getUser(this);
                        String loginQuery = QueryHandler.getLoginQuery(loginUser, PreferenceUtils.getSessionKey(this));
                        Log.d(TAG, "------login query------");
                        Log.d(TAG, loginQuery);
                        application.getWebSocketConnection().sendTextMessage(loginQuery);
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NoUserException e) {
                    Log.e(TAG, e.getMessage());
                }
            } else if(payLoad.equalsIgnoreCase("LoginSUCCESS")) {
                Log.d(TAG, "HandleMessage: login success");
                application.setCallback(null);
            } else {
                Log.d(TAG, "HandleMessage: login fail");
            }
        } else {
            Log.e(TAG, "HandleMessage: message is NOT a string(may be location object)");
        }

        return false;
    }

}
