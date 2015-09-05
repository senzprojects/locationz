package com.score.senz.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.handlers.SenzHandler;
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
 * Service for listen UDP socket
 */
public class SenzService extends Service {

    private static final String TAG = SenzService.class.getName();

    // senz service host and port
    private static final String SENZ_HOST = "udp.mysensors.info";
    //private static final String SENZ_HOST = "10.2.2.132";
    private static final int SENZ_PORT = 9090;

    // used to receive messages from various activities and services
    private Messenger senzServiceMessenger;

    // we are listing for UDP socket
    private DatagramSocket socket;

    // server address
    private InetAddress address;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        senzServiceMessenger = new Messenger(new MessageHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initUdpSocket();
        initPingSender();
        initUdpListener();

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return senzServiceMessenger.getBinder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        stopForeground(true);
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
        }
    }

    /**
     * Start thread to send periodic ping messages to server, in current implementation
     * we are sending messages in every minute(60, 000  ms)
     * <p/>
     * The purpose of sending ping message is notify NAT table updates to server
     */
    private void initPingSender() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        // send ping message
                        sendPing();
                    } catch (IOException | NoSuchAlgorithmException | NoUserException | SignatureException |
                            InvalidKeyException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    } finally {
                        // send ping in every 20 seconds
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
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

                        SenzHandler.getInstance().handleSenz(SenzService.this, senz);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Send ping message to server, this method will be invoked by a thread
     *
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoUserException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws IOException
     */
    private void sendPing() throws InvalidKeySpecException, NoSuchAlgorithmException, NoUserException, SignatureException, InvalidKeyException, IOException {
        if (NetworkUtil.isAvailableNetwork(SenzService.this)) {
            String message;
            PrivateKey privateKey = RSAUtils.getPrivateKey(SenzService.this);
            User user = PreferenceUtils.getUser(SenzService.this);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz object
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.DATA);
            senz.setSender(user.getPhoneNo());
            senz.setReceiver("mysensors");
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
        } else {
            Log.e(TAG, "Cannot send ping, No connection available");
        }
    }

    /**
     * Activities and other Services sends messages to SenzService via this
     * message handler
     */
    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String senz = (String) message.obj;
            Log.d(TAG, "SenZ to be sent: " + senz);

            sendSenz(senz);
        }

        /**
         * Send SenZ message to SenZ service via UDP socket
         * Separate thread used to send messages asynchronously
         *
         * @param senz senz message
         */
        private void sendSenz(final String senz) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        // send message to SenZ service
                        if (address == null) address = InetAddress.getByName(SENZ_HOST);
                        DatagramPacket sendPacket = new DatagramPacket(senz.getBytes(), senz.length(), address, SENZ_PORT);
                        socket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}
