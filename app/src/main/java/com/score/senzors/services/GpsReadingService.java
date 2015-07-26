package com.score.senzors.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.utils.QueryParser;

import java.util.HashMap;

public class GpsReadingService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
                                                          GooglePlayServicesClient.OnConnectionFailedListener,
                                                          com.google.android.gms.location.LocationListener {

    private static final String TAG = GpsReadingService.class.getName();
    private SenzorApplication application;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Define an object that holds accuracy and frequency parameters
    private LocationClient locationClient;
    private LocationRequest locationRequest;

    // two types of location request
    //      1. request from this phone
    //      2. request from friend
    boolean isMyLocation = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        application = (SenzorApplication) getApplication();
        locationClient = new LocationClient(getApplicationContext(), this,this);

        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isMyLocation = intent.getExtras().getBoolean("isMyLocation");
        if (isServicesConnected()) locationClient.connect();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "OnConnectionFailed: connection fail");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnected(Bundle arg0) {
        Log.d(TAG, "OnConnected: location client connected");
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisconnected() {
        Log.d(TAG, "OnDisconnected: location client disconnected");
    }

    /**
     * Verify that Google Play services is available before making a request.
     * @return true if Google Play services is available, otherwise false
     */
    private boolean isServicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(GpsReadingService.this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "IsServicesConnected: google play service available");
            return true;
        } else {
            Log.e(TAG, "IsServicesConnected: google play service not available");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy: service destroyed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "OnLocationChanged: location changed");
        Log.d(TAG, "OnLocationChanged: lat - " + location.getLatitude());
        Log.d(TAG, "OnLocationChanged: lon - " + location.getLongitude());

        // get location and send to appropriate handle
        // the close location updates
        // stop service
        if(isMyLocation) {
            // send location result to sensor list via message
            handleLocationRequestFromSensorList(location);
        } else {
            // send location to server via web socket
            handleLocationRequestFromSever(location);
        }

        // If the client is connected
        if (locationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            Log.d(TAG, "OnLocationChanged: removed location updates");
            locationClient.removeLocationUpdates(this);
        }

        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        locationClient.disconnect();
        stopSelf();
    }

    /**
     * Handle location request that comes from server as a query
     * need to send location to server via web socket
     * @param location current location
     */
    private void handleLocationRequestFromSever(Location location) {
        String command = "DATA";
        String user = application.getRequestQuery().getUser();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon", Double.toString(location.getLongitude()));
        String message = QueryParser.getMessage(new Query(command, user, params));

        // send data to server
        if(application.getWebSocketConnection().isConnected()) {
            Log.d(TAG, "HandleLocationRequestFromSever: web socket connected, so sending reply to server");
            application.getWebSocketConnection().sendTextMessage(message);
        } else {
            Log.e(TAG, "HandleLocationRequestFromSever: web socket not connected");
        }
    }

    /**
     * Location request comes from internal(from sensor list)by clicking my location sensor
     * So need to send update to sensor list
     * @param location current location
     */
    private void handleLocationRequestFromSensorList(Location location) {
        LatLon latLon = new LatLon(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));

        // send message to available handler
        Message message = Message.obtain();
        message.obj = latLon;
        if (application.getHandler()!=null) {
            Log.d(TAG, "HandleLocationRequestFromSensorList: send message to available handler");
            application.getHandler().sendMessage(message);
        } else {
            Log.e(TAG, "HandleLocationRequestFromSensorList: no available handler");
        }
    }
}
