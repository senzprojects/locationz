package com.score.senz.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.ISenzService;
import com.score.senz.R;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.InvalidInputFieldsException;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.services.RemoteSenzService;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.NetworkUtil;
import com.score.senz.utils.PreferenceUtils;
import com.score.senz.utils.RSAUtils;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
    private EditText editTextUsername;
    private RelativeLayout signUpButton;
    private Typeface typeface;

    // service interface
    private ISenzService senzService = null;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
            doRegistration();
        }

        public void onServiceDisconnected(ComponentName className) {
            senzService = null;
            Log.d("TAG", "Disconnected from senz service");
        }
    };

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        initUi();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(senzServiceConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(senzMessageReceiver, new IntentFilter("DATA"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(senzMessageReceiver);
    }

    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        editTextUsername = (EditText) findViewById(R.id.registration_phone_no);
        signUpButton = (RelativeLayout) findViewById(R.id.registration_sign_up_button);
        signUpButton.setOnClickListener(RegistrationActivity.this);

        editTextUsername.setTypeface(typeface, Typeface.NORMAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v == signUpButton) {
            if (NetworkUtil.isAvailableNetwork(this)) {
                onClickRegister();
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Sign-up button action,
     * Need to connect web socket and send PUT query to register
     * the user
     */
    private void onClickRegister() {
        ActivityUtils.hideSoftKeyboard(this);

        try {
            ActivityUtils.isValidRegistrationFields(registeringUser);
            String confirmationMessage = "<font color=#000000>Are you sure you want to register on SenZ with </font> <font color=#ffc027>" + "<b>" + registeringUser.getUsername() + "</b>" + "</font>";
            displayConfirmationMessageDialog(confirmationMessage);
        } catch (InvalidInputFieldsException e) {
            Toast.makeText(this, "Invalid username", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Create user
     * First initialize key pair
     * start service
     * bind service
     */
    private void doPreRegistration() {
        try {
            // crate user
            String username = editTextUsername.getText().toString().trim();
            registeringUser = new User("0", username);

            // init keys
            RSAUtils.initKeys(this);

            // start service from here
            Intent serviceIntent = new Intent(RegistrationActivity.this, RemoteSenzService.class);
            startService(serviceIntent);

            // bind to service from here as well
            Intent intent = new Intent();
            intent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
            bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create register senz
     * Send register senz to senz service via service binder
     */
    private void doRegistration() {
        try {
            // first create create senz
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());
            senzAttributes.put("pubkey", PreferenceUtils.getRsaKey(this, RSAUtils.PUBLIC_KEY));

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.SHARE);
            senz.setReceiver(new User("", "mysensors"));
            senz.setSender(new User("", registeringUser.getUsername()));
            senz.setAttributes(senzAttributes);

            senzService.sendSenz(registeringUser);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver senzMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got message from Senz service");
            handleMessage(intent);
        }
    };

    /**
     * Handle broadcast message receives
     * Need to handle registration success failure here
     *
     * @param intent intent
     */
    private void handleMessage(Intent intent) {
        String action = intent.getAction();

        if (action.equals("DATA")) {
            boolean senzMessage = intent.getExtras().getBoolean("extra");
            if (senzMessage) {
                // save user
                // navigate home
                PreferenceUtils.saveUser(this, registeringUser);

                ActivityUtils.cancelProgressDialog();
                Toast.makeText(this, "Successfully registered", Toast.LENGTH_LONG).show();

                navigateToHome();
            } else {
                ActivityUtils.cancelProgressDialog();

                String informationMessage = "<font color=#4a4a4a>Seems username </font> <font color=#eada00>" + "<b>" + registeringUser.getUsername() + "</b>" + "</font> <font color=#4a4a4a> already obtained by some other user, try SenZ with different username</font>";
                displayInformationMessageDialog("Registration fail", informationMessage);
            }
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToHome() {
        Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
        RegistrationActivity.this.startActivity(intent);
        RegistrationActivity.this.finish();
    }

    /**
     * Display message dialog when user request(click) to register
     *
     * @param message message to be display
     */
    public void displayConfirmationMessageDialog(String message) {
        final Dialog dialog = new Dialog(RegistrationActivity.this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_confirm_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText("Confirm username");
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
                ActivityUtils.showProgressDialog(RegistrationActivity.this, "Please wait...");
                doPreRegistration();
            }
        });

        // cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(typeface);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    /**
     * Display message dialog with registration status
     *
     * @param message message to be display
     */
    public void displayInformationMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(RegistrationActivity.this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.information_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(title);
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }
}