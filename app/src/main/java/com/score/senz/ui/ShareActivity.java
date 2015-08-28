package com.score.senz.ui;

import android.app.ActionBar;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.R;
import com.score.senz.application.SenzApplication;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.pojos.Sensor;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.services.SenzService;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.NetworkUtil;
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
 * Activity class for sharing
 * Implement sharing related functions
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class ShareActivity extends Activity {

    private static final String TAG = ShareActivity.class.getName();

    private SenzApplication application;

    private TextView phoneNoLabel;
    private EditText phoneNoEditText;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);
        application = (SenzApplication) getApplication();

        initSharingData();
        initUi();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();

        bindService(new Intent(ShareActivity.this, SenzService.class), senzServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();

        application.setCallback(null);
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");

        phoneNoLabel = (TextView) findViewById(R.id.share_layout_phone_no_label);
        phoneNoEditText = (EditText) findViewById(R.id.share_layout_phone_no);

        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Share @");

        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (this.findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typefaceThin);
        phoneNoLabel.setTypeface(typefaceThin);
        phoneNoEditText.setTypeface(typefaceThin);
    }

    /**
     * Initialize
     * 1. Sensor
     * 2. User
     * extract it and initialize the thisInvoice
     */
    private void initSharingData() {
        Bundle bundle = getIntent().getExtras();
        //sharingSensor = bundle.getParcelable("com.score.senz.pojos.Sensor");
        //sharingUser = bundle.getParcelable("com.score.senz.pojos.User");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ShareActivity.this.finish();
                ShareActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
            case R.id.action_share_done:
                share();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Share current sensor
     * Need to send share query to server via web socket
     */
    private void share() {
        String query = "SHARE" + " " + "#lat #lon" + " " + "@" + phoneNoEditText.getText().toString().trim();
        Log.d(TAG, "Share: sharing query " + query);

        try {
            // create key pair
            PrivateKey privateKey = RSAUtils.getPrivateKey(this);

            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("lat", "lat");
            senzAttributes.put("lon", "lon");
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            Senz senz = new Senz();
            senz.setSenzType(SenzTypeEnum.SHARE);
            senz.setReceiver(phoneNoEditText.getText().toString().trim());
            senz.setSender(PreferenceUtils.getUser(this).getPhoneNo());
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoUserException e) {
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShareActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }

}
