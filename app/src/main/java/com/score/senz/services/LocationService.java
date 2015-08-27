package com.score.senz.services;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Service to get current location
 * We are listening to location updates via LocationListener here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class LocationService extends Service {

    private LocationListener locationListener;
    private LocationManager locationManager;

    private static final String TAG = LocationService.class.getName();

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
}
