package com.score.senzors.ui;

import android.app.Activity;
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
import com.score.senzors.exceptions.*;
import com.score.senzors.pojos.User;
import com.score.senzors.utils.*;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Activity class that handles user registrations
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class RegistrationActivity extends Activity implements View.OnClickListener, Handler.Callback {

    private static final String TAG = RegistrationActivity.class.getName();
    private final WebSocketConnection webSocketConnection = new WebSocketConnection();
    private SenzorApplication application;

    // UI fields
    private EditText editTextPhoneNo;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private TextView countryCodeText;
    private TextView textViewHeaderText;
    private TextView textViewSignUpText;
    private RelativeLayout signUpButton;

    // registration deal with User object
    User registeringUser;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        application = (SenzorApplication) this.getApplication();
        application.setCallback(this);

        initUi();
    }

    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        editTextPhoneNo = (EditText) findViewById(R.id.registration_phone_no);
        editTextPassword = (EditText) findViewById(R.id.registration_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.registration_confirm_password);
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
        editTextPassword.setTypeface(typefaceThin, Typeface.NORMAL);
        editTextConfirmPassword.setTypeface(typefaceThin, Typeface.NORMAL);
    }

    /**
     * Initialize user object
     */
    private void initRegisteringUser() {
        String countryCode = countryCodeText.getText().toString().trim();
        String phoneNo = editTextPhoneNo.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String internationalPhoneNo = countryCode + phoneNo.substring(phoneNo.length() - 9);

        registeringUser = new User("0", internationalPhoneNo, password);
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
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        try {
            ActivityUtils.isValidRegistrationFields(registeringUser, confirmPassword);

            ActivityUtils.hideSoftKeyboard(this);
            ActivityUtils.showProgressDialog(this, "Registering...");
            registerUser();
        } catch (InvalidInputFieldsException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Invalid input fields", Toast.LENGTH_LONG).show();
        } catch (MismatchPasswordException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Mismatching passwords", Toast.LENGTH_LONG).show();
        } catch (InvalidPhoneNoException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Phone no should contains 10 digits", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create user via sending PUT query to server,
     * need to send the query via the web socket
     */
    private void registerUser() {
        try {
            webSocketConnection.connect(SenzorApplication.WEB_SOCKET_URI, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    // send put query
                    Log.d(TAG, "Web socket open");
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(TAG, "Got web socket message: " + payload);

                    // handle message according to message content
                    // Need to check
                    //      1. message contains "pubkey" of server
                    //      2. message contains registration status
                    QueryHandler.handleQuery(application, payload);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Web socket connection lost");
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String) message.obj;
            Log.d(TAG, "HandleMessage: message is a string - " + payLoad);

            if (payLoad.equalsIgnoreCase("SERVER_KEY_EXTRACTION_SUCCESS")) {
                // server key extraction success
                // so send PUT query to create user
                try {
                    String countryCode = countryCodeText.getText().toString().trim();
                    String phone = countryCode + registeringUser.getPhoneNo().substring(registeringUser.getPhoneNo().length() - 9);
                    registeringUser.setPhoneNo(phone);
                    String putQuery = QueryHandler.getRegistrationQuery(registeringUser);
                    Log.d(TAG, "------put query------");
                    Log.d(TAG, putQuery);

                    if(webSocketConnection.isConnected()) {
                        webSocketConnection.sendTextMessage(putQuery);
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                }
            } else if(payLoad.equalsIgnoreCase("SERVER_KEY_EXTRACTION_FAIL")) {
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(RegistrationActivity.this, "Registration fail", Toast.LENGTH_LONG).show();
            } else if(payLoad.equalsIgnoreCase("UserCreated")) {
                // stop service
                // save user in shared preference
                // move to login
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(RegistrationActivity.this, "Successfully registered", Toast.LENGTH_LONG).show();

                // create user from here
                // we don't store passwords in DB, so when save user in shared preference need to set password
                // save my sensors in db
                User user = new SenzorsDbSource(this).getOrCreateUser(registeringUser.getPhoneNo());
                user.setPassword(registeringUser.getPassword());
                PreferenceUtils.saveUser(this, user);
                SenzUtils.addMySensorsToDb(this, user);

                // disconnect at the end
                webSocketConnection.disconnect();
                application.setCallback(null);
                this.finish();
                this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
            } else if (payLoad.equalsIgnoreCase("UserCreationFailed")) {
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                webSocketConnection.disconnect();
            }
        }

        return false;
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