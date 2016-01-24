package com.score.locationz.application;

import android.app.Application;

import com.score.senzc.pojos.Senz;

/**
 * Application class to hold shared attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzApplication extends Application {

    private Senz senz;

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

    public Senz getSenz() {
        return senz;
    }

    public void setSenz(Senz senz) {
        this.senz = senz;
    }
}
