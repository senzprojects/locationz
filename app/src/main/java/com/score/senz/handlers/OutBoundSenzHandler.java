package com.score.senz.handlers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.score.senz.exceptions.NoUserException;
import com.score.senz.services.RemoteSenzService;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.RSAUtils;
import com.score.senz.utils.SenzParser;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/**
 * Handle all out going senz messages from here
 *
 * @author eranga herath(eranga.herath@pagero.com)
 */
public class OutBoundSenzHandler {

    private static final String TAG = SenzHandler.class.getName();
    private static OutBoundSenzHandler instance;

    // keeps weather service already bound or not
    boolean isServiceBound = false;

    // use to send senz messages to SenzService
    Messenger senzServiceMessenger;

    // connection for SenzService
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            senzServiceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            senzServiceMessenger = null;
        }
    };

    private OutBoundSenzHandler() {
    }

    public static OutBoundSenzHandler getInstance() {
        if (instance == null) {
            instance = new OutBoundSenzHandler();
        }

        return instance;
    }

    public void sendSenz(Context context, User receiver, boolean isDone) {
        // bind to senz service
        if (!isServiceBound) {
            context.bindService(new Intent(context, RemoteSenzService.class), senzServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }

        try {
            // create key pair
            PrivateKey privateKey = RSAUtils.getPrivateKey(context);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            if (isDone) senzAttributes.put("msg", "ShareDone");
            else senzAttributes.put("msg", "ShareFail");

            User user = PreferenceUtils.getUser(context);

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.DATA);
            senz.setReceiver(receiver);
            senz.setSender(user);
            senz.setAttributes(senzAttributes);

            // get digital signature of the senz
            String senzPayload = SenzParser.getSenzPayload(senz);
            String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
            String senzMessage = SenzParser.getSenzMessage(senzPayload, senzSignature);

            // send senz to server
            Message msg = new Message();
            msg.obj = senzMessage;

            System.out.println(senzMessage);
            try {
                senzServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | NoUserException e) {
            e.printStackTrace();
        }
    }

}
