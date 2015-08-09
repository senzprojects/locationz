package com.score.senz.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

    // we are listing for UDP socket
    private DatagramSocket socket;
    private InetAddress address;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
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
        try {
            address = InetAddress.getByName("10.2.2.132");
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket();
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start thread to send periodic ping messages to server, in current implementation
     * we are sending messages in every minute(60, 000  ms)
     *
     * The purpose of sending ping message is notify NAT table updates to server
     */
    private void initUdpSender() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        String message = "ping";

                        // send message
                        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), address, 9090);
                        socket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
                byte[] message = new byte[1500];
                DatagramPacket packet = new DatagramPacket(message, message.length, address, 9090);

                try {
                    while (true) {
                        socket.receive(packet);
                        String fromServer = new String(message, 0, packet.getLength());

                        System.out.println("-------------");
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
