package com.score.senz.application;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

/**
 * Application class to hold shared attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzApplication extends Application {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
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
            if (realCallback != null) {
                realCallback.handleMessage(message);
            }
        }
    };

    Handler.Callback realCallback = null;

    public Handler getHandler() {
        return handler;
    }

    public void setCallback(Handler.Callback c) {
        realCallback = c;
    }

}
