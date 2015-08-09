package com.score.senz.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service for listen UDP socket
 * Created by eranga on 7/26/15.
 */
public class SenzService extends Service {

    private static final String TAG = WebSocketService.class.getName();
    final Handler handler = new Handler();
    private boolean isRunning;
    // we are listing for UDP socket
    private DatagramSocket socket;
    private InetAddress address;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        this.isRunning = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initUdpSocket();
        //new UdpSender().execute("ping");
        //new UdpListener().execute("UDP");
        startUdpSender();
        startUdpListener();

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

    /**
     * Initialize/Create UDP socket
     */
    private void initUdpSocket() {
        try {
            address = InetAddress.getByName("10.2.2.132");
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void startUdpSender() {
        new Thread(new Runnable() {
            public void run() {
                for (; ; ) {
                    try {
                        String message = "ping";

                        // send message
                        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), address, 9090);
                        socket.send(sendPacket);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
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

    private void startUdpListener() {
        new Thread(new Runnable() {
            public void run() {
                byte[] message = new byte[1500];
                DatagramPacket packet = new DatagramPacket(message, message.length, address, 9090);

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
        }).start();
    }

    /**
     * Async task to send data to senz server
     */
    public class UdpSender extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String[] params) {
            initSender();

            return null;
        }

        private void initSender() {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                String message = "ping";

                                // send message
                                DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), address, 9090);
                                socket.send(sendPacket);
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
            timer.schedule(task, 0, 1000);
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
        }
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
            DatagramPacket packet = new DatagramPacket(message, message.length, address, 9090);

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
