package com.score.senz.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.score.senz.ISenzService;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.handlers.SenzHandler;
import com.score.senz.listeners.ShareSenzListener;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.utils.NetworkUtil;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.RSAUtils;
import com.score.senz.utils.SenzParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/**
 * Remote service which handles UDP related functions
 *
 * @author eranga herath(erangaeb@gamil.com)
 */
public class RemoteSenzService extends Service implements ShareSenzListener {

    private static final String TAG = SenzService.class.getName();

    // senz service host and port
    private static final String SENZ_HOST = "udp.mysensors.info";
    private static final int SENZ_PORT = 9090;

    // we are listing for UDP socket
    private DatagramSocket socket;

    // server address
    private InetAddress address;

    // broadcast receiver to check network status changes
    private final BroadcastReceiver networkStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            Log.d(TAG, "Network status changed");

            //should check null because in air plan mode it will be null
            if (netInfo != null && netInfo.isConnected()) {
                // send ping from here
                initUdpSender();
            }
        }
    };

    // API end point of this service, we expose the endpoints define in ISenzService.aidl
    private final ISenzService.Stub apiEndPoints = new ISenzService.Stub() {
        @Override
        public void sendSenz(User user) throws RemoteException {
            Log.d(TAG, "Senz service call with senz " + user.getUsername());
        }

        @Override
        public void send(Senz senz) throws RemoteException {
            Log.d(TAG, "Senz service call with senz " + senz.getId());
            sendSenzMessage(senz);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return apiEndPoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        // TODO register AIDL here

        // Register network status receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStatusReceiver, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initUdpSocket();
        initUdpSender();
        initUdpListener();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroyed");

        // unregister connectivity listener
        unregisterReceiver(networkStatusReceiver);

        // restart service again
        // its done via broadcast receiver
        Intent intent = new Intent("com.score.senz.senzservice");
        sendBroadcast(intent);
    }

    /**
     * Initialize/Create UDP socket
     */
    private void initUdpSocket() {
        if (socket == null || socket.isClosed()) {
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Socket already initialized");
        }
    }

    /**
     * Start thread to send initial PING messages to server
     * We initialize UDP connection from here
     */
    private void initUdpSender() {
        if (socket != null) {
            new Thread(new Runnable() {
                public void run() {
                    // send ping message
                    sendPingMessage();
                }
            }).start();
        } else {
            Log.e(TAG, "Socket not connected");
        }
    }

    /**
     * Start thread for listen to UDP socket, all the incoming messages receives from
     * here, when message receives it should be broadcast or delegate to appropriate message
     * handler
     */
    private void initUdpListener() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] message = new byte[512];

                    while (true) {
                        // listen for senz
                        DatagramPacket receivePacket = new DatagramPacket(message, message.length);
                        socket.receive(receivePacket);
                        String senz = new String(message, 0, receivePacket.getLength());

                        Log.d(TAG, "SenZ received: " + senz);

                        SenzHandler.getInstance(RemoteSenzService.this, RemoteSenzService.this).handleSenz(senz);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Send ping message to server, this method will be invoked by a thread
     */
    private void sendPingMessage() {
        if (NetworkUtil.isAvailableNetwork(RemoteSenzService.this)) {
            try {
                String message;
                PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);
                User user = PreferenceUtils.getUser(RemoteSenzService.this);

                // create senz attributes
                HashMap<String, String> senzAttributes = new HashMap<>();
                senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

                // new senz object
                Senz senz = new Senz();
                senz.setSenzType(SenzTypeEnum.DATA);
                senz.setSender(new User("", user.getUsername()));
                senz.setReceiver(new User("", "mysensors"));
                senz.setAttributes(senzAttributes);

                // get digital signature of the senz
                String senzPayload = SenzParser.getSenzPayload(senz);
                String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                message = SenzParser.getSenzMessage(senzPayload, senzSignature);

                Log.d(TAG, "Ping to be send: " + message);

                // send message
                if (address == null) address = InetAddress.getByName(SENZ_HOST);
                DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), address, SENZ_PORT);
                socket.send(sendPacket);
            } catch (IOException | NoSuchAlgorithmException | NoUserException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Cannot send ping, No connection available");
        }
    }

    /**
     * Send SenZ message to SenZ service via UDP socket
     * Separate thread used to send messages asynchronously
     *
     * @param senz senz message
     */
    public void sendSenzMessage(final Senz senz) {
        if (NetworkUtil.isAvailableNetwork(RemoteSenzService.this)) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        PrivateKey privateKey = RSAUtils.getPrivateKey(RemoteSenzService.this);

                        // get digital signature of the senz
                        String senzPayload = SenzParser.getSenzPayload(senz);
                        String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
                        String message = SenzParser.getSenzMessage(senzPayload, senzSignature);

                        Log.d(TAG, "Senz to be send: " + message);

                        // send message
                        if (address == null) address = InetAddress.getByName(SENZ_HOST);
                        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), address, SENZ_PORT);
                        socket.send(sendPacket);
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.e(TAG, "Cannot send senz, No connection available");
        }
    }

    @Override
    public void onShareSenz(Senz senz) {
        sendSenzMessage(senz);
    }
}
