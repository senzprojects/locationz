package com.score.senz.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Service for listen UDP socket
 */
public class SenzService extends Service {

    private static final String TAG = SenzService.class.getName();

    // senz service host and port
    private static final String SENZ_HOST = "udp.mysensors.info";
    private static final int SENZ_PORT = 9090;

    // used to receive messages from various activities and services
    private Messenger senzServiceMessenger;

    // we are listing for UDP socket
    private DatagramSocket socket;

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
                while (true) try {
                    String message = "#ping";

                    // send message
                    InetAddress address = InetAddress.getByName(SENZ_HOST);
                    DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), address, SENZ_PORT);
                    socket.send(sendPacket);

                    // send ping in every minute
                    Thread.sleep(60000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
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
                    byte[] message = new byte[1500];
                    InetAddress address = InetAddress.getByName(SENZ_HOST);
                    DatagramPacket packet = new DatagramPacket(message, message.length, address, SENZ_PORT);

                    while (true) {
                        // listen for senz
                        socket.receive(packet);
                        String senz = new String(message, 0, packet.getLength());

                        Log.d(TAG, "SenZ received: " + senz);

                        // broadcast message or send message to query handler
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                        InetAddress address = InetAddress.getByName(SENZ_HOST);
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
