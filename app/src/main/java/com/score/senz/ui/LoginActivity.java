package com.score.senz.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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
import com.score.senz.exceptions.NoUserException;
import com.score.senz.pojos.User;
import com.score.senz.services.SenzService;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.NetworkUtil;
import com.score.senz.utils.PhoneBookUtils;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.SenzUtils;

/**
 * Activity class for login
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    public static final String LOGIN_ACTIVITY_MESSENGER = "LOGIN_ACTIVITY_MESSENGER";
    private static final String TAG = LoginActivity.class.getName();

    // keep user object to use in this activity
    User loginUser;

    // use to send senz messages to SenzService
    Messenger senzServiceMessenger;

    // use to receive messages from the service
    Messenger activityMessenger;

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

        // init activity messenger
        // send activity message handler to service in order to send messages to this activity
        activityMessenger = new Messenger(new SenZMessageHandler());
        Intent serviceIntent = new Intent(LoginActivity.this, SenzService.class);
        serviceIntent.putExtra(LOGIN_ACTIVITY_MESSENGER, activityMessenger);
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
            msg.obj = "Hi service..";
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
            ((SenzApplication) getApplication()).setUpSenzors();
        }
    }

    /**
     * Handle incoming senz messages from SenzService
     */
    private class SenZMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            System.out.println("message from service :" + str);
        }
    }

}

