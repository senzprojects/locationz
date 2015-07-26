package com.score.senzors.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;
import com.score.senzors.services.WebSocketService;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;
import com.score.senzors.utils.PreferenceUtils;
import com.score.senzors.utils.QueryHandler;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Splash activity, send login query from here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SplashActivity extends Activity implements Handler.Callback {
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final String TAG = SplashActivity.class.getName();

    private SenzorApplication application;
    private User loginUser;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);

        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        TextView appName = (TextView) findViewById(R.id.splash_text);
        appName.setTypeface(typefaceThin, Typeface.BOLD);

        application = (SenzorApplication) getApplication();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
        application.setCallback(this);

        initNavigation();
    }

    /**
     * Determine where to go from here
     */
    private void initNavigation() {
        // decide where to go
        // 1. goto registration
        // 2. send login query
        try {
            loginUser = PreferenceUtils.getUser(this);
            login();
        } catch (NoUserException e) {
            e.printStackTrace();

            // no user means navigate to login
            navigateToLogin();
        }
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    /**
     * Connect to web socket
     */
    private void login() {
        if(NetworkUtil.isAvailableNetwork(SplashActivity.this)) {
            ActivityUtils.hideSoftKeyboard(this);

            Intent serviceIntent = new Intent(SplashActivity.this, WebSocketService.class);
            startService(serviceIntent);
        } else {
            Toast.makeText(this, "No network connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void switchToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        this.startActivity(intent);

        SplashActivity.this.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        // we handle string messages only from here
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            if (payLoad.equalsIgnoreCase("SERVER_KEY_EXTRACTION_SUCCESS")) {
                Log.d(TAG, "HandleMessage: server key extracted");

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
                application.setCallback(null);
                application.setUpSenzors();
                switchToHome();
            } else {
                Log.d(TAG, "HandleMessage: login fail");
                stopService(new Intent(getApplicationContext(), WebSocketService.class));

                Toast.makeText(SplashActivity.this, "Login fail", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "HandleMessage: message is NOT a string(may be location object)");
        }

        return false;
    }
}