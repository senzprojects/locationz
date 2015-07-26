package com.score.senzors.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.exceptions.InvalidQueryException;
import com.score.senzors.pojos.LatLon;
import com.score.senzors.pojos.Query;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.pojos.User;
import com.score.senzors.services.GpsReadingService;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Handler class for incoming queries
 * Handle following queries
 *  1. STATUS
 *  2. SHARE
 *  3. GET
 *  4. LOGIN
 *  5. DATA
 *
 *  @author Eranga Herath(erangaeb@gmail.com)
 */
public class QueryHandler {

    private static final String TAG = QueryHandler.class.getName();

    /**
     * Generate login query and send to server
     * @param user login user
     */
    public static String getLoginQuery(User user, String sessionKey) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // generate login query with user credentials
        // sample query - LOGIN #name era #skey 123 @mysensors
        String command = "LOGIN";
        HashMap<String, String> params = new HashMap<String, String>();

        // need to append session key to password --> key = base64(sha1(password)) + session_key
        // then get SHA1 of key and encode with base64
        StringBuilder builder = new StringBuilder();
        builder.append(CryptoUtils.encodeMessage(user.getPassword()));
        builder.append(sessionKey);
        String key = builder.toString();
        params.put("hkey", CryptoUtils.encodeMessage(key));
        params.put("name", user.getPhoneNo());

