package com.score.senz.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
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
 * Service to get current location
 * We are listening to location updates via LocationListener here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class LocationService extends Service {

    private static final String TAG = LocationService.class.getName();

    private LocationListener locationListener;
    private LocationManager locationManager;

    String receiverPhone;

    // keeps weather service already bound or not
    boolean isServiceBound = false;

    // use to send senz messages to SenzService
    Messenger senzServiceMessenger;

    // connection for SenzService
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            senzServiceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            senzServiceMessenger = null;
        }
    };

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
    public void onCreate() {
        // start to listen location updates from here
        locationListener = new SenzLocationListener();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(getBestLocationProvider(), 0, 0, locationListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        this.receiverPhone = intent.getExtras().getString("PHONE");

        // bind with senz service
        if (!isServiceBound) {
            bindService(new Intent(LocationService.this, SenzService.class), senzServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }

        return START_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        locationManager.removeUpdates(locationListener);
    }

    /**
     * Get best available location provider via Criteria
     * @return location provider
     */
    private String getBestLocationProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        return locationManager.getBestProvider(criteria, true);
    }

    /**
     * Location listener to get accurate location
     * We only need one location update, so when receives one update we stop the service
     */
    private class SenzLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, String.valueOf(location.getLatitude()));
            Log.d(TAG, String.valueOf(location.getLongitude()));

            sendLocation(location);

            // unbind the service
            if (isServiceBound) {
                unbindService(senzServiceConnection);
                isServiceBound = false;
            }

            // stop the service when receive a location update
            stopSelf();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void sendLocation(Location location) {
        try {
            // create key pair
            PrivateKey privateKey = RSAUtils.getPrivateKey(this);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            senzAttributes.put("lat", Double.toString(location.getLatitude()));
            senzAttributes.put("lon", Double.toString(location.getLongitude()));

            User user = PreferenceUtils.getUser(this);

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.DATA);
            senz.setReceiver(receiverPhone);
            senz.setSender(user.getPhoneNo());
            senz.setAttributes(senzAttributes);

            // get digital signature of the senz
            String senzPayload = SenzParser.getSenzPayload(senz);
            String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
            String senzMessage = SenzParser.getSenzMessage(senzPayload, senzSignature);

            // send senz to server
            Message msg = new Message();
            msg.obj = senzMessage;
            try {
                senzServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }
}
