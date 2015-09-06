package com.score.senz.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.R;
import com.score.senz.application.SenzApplication;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.services.SenzService;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.NetworkUtil;
import com.score.senz.utils.PhoneBookUtils;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.RSAUtils;
import com.score.senz.utils.SenzParser;
import com.score.senz.utils.SenzUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    // keep user object to use in this activity
    User loginUser;

    // use to send senz messages to SenzService
    Messenger senzServiceMessenger;

    private static final String TAG = LoginActivity.class.getName();

    // connection for SenzService
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            senzServiceMessenger = new Messenger(service);

        }

        public void onServiceDisconnected(ComponentName className) {
            senzServiceMessenger = null;
        }
    };

    private EditText editTextPhoneNo;
    private TextView headerText;
    private TextView signUpText;
    private TextView registerLink;
    private RelativeLayout signInButton;
    private RelativeLayout signUpButton;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // start senz service
        Intent serviceIntent = new Intent(LoginActivity.this, SenzService.class);
        startService(serviceIntent);

        initUi();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();

        bindService(new Intent(LoginActivity.this, SenzService.class), senzServiceConnection, Context.BIND_AUTO_CREATE);

        displayUserCredentials();
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        editTextPhoneNo = (EditText) findViewById(R.id.login_phone_no);
        signInButton = (RelativeLayout) findViewById(R.id.sign_in_button_panel);
        signUpButton = (RelativeLayout) findViewById(R.id.not_registered);
        headerText = (TextView) findViewById(R.id.header_text);
        signUpText = (TextView) findViewById(R.id.sign_up_text);
        registerLink = (TextView) findViewById(R.id.link);
        signInButton.setOnClickListener(LoginActivity.this);
        signUpButton.setOnClickListener(LoginActivity.this);

        headerText.setTypeface(typefaceThin, Typeface.BOLD);
        signUpText.setTypeface(typefaceThin, Typeface.BOLD);
        editTextPhoneNo.setTypeface(typefaceThin, Typeface.NORMAL);
        registerLink.setTypeface(typefaceThin, Typeface.BOLD);

        displayUserCredentials();
    }

    /**
     * Set user fields in UI
     * actually display phone no and password in UI
     */
    private void displayUserCredentials() {
        try {
            loginUser = PreferenceUtils.getUser(LoginActivity.this);
            editTextPhoneNo.setText(loginUser.getPhoneNo());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v == signInButton) {
            // send
            Message msg = new Message();
            String senz = registerUser();
            msg.obj = senz;
            try {
                senzServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == signUpButton) {
            switchToRegister();
        }
    }

    /**
     * Login action
     * Connect to web socket and send phone no password to server
     */
    private void login() {
        initLoginUser();

        if (NetworkUtil.isAvailableNetwork(LoginActivity.this)) {
            if (ActivityUtils.isValidLoginFields(loginUser)) {
                // start senz service
            } else {
                Log.d(TAG, "Login: empty phone no");
                Toast.makeText(LoginActivity.this, "Invalid input fields", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Login: no network connection");
            Toast.makeText(LoginActivity.this, "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initialize login user according to display content
     */
    private void initLoginUser() {
        String countryCode = PhoneBookUtils.getCountryCode(this);
        String phoneNo = editTextPhoneNo.getText().toString().trim();
        String internationalPhoneNo = countryCode + phoneNo.substring(phoneNo.length() - 9);

        loginUser = new User("0", internationalPhoneNo, "");
        loginUser.setUsername("Me");
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void switchToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        this.startActivity(intent);
        LoginActivity.this.overridePendingTransition(R.anim.right_in, R.anim.left_out);

        LoginActivity.this.finish();
    }

    /**
     * Switch to register activity
     * This method will be call after successful login
     */
    private void switchToRegister() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        this.startActivity(intent);
        LoginActivity.this.overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);
    }

    /**
     * Set up app when user first time login to the system
     * Create user if no saved user
     * save my sensors in db
     */
    private void setUpApp() {
        try {
            PreferenceUtils.getUser(LoginActivity.this);
        } catch (NoUserException e) {
            e.printStackTrace();

            // no user means first time login
            User user = new SenzorsDbSource(this).getOrCreateUser(loginUser.getPhoneNo());
            user.setPassword(loginUser.getPassword());
            PreferenceUtils.saveUser(this, user);
            SenzUtils.addMySensorsToDb(this, user);
        } finally {
            //((SenzApplication) getApplication()).setUpSenzors();
        }
    }

    /**
     * Create user via sending PUT query to server,
     * need to send the query via the web socket
     */
    private String registerUser() {
        // send public key to SenZ server(via senz message)
        try {
            // create key pair
            RSAUtils.initKeys(this);
            PublicKey publicKey = RSAUtils.getPublicKey(this);
            PrivateKey privateKey = RSAUtils.getPrivateKey(this);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            senzAttributes.put("pubkey", PreferenceUtils.getRsaKey(this, RSAUtils.PUBLIC_KEY));

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.SHARE);
            senz.setReceiver("mysensors");
            senz.setSender(editTextPhoneNo.getText().toString().trim());
            senz.setAttributes(senzAttributes);

            // get digital signature of the senz
            String senzPayload = SenzParser.getSenzPayload(senz);
            String senzSignature = RSAUtils.getDigitalSignature(senzPayload.replaceAll(" ", ""), privateKey);

            System.out.println("-------------");
            System.out.println(senzPayload);
            System.out.println("-------------");

            SenzParser.getSenzMessage(senzPayload, senzSignature);
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

        return "";
    }

}

