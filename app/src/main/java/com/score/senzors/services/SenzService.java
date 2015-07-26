package com.score.senzors.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.score.senzors.application.SenzApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Service for listen UDP socket
 * Created by eranga on 7/26/15.
 */
public class SenzService extends Service {

    private static final String TAG = WebSocketService.class.getName();
    private SenzApplication application;
    private boolean isRunning;

    // we are listing for UDP socket
    private DatagramSocket socket;
    private InetAddress address;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        application = (SenzApplication) getApplication();
        this.isRunning = false;

        initUdpSocket();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new UdpListener().execute("UDP");
        this.isRunning = true;

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    /**
     * Initialize/Create UDP socket
     */
    private void initUdpSocket() {
        try {
            address = InetAddress.getByName(SenzApplication.SENZ_HOST);

            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket(SenzApplication.PORT, address);

                // send init message to server
                String message = "INIT";
                DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length());
                socket.send(packet);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // here we
        //  1. cancel/update all notifications
        //  2. delete all sensors in my sensor list
        //  3. send broadcast message about service disconnecting
        stopForeground(true);
        this.isRunning = false;
    }

    private void sendInit() {

    }

    /**
     * Async tack to listen UDP socket
     */
    public class UdpListener extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            initListener();

            return null;
        }

        /**
         * Initialize to listen for UDP socket
         */
        private void initListener() {
            byte[] message = new byte[1500];
            DatagramPacket packet = new DatagramPacket(message, message.length);

            // send
            try {
                while (true) {
                    socket.receive(packet);
                    String fromServer = new String(message, 0, packet.getLength());

                    System.out.println("-------------");
                    System.out.println(fromServer);
                }
            } catch (SocketException e) {
                isRunning = false;
                e.printStackTrace();
            } catch (IOException e) {
                isRunning = false;
                e.printStackTrace();
            }
        }
    }
}
