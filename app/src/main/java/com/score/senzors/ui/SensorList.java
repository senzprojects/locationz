package com.score.senzors.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.R;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.services.GpsReadingService;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;

import java.util.ArrayList;

/**
 * Display sensor list/ Fragment
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorList extends Fragment implements Handler.Callback {

    private static final String TAG = SensorList.class.getName();
    private SenzorApplication application;

    // list view components
    private ListView sensorListView;
    private ArrayList<Sensor> sensorList;
    private SensorListAdapter adapter;

    // empty view to display when no sensors available
    private ViewStub emptyView;

    // use custom font here
    private Typeface typeface;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.sensor_list_layout, container, false);

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        application = (SenzorApplication) getActivity().getApplication();
        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");

        initEmptyView();
        initSensorListView();
        initSensorList();
        displaySensorList();
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();

        // TODO refresh sensor list if need
        initSensorList();
        displaySensorList();
        application.setCallback(this);
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        super.onPause();
        application.setCallback(null);
    }

    /**
     * Initialize UI components
     */
    private void initSensorListView() {
        sensorListView = (ListView)getActivity().findViewById(R.id.sensor_list_layout_sensor_list);

        // add header and footer for list
        View headerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        sensorListView.addHeaderView(headerView);
        sensorListView.addFooterView(footerView);

        // set up click listener
        sensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: click on sensor list item");
                if(position>0 && position <= sensorList.size()) {
                    Sensor sensor = sensorList.get(position-1);
                    application.setCurrentSensor(sensor);
                    handleSensorListItemClick(sensor);
                }
            }
        });
    }

    /**
     * Initialize sensor list according to the currently selected sensor type, there are two scenarios
     *  1. Display My.SenZors
     *  2. Display Friends.SenZors
     */
    private void initSensorList() {
        // two sensor types to display
        //  1. My senzors
        //  2. Friends senzors
        if(SenzorApplication.SENSOR_TYPE.equalsIgnoreCase(SenzorApplication.MY_SENSORS)) {
            // display my sensors
            //  1. initialize my sensors
            //  2. initialize location listener
            //  3. create list view
            sensorList = application.getMySensorList();
            setUpActionBarTitle("My.senZors");
        } else {
            // display friends sensors
            //  1. initialize friends sensor list
            //  2. create list view
            sensorList = application.getFiendSensorList();
            setUpActionBarTitle("Friends.senZors");
        }
    }

    /**
     * Initialize empty view for list view
     * empty view need to be display when no sensors available
     */
    private void initEmptyView() {
        emptyView = (ViewStub) getActivity().findViewById(R.id.sensor_list_layout_empty_view);
        View inflatedEmptyView = emptyView.inflate();
        TextView emptyText = (TextView) inflatedEmptyView.findViewById(R.id.empty_text);
        emptyText.setText("No Friends.SenZors available");
        emptyText.setTypeface(typeface);
    }

    /**
     * Display sensor list
     * Basically setup list adapter if have items to display otherwise display empty view
     */
    private void displaySensorList() {
        // construct list adapter
        if(sensorList.size()>0) {
            adapter = new SensorListAdapter(SensorList.this.getActivity(), sensorList);
            adapter.notifyDataSetChanged();
            sensorListView.setAdapter(adapter);
        } else {
            adapter = new SensorListAdapter(SensorList.this.getActivity(), sensorList);
            sensorListView.setAdapter(adapter);
            sensorListView.setEmptyView(emptyView);
        }
    }

    /**
     * Set action bar title according to currently selected sensor type
     * Set custom font to title
     * @param title action bar title
     */
    private void setUpActionBarTitle(String title) {
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) (this.getActivity().findViewById(titleId));
        yourTextView.setTextColor(getResources().getColor(R.color.white));
        yourTextView.setTypeface(typeface);

        getActivity().getActionBar().setTitle(title);
    }

    /**
     * Handle list item click, there are tow scenarios here
     *  1. If clicked item is my sensor, get my current location via location service and display in a map
     *  2. If clicked item is friends sensor, send a GET query to server in order to get the friends location
     * @param sensor selected sensor
     */
    private void handleSensorListItemClick(Sensor sensor) {
        if(sensor != null) {
            if(sensor.isMySensor()) {
                // start location service to get my location
                if(NetworkUtil.isAvailableNetwork(SensorList.this.getActivity())) {
                    ActivityUtils.showProgressDialog(SensorList.this.getActivity(), "Accessing location...");

                    Intent serviceIntent = new Intent(getActivity(), GpsReadingService.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isMyLocation", true);
                    serviceIntent.putExtras(bundle);
                    getActivity().startService(serviceIntent);
                } else {
                    Log.w(TAG, "handleSensorListItemClick: not network connection available");
                    Toast.makeText(SensorList.this.getActivity(), "Cannot connect to service, please check your network connection", Toast.LENGTH_LONG).show();
                }
            } else {
                // friend sensor
                // so need to get request to server
                // send query to get data
                if(NetworkUtil.isAvailableNetwork(SensorList.this.getActivity())) {
                    if(application.getWebSocketConnection().isConnected()) {
                        ActivityUtils.showProgressDialog(SensorList.this.getActivity(), "Accessing location ...");
                        application.getWebSocketConnection().sendTextMessage("GET #lat #lon " + "@" + sensor.getUser().getPhoneNo());
                    } else {
                        Log.w(TAG, "handleSensorListItemClick: web socket not connected");
                        Toast.makeText(SensorList.this.getActivity(), "You are disconnected from senZors service", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "handleSensorListItemClick: no network connection available");
                    Toast.makeText(SensorList.this.getActivity(), "Cannot connect to server, please check your network connection", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            Log.e(TAG, "handleSensorListItemClick: invalid list item click(sensor is null)");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        if(message.obj instanceof LatLon) {
            // we handle LatLon messages only, from here
            // get address from location
            Log.d(TAG, "HandleMessage: message is a LatLon object so display map activity");
            ActivityUtils.cancelProgressDialog();
            LatLon latLon = (LatLon) message.obj;

            // start map activity to display location
            application.setLatLon(latLon);
            Intent intent = new Intent(SensorList.this.getActivity(), SensorDetailsActivity.class);
            intent.putExtra("com.score.senzors.pojos.Sensor", application.getCurrentSensor());
            this.startActivity(intent);
            SensorList.this.getActivity().overridePendingTransition(R.anim.right_in, R.anim.stay_in);
        } else {
            Log.e(TAG, "HandleMessage: message not a LatLon object");
        }

        return false;
    }

}
