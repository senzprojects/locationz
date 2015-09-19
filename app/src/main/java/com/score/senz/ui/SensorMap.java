package com.score.senz.ui;

import android.app.ActionBar;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
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
public class SensorMap extends FragmentActivity {

    private static final String TAG = SensorMap.class.getName();

    private LatLng locationCoordinates;

    private GoogleMap map;
    private Marker marker;
    private Circle circle;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.senz_map_layout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        setUpActionBar();
        initLocationCoordinates();
        setUpMapIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
    }

    /**
     * Set action bar title and font
     */
    private void setUpActionBar() {
        ActionBar actionBar = getActionBar();
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (this.findViewById(titleId));

        Typeface typefaceThin = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typefaceThin);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("#Location");
    }

    /**
     * Initialize LatLng object from here
     */
    private void initLocationCoordinates() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.locationCoordinates = bundle.getParcelable("extra");
        }
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
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            map.getUiSettings().setZoomControlsEnabled(true);
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
            marker = map.addMarker(new MarkerOptions().position(this.locationCoordinates).title("Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot)));
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
            Toast.makeText(this, "Invalid location", Toast.LENGTH_LONG).show();
            Log.d(TAG, "MoveToLocation: invalid lat lon parameters");
        }
    }

}
