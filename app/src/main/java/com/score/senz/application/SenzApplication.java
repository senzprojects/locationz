package com.score.senz.application;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.pojos.LatLon;
import com.score.senz.pojos.Query;
import com.score.senz.pojos.Sensor;
import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;

import java.util.ArrayList;

/**
 * Application class to hold shared attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzApplication extends Application {

    // determine sensor type
    //  1. my sensors
    //  2. friends sensors
    public final static String MY_SENSORS = "MY_SENSORS";
    public final static String FRIENDS_SENSORS = "FRIENDS_SENSORS";
    public static String SENSOR_TYPE = MY_SENSORS;

    // UDP server host and port
    public final static String SENZ_HOST = "10.2.2.132";
    public final static int SENZ_PORT = 9090;

    // web socket server up and running in this API
    // need to connect this server when starting the app
    //public final static String WEB_SOCKET_URI = "ws://10.2.4.14:8080";
    public final static String WEB_SOCKET_URI = "ws://connect.mysensors.mobi:8080";

    // web socket connection share in application
    // we are using one instance of web socket in all over the application
    public final WebSocket webSocketConnection = new WebSocketConnection();

    // keep sensors
    //  1. my sensors(ex: location)
    //  2. friends sensors(sensors shared by friends to me)
    private ArrayList<Sensor> friendSensorList;
    private ArrayList<Sensor> mySensorList;

    // to types of queries need to be shared in application
    //  1. GET query from friend
    //  2. DATA query
    private Query requestQuery;

    // keep current location
    // this location display on google map
    LatLon latLon;

    // keep track with current senzor
    // we mainly focus on type of current sensor, all the logic depends on the sensor type
    Sensor currentSensor;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize sensor lists
        // initially add my location to my sensor list
        setFiendSensorList(new ArrayList<Sensor>());
        setMySensorList(new ArrayList<Sensor>());
    }

    public WebSocket getWebSocketConnection() {
        return webSocketConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message message) {
            if (realCallback!=null) {
                realCallback.handleMessage(message);
            }
        }
    };

    Handler.Callback realCallback=null;

    public Handler getHandler() {
        return handler;
    }

    public void setCallback(Handler.Callback c) {
        realCallback = c;
    }

    public ArrayList<Sensor> getFiendSensorList() {
        return friendSensorList;
    }

    public void setFiendSensorList(ArrayList<Sensor> friendSensorList) {
        this.friendSensorList = friendSensorList;
    }

    public ArrayList<Sensor> getMySensorList() {
        return mySensorList;
    }

    public void setMySensorList(ArrayList<Sensor> mySensorList) {
        this.mySensorList = mySensorList;
    }

    public Query getRequestQuery() {
        return requestQuery;
    }

    public void setRequestQuery(Query requestQuery) {
        this.requestQuery = requestQuery;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public void setLatLon(LatLon latLon) {
        this.latLon = latLon;
    }

    public Sensor getCurrentSensor() {
        return currentSensor;
    }

    public void setCurrentSensor(Sensor currentSensor) {
        this.currentSensor = currentSensor;
    }

    /**
     * Set up SenZors app, we do
     *  1. set the app for first time
     *  2. initialize sensors
     *  3. read contact list
     */
    public void setUpSenzors() {
        initMySensors();
        initFriendsSensors();
    }

    /**
     * Initialize friends sensor list
     * Get saved friend sensors in database and load to friend sensor list
     */
    public void initFriendsSensors() {
        friendSensorList = (ArrayList<Sensor>)new SenzorsDbSource(this).getSensors(false);
    }

    /**
     * Initialize my sensor list
     * Get all available sensors of me and add to sensor list shared in application
     */
    public void initMySensors() {
        mySensorList = (ArrayList<Sensor>)new SenzorsDbSource(this).getSensors(true);
    }

    /**
     * Delete all sensors in my sensor list
     */
    public void emptyMySensors() {
        for(Sensor sensor: this.mySensorList) {
            mySensorList.remove(sensor);
        }
    }
}
