package com.score.locationz.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteConstraintException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.score.locationz.R;
import com.score.locationz.db.SenzorsDbSource;
import com.score.locationz.utils.NotificationUtils;
import com.score.senz.ISenzService;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

/**
 * Created by eranga on 1/24/16
 */
public class ShareHandlerService extends Service implements ServiceConnection {

    private static final String TAG = ShareHandlerService.class.getName();

    private Senz senz;

    private boolean isServiceConnected = false;

    // service interface
    private ISenzService senzService = null;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Connected with senz service");
        senzService = ISenzService.Stub.asInterface(service);
        isServiceConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        senzService = null;
        isServiceConnected = false;
        Log.d("TAG", "Disconnected from senz service");
    }

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
    public void onCreate() {
        // bind to senz service
        Intent serviceIntent = new Intent();
        serviceIntent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        senz = intent.getExtras().getParcelable("SENZ");

        while (isServiceConnected) {
            saveSenz(senz);

            break;
        }

        return START_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(this);
    }

    private void saveSenz(Senz senz) {
        SenzorsDbSource dbSource = new SenzorsDbSource(this);
        User sender = dbSource.getOrCreateUser(senz.getSender().getUsername());
        senz.setSender(sender);

        Log.d(TAG, "save senz");

        // if senz already exists in the db, SQLiteConstraintException should throw
        try {
            dbSource.createSenz(senz);

            NotificationUtils.showNotification(this, this.getString(R.string.new_senz), "LocationZ received from @" + senz.getSender().getUsername());
            sendResponse(sender, true);
        } catch (SQLiteConstraintException e) {
            sendResponse(sender, false);
            Log.e(TAG, e.toString());
        }
    }

    private void sendResponse(User receiver, boolean isDone) {
        Log.d(TAG, "send response");
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
