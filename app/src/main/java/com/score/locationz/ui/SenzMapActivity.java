package com.score.locationz.ui;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.score.locationz.utils.LocationUtils;
import com.score.locationz.R;
import com.score.locationz.utils.ActivityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: eranga
 * Date: 2/11/14
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SenzMapActivity extends FragmentActivity implements LocationListener {

    private static final String TAG = SenzMapActivity.class.getName();

    RelativeLayout myLocation;
    RelativeLayout myRoute;

    private LocationManager locationManager;

    private LatLng friendLatLan;
    private LatLng myLatLan;

    private GoogleMap map;
    private Marker marker;
    private Circle circle;

    Polyline line;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.senz_map_layout);
        myLocation = (RelativeLayout) findViewById(R.id.map_location);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //displayMyLocation(new LatLng(7.842891, 80.809937));
                ActivityUtils.showProgressDialog(SenzMapActivity.this, "Please wait...");
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationUtils.getBestLocationProvider(locationManager), 0, 0, SenzMapActivity.this);
            }
        });

        myRoute = (RelativeLayout) findViewById(R.id.map_route);
        myRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(getApplicationContext(),"work",Toast.LENGTH_LONG).show();
                if (myLatLan ==null){
                    Toast.makeText(getApplicationContext(),"Please select your location first",Toast.LENGTH_LONG).show();

                }
                else {
                    new MapDerection().execute(makeURL(myLatLan.latitude, myLatLan.longitude, friendLatLan.latitude, friendLatLan.longitude));
                }
            }
        });

        setUpActionBar();
        initLocationCoordinates();
        setUpMapIfNeeded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationManager != null) locationManager.removeUpdates(this);
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
            this.friendLatLan = bundle.getParcelable("extra");
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
            marker = map.addMarker(new MarkerOptions().position(this.friendLatLan).title("Friend").icon(BitmapDescriptorFactory.fromResource(R.drawable.location_friend)));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(this.friendLatLan, 10));

            // ... get a map
            // Add a circle
//            circle = map.addCircle(new CircleOptions()
//                    .center(this.friendLatLan)
//                    .radius(14000)
//                    .strokeColor(0xFF0000FF)
//                    .strokeWidth(0.5f)
//                    .fillColor(0x110000FF));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid location", Toast.LENGTH_LONG).show();
            Log.d(TAG, "MoveToLocation: invalid lat lon parameters");
        }
    }

    /**
     * Add marker to my location and set up zoom level
     *
     * @param latLng
     */
    private void displayMyLocation(LatLng latLng) {
        marker = map.addMarker(new MarkerOptions().position(latLng).title("Me").icon(BitmapDescriptorFactory.fromResource(R.drawable.location_me)));

//        circle = map.addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(14000)
//                .strokeColor(0xFF0000FF)
//                .strokeWidth(0.5f)
//                .fillColor(0x110000FF));

        // set zoom level
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng);
        builder.include(this.friendLatLan);
        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        map.animateCamera(cu);
    }

    @Override
    public void onLocationChanged(Location location) {
        ActivityUtils.cancelProgressDialog();
        locationManager.removeUpdates(this);
        myLatLan =new LatLng(location.getLatitude(), location.getLongitude());
        displayMyLocation(myLatLan);

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

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyBmbqJcnUO5up5j_DPB330nV8esjlsk32s");
        return urlString.toString();
    }

    private class MapDerection extends AsyncTask<String,Void,String>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ActivityUtils.showProgressDialog(SenzMapActivity.this, "Please wait...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            LatLng previous = null;

            try {

                JSONObject jsnObj = new JSONObject(s);
                JSONArray Jroute = jsnObj.getJSONArray("routes");
                for (int i=0; i<1; i++){
                    JSONArray Jlegs = ((JSONObject) Jroute.get(i)).getJSONArray("legs");
                    for (int j=0; j<Jlegs.length(); j++){
                        JSONArray Jsteps = ((JSONObject) Jlegs.get(j)).getJSONArray("steps");
                        for (int n=0; n<Jsteps.length(); n++){
                            String polyline = (String) ((JSONObject)((JSONObject) Jsteps.get(n)).get("polyline")).get("points");
                            List<LatLng> data= PolyUtil.decode(polyline);
                            PolylineOptions poly = new PolylineOptions()
                                    .color(Color.rgb(0,92,130))
                                    .width(7)
                                    .visible(true)
                                    .zIndex(30);
                            poly.addAll(data);
                            map.addPolyline(poly);
                        }

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ActivityUtils.cancelProgressDialog();
        }

        @Override
        protected String doInBackground(String... url) {
            return GET(url[0]);
        }

        public String GET(String url){
            InputStream inputStream = null;
            String result = "";
            try {

                // create HttpClient

                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            return result;
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;


            return result;

        }
    }


}
