package com.score.senz.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteConstraintException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.score.senz.ISenzService;
import com.score.senz.R;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.utils.NotificationUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

/**
 * Created by eranga on 1/24/16
 */
public class SenzShareHandlerService extends Service {

    private static final String TAG = LocationService.class.getName();

    // senz message
    private Senz senz;

    // keeps weather service already bound or not
    boolean isServiceBound = false;

    // service interface
    private ISenzService senzService = null;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            senzService = null;
            Log.d("TAG", "Disconnected from senz service");
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        this.senz = intent.getExtras().getParcelable("SENZ");

        // bind with senz service
        if (!isServiceBound) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
            bindService(serviceIntent, senzServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }

        saveSenz();

        return START_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isServiceBound) {
            unbindService(senzServiceConnection);
            isServiceBound = false;
        }
    }

    private void saveSenz() {
        SenzorsDbSource dbSource = new SenzorsDbSource(this);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);

        // if senz already exists in the db, SQLiteConstraintException should throw
        try {
            NotificationUtils.showNotification(this, this.getString(R.string.new_senz), "LocationZ received from @" + senz.getSender().getUsername());
            sendResponse(sender, true);
        } catch (SQLiteConstraintException e) {
            sendResponse(sender, false);
            Log.e(TAG, e.toString());
        }
    }

    private void sendResponse(User receiver, boolean isDone) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) senzAttributes.put("msg", "ShareDone");
            else senzAttributes.put("msg", "ShareFail");

            String id = "_ID";
            String signature = "";
            SenzTypeEnum senzType = SenzTypeEnum.DATA;
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
