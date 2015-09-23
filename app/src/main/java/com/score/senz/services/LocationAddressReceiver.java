package com.score.senz.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by eranga on 9/23/15.
 */
public class LocationAddressReceiver extends AsyncTask<String, String, String> {
    Context context;
    LatLng latLng;
    String sender;

    public LocationAddressReceiver(Context context, LatLng latLng, String sender) {
        this.context = context;
        this.latLng = latLng;
        this.sender = sender;
    }

    @Override
    protected String doInBackground(String... params) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String city = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                if (addressList.size() > 0) {
                    city = addressList.get(0).getSubLocality();
                }
            }
        } catch (IOException e) {
            Log.d("Address", "Unable connect to Geocoder", e);
        }

        return city;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        // update sender's last know location in database
    }

}
