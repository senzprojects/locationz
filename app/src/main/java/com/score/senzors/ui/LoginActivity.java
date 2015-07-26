package com.score.senzors.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;
import com.score.senzors.services.WebSocketService;
import com.score.senzors.utils.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener, Handler.Callback {

    private static final String TAG = LoginActivity.class.getName();

    private SenzorApplication application;
    private DataUpdateReceiver dataUpdateReceiver;

    private EditText editTextPhoneNo;
    private EditText editTextPassword;
    private TextView headerText;
    private TextView signUpText;
    private TextView link;
    private RelativeLayout signInButton;
    private RelativeLayout signUpButton;

    // keep user object to use in this activity
    User loginUser;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        application = (SenzorApplication) this.getApplication();
        initUi();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
        application.setCallback(this);
        displayUserCredentials();

        // register broadcast receiver from here
        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(WebSocketService.WEB_SOCKET_CONNECTED);
        registerReceiver(dataUpdateReceiver, intentFilter);
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();

        // un-register broadcast receiver from here
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        editTextPhoneNo = (EditText) findViewById(R.id.login_phone_no);
        editTextPassword = (EditText) findViewById(R.id.login_password);
        signInButton = (RelativeLayout) findViewById(R.id.sign_in_button_panel);
        signUpButton = (RelativeLayout) findViewById(R.id.not_registered);
        headerText = (TextView) findViewById(R.id.header_text);
        signUpText = (TextView) findViewById(R.id.sign_up_text);
        link = (TextView) findViewById(R.id.link);
        signInButton.setOnClickListener(LoginActivity.this);
        signUpButton.setOnClickListener(LoginActivity.this);

        headerText.setTypeface(typefaceThin, Typeface.BOLD);
        signUpText.setTypeface(typefaceThin, Typeface.BOLD);
        editTextPhoneNo.setTypeface(typefaceThin, Typeface.NORMAL);
        editTextPassword.setTypeface(typefaceThin, Typeface.NORMAL);
        link.setTypeface(typefaceThin, Typeface.BOLD);

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
            editTextPassword.setText(loginUser.getPassword());
        } catch (NoUserException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v==signInButton) {
            login();
        } else if(v==signUpButton) {
            switchToRegister();
        }
    }

    /**
     * Login action
     * Connect to web socket and send phone no password to server
     */
    private void login() {
        initLoginUser();

        if(NetworkUtil.isAvailableNetwork(LoginActivity.this)) {
            if(ActivityUtils.isValidLoginFields(loginUser)) {
                // we are authenticate with web sockets
                if(!application.getWebSocketConnection().isConnected()) {
                    ActivityUtils.hideSoftKeyboard(this);
                    ActivityUtils.showProgressDialog(LoginActivity.this, "Connecting to senZors...");

                    Intent serviceIntent = new Intent(LoginActivity.this, WebSocketService.class);
                    startService(serviceIntent);
                } else {
                    Log.d(TAG, "Login: already connected to web socket");
                }
            } else {
                Log.d(TAG, "Login: empty phone no/password");
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
        String password = editTextPassword.getText().toString().trim();
        String internationalPhoneNo = countryCode + phoneNo.substring(phoneNo.length() - 9);

        loginUser = new User("0", internationalPhoneNo, password);
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
            application.setUpSenzors();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        ActivityUtils.cancelProgressDialog();

        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            if (payLoad.equalsIgnoreCase("SERVER_KEY_EXTRACTION_SUCCESS")) {
                Log.d(TAG, "HandleMessage: server key extracted");

                // server key extraction success
                // so send PUT query to create user
                try {
                    if(application.getWebSocketConnection().isConnected()) {
                        String loginQuery = QueryHandler.getLoginQuery(loginUser, PreferenceUtils.getSessionKey(this));
                        Log.d(TAG, "------login query------");
                        Log.d(TAG, loginQuery);
                        application.getWebSocketConnection().sendTextMessage(loginQuery);
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                }
            } else if(payLoad.equalsIgnoreCase("LoginSUCCESS")) {
                Log.d(TAG, "HandleMessage: login success");

                setUpApp();
                switchToHome();
            } else {
                Log.d(TAG, "HandleMessage: login fail");
                stopService(new Intent(getApplicationContext(), WebSocketService.class));

                Toast.makeText(LoginActivity.this, "Login fail", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "HandleMessage: message is NOT a string(may be location object)");
        }

        return false;
    }

    /**
     * Register this receiver to get connect/ disconnect messages from web socket
     * Need to do relevant action according to the message, actions as below
     *  1. connect - send login query to server via web socket connections
     *  2. disconnect - logout user
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WebSocketService.WEB_SOCKET_CONNECTED)) {
                // send login request to server
                Log.d(TAG, "OnReceive: received broadcast message " + WebSocketService.WEB_SOCKET_CONNECTED);
                //QueryHandler.handleLogin(application);
            }
        }
    }

}

