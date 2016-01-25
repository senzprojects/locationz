package com.score.locationz.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.score.locationz.R;

/**
 * Splash activity, send login query from here
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final String TAG = SplashActivity.class.getName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);

        initUi();
        initNavigation();
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        TextView appName = (TextView) findViewById(R.id.splash_text);
        appName.setTypeface(typefaceThin, Typeface.BOLD);
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
    }

    /**
     * Determine where to go from here
     */
    private void initNavigation() {
        PackageManager packageManager = this.getPackageManager();
        try {
            Log.i("MyApp", "package already installed! Awesome!");
            packageManager.getPackageInfo("com.score.senz", PackageManager.GET_ACTIVITIES);

            navigateToHome();
        } catch (PackageManager.NameNotFoundException e) {
            //App is not installed, launch Google Play Store
            displayConfirmDialog("In order to work with the application you have to install and register on SenZ service first" +
                    "Are you sure you want to install the SenZ service");
        }
    }

    /**
     * Switch to home activity
     * This method will be call after successful login
     */
    private void navigateToHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    /**
     * Display message dialog when user request(click) to delete invoice
     *
     * @param message message to be display
     */
    public void displayConfirmDialog(String message) {
        final Dialog dialog = new Dialog(this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_confirm_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText("#Share");
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        messageHeaderTextView.setTypeface(face);
        messageTextView.setTypeface(face);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(face);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.score.senz"));
                startActivity(intent);
            }
        });

        // cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(face);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }
}