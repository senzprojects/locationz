package com.score.locationz.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.score.senz.ISenzService;

import java.util.ArrayList;

/**
 * Created by eranga on 1/25/16.
 */
public class SenzServiceConnection implements ServiceConnection {

    private static final String TAG = SenzServiceConnection.class.getSimpleName();

    private Context mContext = null;
    private ISenzService senzService = null;
    private ArrayList<Runnable> runnableArrayList;
    private Boolean isConnected = false;

    public SenzServiceConnection(Context context) {
        mContext = context;
        runnableArrayList = new ArrayList<>();
    }

    public ISenzService getInterface() {
        return senzService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.v(TAG, "Connected Service: " + name);
        senzService = ISenzService.Stub.asInterface(service);
        isConnected = true;

        for (Runnable action : runnableArrayList) {
            action.run();
        }
        runnableArrayList.clear();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        try {
            senzService = null;
            mContext.unbindService(this);
            isConnected = false;
            Log.v(TAG, "Disconnected Service: " + name);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void executeAfterServiceConnected(Runnable action) {
        Log.v(TAG, "executeAfterServiceConnected");

        if (isConnected) {
            Log.v(TAG, "Service already connected, execute now");
            action.run();
        } else {
            // this action will be executed at the end of onServiceConnected method
            Log.v(TAG, "Service not connected yet, execute later");
            runnableArrayList.add(action);
        }
    }
}