        return QueryParser.getMessage(new Query(command, "mysensors", params));
    }

    /**
     * Generate PUT query to send to server,
     * server use PUT queries when creating users, need to encrypt phone no with server public key
     * and send it as "hkey"
     *
     * @param user user object
     */
    public static String getRegistrationQuery(User user) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // construct PUT message
        String command = "PUT";
        final HashMap<String, String> params = new HashMap<String, String>();

        // encode password/pin with SHA1 and encode with Base64
        params.put("hkey", CryptoUtils.encodeMessage(user.getPassword()));
        params.put("name", user.getPhoneNo());

        return QueryParser.getMessage(new Query(command, "mysensors", params));
    }

    /**
     * Handle query message from web socket
     *
     * @param application application object
     * @param payload payload from server
     */
    public static void handleQuery(SenzorApplication application, String payload) {
        try {
            // need to parse query in order to further processing
            Query query = QueryParser.parse(payload);

            if(query.getCommand().equalsIgnoreCase("STATUS")) {
                // STATUS query
                // handle LOGIN and SHARE status from handleStatus
                handleStatusQuery(application, query);
            } else if(query.getCommand().equalsIgnoreCase("SHARE")) {
                // SHARE query
                // handle SHARE query from handleShare
                handleShareQuery(application, query);
            } else if(query.getCommand().equalsIgnoreCase(":SHARE")) {
                // UN-SHARE query
                // handle Un-SHARE query from handleUnShare
                handleUnShareQuery(application, query);
            } else if (query.getCommand().equalsIgnoreCase("GET")) {
                // GET query
                // handle via handleGet
                handleGetQuery(application, query);
            } else if(query.getCommand().equalsIgnoreCase("DATA")) {
                // DATA query
                // handle via handleData
                handleDataQuery(application, query);
            } else {
                // invalid query or not supporting query
                Log.e(TAG, "INVALID/UN-SUPPORTING query");
            }
        } catch (InvalidQueryException e) {
            Log.e(TAG, "HandleQuery: " + e.getMessage());
        }
    }

    /**
     * Handle STATUS query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleStatusQuery(SenzorApplication application, Query query) {
        // get status from query
        String status = "success";

        if (query.getParams().containsKey("login")) {
            // login status
            status = query.getParams().get("login");
        } else if (query.getParams().containsKey("share")) {
            // share status
            status = query.getParams().get("share");
        }

        // just send status to available handler
        sendMessage(application, status);
    }

    /**
     * Handle SHARE query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleShareQuery(SenzorApplication application, Query query) {
        // get or create matching user
        // create/save new sensor in db
        String username = PhoneBookUtils.getContactName(application, query.getUser());
        User user = new SenzorsDbSource(application.getApplicationContext()).getOrCreateUser(query.getUser());
        user.setUsername(username);
        Sensor sensor = new Sensor("0", "Location", "Location", false, user, null);

        try {
            // save sensor in db and refresh friend sensor list
            new SenzorsDbSource(application.getApplicationContext()).addSensor(sensor);
            application.initFriendsSensors();
            Log.d(TAG, "HandleShareQuery: saved sensor from - " + user.getUsername());

            // currently we have to launch friend sensor
            // update notification to notify user about incoming query/ share request
            SenzorApplication.SENSOR_TYPE = SenzorApplication.FRIENDS_SENSORS;
            NotificationUtils.updateNotification(application.getApplicationContext(), "Location @" + user.getUsername());
        } catch (Exception e) {
            // Db exception here
            Log.e(TAG, "HandleShareQuery: db error " + e.toString());
        }
    }

    /**
     * Handle UNSHARE query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleUnShareQuery(SenzorApplication application, Query query) {
        // get match user and sensor
        User user = new SenzorsDbSource(application.getApplicationContext()).getOrCreateUser(query.getUser());
        Sensor sensor = new Sensor("0", "Location", "Location", false, user, null);
        try {
            // delete sensor  from db
            // new SenzorsDbSource(application.getApplicationContext()).deleteSharedUser(user);
            new SenzorsDbSource(application.getApplicationContext()).deleteSensorOfUser(sensor);
            application.initFriendsSensors();
            Log.d(TAG, "HandleUnShareQuery: deleted sensor from - " + user.getPhoneNo());

            // currently we have to launch friend sensor
            // update notification to notify user about incoming query/ share request
            SenzorApplication.SENSOR_TYPE = SenzorApplication.FRIENDS_SENSORS;
            NotificationUtils.updateNotification(application.getApplicationContext(), "Unshared Location @" + user.getUsername());
        } catch (Exception e) {
            // Db exception here
            Log.e(TAG, "HandleUnShareQuery: db error " + e.toString());
        }
    }

    /**
     * Handle GET query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleGetQuery(SenzorApplication application, Query query) {
        // get location by starting location service
        if(application.getWebSocketConnection().isConnected()) {
            // current location request is from web socket service
            // start location service
            application.setRequestQuery(query);
            Intent serviceIntent = new Intent(application.getApplicationContext(), GpsReadingService.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isMyLocation", false);
            serviceIntent.putExtras(bundle);
            application.getApplicationContext().startService(serviceIntent);
        }
    }

    /**
     * Handle DATA query from server
     * @param application application
     * @param query parsed query
     */
    private static void handleDataQuery(SenzorApplication application, Query query) {
        if(query.getUser().equalsIgnoreCase("mysensors")) {
            if (query.getParams().containsKey("pubkey")) {
                ExtractServerKeys(application, query);
            } else {
                // this is a status query, just send status to available handler
                // @mysensors DATA #msg LoginSuccess
                String status = query.getParams().get("msg");
                if (status != null && !status.equalsIgnoreCase("UnsupportedQueryType"))
                    sendMessage(application, status);
            }
        } else {
            // from a specific user
            // create LatLon object from query params
            // we assume incoming query contains lat lon values
            LatLon latLon = new LatLon(query.getParams().get("lat"), query.getParams().get("lon"));
            sendMessage(application, latLon);
        }
    }

    /**
     * Extract server keys from query, two types of keys
     *      1. server public key
     *      2. session key
     * @param query parsed query
     */
    private static void ExtractServerKeys(SenzorApplication application, Query query) {
        // receives server public key and session key
        // @mysensors DATA #pubkey <public key> #websocketkey <session key>
        try {
            CryptoUtils.saveServerPublicKey(application, query.getParams().get("pubkey"));
            PreferenceUtils.saveSessionKey(application, query.getParams().get("websocketkey"));
            sendMessage(application, "SERVER_KEY_EXTRACTION_SUCCESS");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());

            sendMessage(application, "SERVER_KEY_EXTRACTION_FAIL");
        }
    }

    /**
     * Send message to appropriate handler
     * @param application application
     * @param obj payload from server
     */
    private static void sendMessage(SenzorApplication application, Object obj) {
        Message message = Message.obtain();
        message.obj = obj;
        if (application.getHandler()!=null) {
            application.getHandler().sendMessage(message);
        }
    }

}
