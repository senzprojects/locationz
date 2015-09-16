package com.score.senz.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.score.senz.R;

/**
 * Created with IntelliJ IDEA.
 * User: eranga
 * Date: 2/11/14
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SensorMap extends Fragment {

    private static final String TAG = SensorMap.class.getName();

    private LatLng locationCoordinates;

    private GoogleMap map;
    private Marker marker;
    private Circle circle;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView: creating view");
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.main, container, false);

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "OnActivityCreated: activity created");
        initLocationCoordinates();
        setUpMapIfNeeded();
    }

    /**
     * Initialize LatLng object from here
     */
    private void initLocationCoordinates() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.locationCoordinates = bundle.getParcelable("extra");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "OnResume: setting up map, set handler callback MapActivity");
        setUpMapIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        super.onPause();

        // un-register handler from here
        Log.d(TAG, "OnPause: reset handler callback MapActivity");
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call  once when {@link #map} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment
            // disable zoom controller
            Log.d(TAG, "SetUpMapIfNeeded: map is empty, so set up it");
            map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                moveToLocation();
            }
        }
    }

    /**
     * Move map to given location
     */
    private void moveToLocation() {
        Log.d(TAG, "MoveToLocation: move map to given location");

        // remove existing markers
        if (marker != null) marker.remove();
        if (circle != null) circle.remove();

        // add location marker
        try {
            marker = map.addMarker(new MarkerOptions().position(this.locationCoordinates).title("My new location").icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot)));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(this.locationCoordinates, 10));

            // ... get a map
            // Add a circle
            circle = map.addCircle(new CircleOptions()
                    .center(this.locationCoordinates)
                    .radius(14000)
                    .strokeColor(0xFF0000FF)
                    .strokeWidth(0.5f)
                    .fillColor(0x110000FF));
        } catch (NumberFormatException e) {
            Toast.makeText(this.getActivity(), "Invalid location", Toast.LENGTH_LONG).show();
            Log.d(TAG, "MoveToLocation: invalid lat lon parameters");
        }
    }

}
