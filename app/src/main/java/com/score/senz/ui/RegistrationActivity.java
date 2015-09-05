package com.score.senz.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.R;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.services.SenzService;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.PhoneBookUtils;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.RSAUtils;
import com.score.senz.utils.SenzParser;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/**
 * Activity class that handles user registrations
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class RegistrationActivity extends Activity implements View.OnClickListener {

    private static final String TAG = RegistrationActivity.class.getName();

    // registration deal with User object
    private User registeringUser;

    // UI fields
    private EditText editTextPhoneNo;
    private TextView countryCodeText;
    private TextView textViewHeaderText;
    private TextView textViewSignUpText;
    private RelativeLayout signUpButton;

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

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);

        initUi();
        LocalBroadcastManager.getInstance(this).registerReceiver(senzMessageReciver, new IntentFilter("DATA"));
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();

        // bind to senz service
        if (!isServiceBound) {
            bindService(new Intent(RegistrationActivity.this, SenzService.class), senzServiceConnection, Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(senzMessageReciver, new IntentFilter("DATA"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the service
        if (isServiceBound) {
            unbindService(senzServiceConnection);
            isServiceBound = false;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(senzMessageReciver);
    }

    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        editTextPhoneNo = (EditText) findViewById(R.id.registration_phone_no);
        signUpButton = (RelativeLayout) findViewById(R.id.registration_sign_up_button);
        textViewHeaderText = (TextView) findViewById(R.id.registration_header_text);
        textViewSignUpText = (TextView) findViewById(R.id.registration_sign_up_text);
        signUpButton.setOnClickListener(RegistrationActivity.this);

        String countryCode = PhoneBookUtils.getCountryCode(this);
        countryCodeText = (TextView) findViewById(R.id.country_code);
        if (!countryCode.isEmpty())
            countryCodeText.setText(countryCode);

        textViewHeaderText.setTypeface(typefaceThin, Typeface.BOLD);
        textViewSignUpText.setTypeface(typefaceThin, Typeface.BOLD);
        editTextPhoneNo.setTypeface(typefaceThin, Typeface.NORMAL);
    }

    /**
     * Initialize user object
     */
    private void initRegisteringUser() {
        String countryCode = countryCodeText.getText().toString().trim();
        String phoneNo = editTextPhoneNo.getText().toString().trim();
        //String internationalPhoneNo = countryCode + phoneNo.substring(phoneNo.length() - 9);

        registeringUser = new User("0", phoneNo, "");
        registeringUser.setUsername("Me");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v == signUpButton) {
            signUp();
        }
    }

    /**
     * Sign-up button action,
     * Need to connect web socket and send PUT query to register
     * the user
     */
    private void signUp() {
        initRegisteringUser();
        PreferenceUtils.saveUser(this, registeringUser);
        ActivityUtils.hideSoftKeyboard(this);
        registerUser();
    }

    /**
     * Create user via sending PUT query to server,
     * need to send the query via the web socket
     */
    private void registerUser() {
        try {
            // create key pair
            RSAUtils.initKeys(this);
            PrivateKey privateKey = RSAUtils.getPrivateKey(this);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            senzAttributes.put("pubkey", PreferenceUtils.getRsaKey(this, RSAUtils.PUBLIC_KEY));

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.SHARE);
            senz.setReceiver("mysensors");
            senz.setSender(registeringUser.getPhoneNo());
            senz.setAttributes(senzAttributes);

            // get digital signature of the senz
            String senzPayload = SenzParser.getSenzPayload(senz);
            String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);
            String senzMessage = SenzParser.getSenzMessage(senzPayload, senzSignature);

            System.out.println("-------------");
            System.out.println(senzPayload);
            System.out.println(senzMessage);
            System.out.println("-------------");

            // send senz to server
            Message msg = new Message();
            msg.obj = senzMessage;
            try {
                senzServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver senzMessageReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    /**
     * @param intent
     */
    private void handleMessage(Intent intent) {
        String action = intent.getAction();

        if (action.equals("DATA")) {
            boolean senzMessage = intent.getExtras().getBoolean("extra");
            if (senzMessage) {
                Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
                // navigate home
            } else {
                Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
                // ask user to retry

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d("TAG", "OnBackPressed: go back");
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }
}