package com.score.senzors.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.services.GpsReadingService;
import com.score.senzors.utils.ActivityUtils;

/**
 * Created with IntelliJ IDEA.
 * User: eranga
 * Date: 2/11/14
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SensorMapFragment extends Fragment implements View.OnClickListener, Handler.Callback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = SensorMapFragment.class.getName();
    private SenzorApplication application;

    private GoogleMap map;
    private Marker marker;
    private Circle circle;

    private RelativeLayout mapLocation;
    private RelativeLayout mapActivity;

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
        application = (SenzorApplication) this.getActivity().getApplication();
        initUi();
        setUpMapIfNeeded();
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Log.d(TAG, "InitUI: initializing UI components");

        mapLocation = (RelativeLayout) this.getActivity().findViewById(R.id.map_location);
        mapActivity = (RelativeLayout) this.getActivity().findViewById(R.id.map_activity);
        mapLocation.setOnClickListener(SensorMapFragment.this);
        mapActivity.setOnClickListener(SensorMapFragment.this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "OnResume: setting up map, set handler callback MapActivity");
        application.setCallback(this);
        setUpMapIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        super.onPause();

        // un-register handler from here
        Log.d(TAG, "OnPause: reset handler callback MapActivity");
        application.setCallback(null);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #map} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
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
            //map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            //map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker to available location
     * <p>
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    private void setUpMap() {
        Log.d(TAG, "SetUpMap: set up map on first time");

        // remove existing markers
        if(marker != null) marker.remove();
        if(circle != null) circle.remove();

        // add location marker
        try {
            LatLon latLon = application.getLatLon();
            if(latLon!=null) {
                LatLng currentCoordinates = new LatLng(Double.parseDouble(latLon.getLat()), Double.parseDouble(latLon.getLon()));
                marker = map.addMarker(new MarkerOptions().position(currentCoordinates).title("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot)));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 10));

                // ... get a map.
                // Add a circle in Sydney
                circle = map.addCircle(new CircleOptions()
                        .center(currentCoordinates)
                        .radius(14000)
                        .strokeColor(0xFF0000FF)
                        .strokeWidth(0.5f)
                        .fillColor(0x110000FF));

                showMyLocation(currentCoordinates);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this.getActivity(), "Invalid location", Toast.LENGTH_LONG).show();
            Log.d(TAG, "setUpMap: invalid lat lon parameters");
        }
    }

    /**
     * Show the my current location in the map current sensor is
     * friends sensor, need to set up zoom level according to the
     * distance between my location and friend location
     */
    private void showMyLocation(LatLng currentCoordinates) {
        if(!application.getCurrentSensor().isMySensor()) {
            // friends sensor
            // display my location and set zoom level
            /*Location myLocation = map.getMyLocation();
            Location friendLocation = new Location("Friend");
            friendLocation.setLatitude(currentCoordinates.latitude);
            friendLocation.setLongitude(currentCoordinates.longitude);
            float distance = myLocation.distanceTo(friendLocation);
            Log.d(TAG, "Distance: " + distance);*/
            Log.d(TAG, "Distance: " + "---------------");
        } else {
            // set default zoom level
            // map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 15));
        }
    }

    /**
     * Move map to given location
     * @param latLon lat/lon object
     */
    private void moveToLocation(LatLon latLon) {
        Log.d(TAG, "MoveToLocation: move map to given location");

        // remove existing markers
        if(marker != null) marker.remove();
        if(circle != null) circle.remove();

        // add location marker
        try {
            LatLng currentCoordinates = new LatLng(Double.parseDouble(latLon.getLat()), Double.parseDouble(latLon.getLon()));
            marker = map.addMarker(new MarkerOptions().position(currentCoordinates).title("My new location").icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot)));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentCoordinates, 10));

            // ... get a map
            // Add a circle
            circle = map.addCircle(new CircleOptions()
                    .center(currentCoordinates)
                    .radius(14000)
                    .strokeColor(0xFF0000FF)
                    .strokeWidth(0.5f)
                    .fillColor(0x110000FF));
        } catch (NumberFormatException e) {
            Toast.makeText(this.getActivity(), "Invalid location", Toast.LENGTH_LONG).show();
            Log.d(TAG, "MoveToLocation: invalid lat lon parameters");
        }
    }

    @Override
    public void onClick(View v) {
        if(v==mapLocation) {
            // get location or send request to server for get friends location
            // currently display my location
            // start location service to get my location
            // TODO if this sensor is from friend get friends location , we currently displaying our location
            Log.d(TAG, "OnClick: click on location, get current location");
            ActivityUtils.showProgressDialog(this.getActivity(), "Accessing location...");

            Intent serviceIntent = new Intent(this.getActivity(), GpsReadingService.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isMyLocation", true);
            serviceIntent.putExtras(bundle);
            this.getActivity().startService(serviceIntent);
        } else if(v==mapActivity) {
            Log.d(TAG, "OnClick: click on activity, get user activity");
            // TODO get user activity
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(marker)) {
            Log.d(TAG, "OnMarkerClick: click on location marker");
            // TODO display user/activity details
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        Log.d(TAG, "HandleMessage: message from server");
        if(message.obj instanceof LatLon) {
            // we handle LatLon messages only, from here
            // get address from location
            Log.d(TAG, "HandleMessage: message is a LatLon object so display it on map");
            LatLon latLon = (LatLon) message.obj;

            // display location
            ActivityUtils.cancelProgressDialog();
            application.setLatLon(latLon);
            moveToLocation(latLon);
        } else {
            Log.e(TAG, "HandleMessage: message not a LatLon object");
        }

        return false;
    }
}
