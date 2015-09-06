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

    // UDP server host and port
    public final static String SENZ_HOST = "10.2.2.132";
    public final static int SENZ_PORT = 9090;

    // web socket connection share in application
    // we are using one instance of web socket in all over the application
    public final WebSocket webSocketConnection = new WebSocketConnection();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
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

}
