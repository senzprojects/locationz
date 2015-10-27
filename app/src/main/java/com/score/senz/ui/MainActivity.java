package com.score.senz.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.score.senz.ISenzService;
import com.score.senz.R;
import com.score.senz.services.RemoteSenzService;

public class MainActivity extends Activity implements View.OnClickListener {

    ISenzService senzService = null;

    Button connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(this);
        Intent serviceIntent = new Intent(MainActivity.this, RemoteSenzService.class);
        startService(serviceIntent);

        Intent intent = new Intent();
        intent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d("TAG", "binding...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            senzService = ISenzService.Stub.asInterface(service);
            Log.d("TAG", "Connected...");
        }

        public void onServiceDisconnected(ComponentName className) {
            senzService = null;
            Log.d("TAG", "Disconnected...");
        }
    };

    @Override
    public void onClick(View v) {
        if (v == connect) {
            try {
                senzService.send(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
