package com.score.senzors.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.R;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.pojos.User;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;

/**
 * Activity class for sharing
 * Implement sharing related functions
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class ShareActivity extends Activity implements Handler.Callback {

    private static final String TAG = ShareActivity.class.getName();

    private SenzorApplication application;
    private Sensor sharingSensor;
    private User sharingUser;

    private TextView phoneNoLabel;
    private EditText phoneNoEditText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_layout);
        application = (SenzorApplication) getApplication();

        initSharingData();
        initUi();
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();

        application.setCallback(this);
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
        phoneNoEditText.setText(sharingUser.getPhoneNo());

        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Share @" + sharingUser.getUsername().toLowerCase());

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
     *      1. Sensor
     *      2. User
     * extract it and initialize the thisInvoice
     */
    private void initSharingData() {
        Bundle bundle = getIntent().getExtras();
        sharingSensor = bundle.getParcelable("com.score.senzors.pojos.Sensor");
        sharingUser = bundle.getParcelable("com.score.senzors.pojos.User");
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

        // validate share attribute first
        if(!sharingUser.getPhoneNo().equalsIgnoreCase("")) {
            // check weather sensor already shared with given user
            if(application.getCurrentSensor().getSharedUsers().contains(sharingUser)) {
                // already shared sensor
                Toast.makeText(ShareActivity.this, "Sensor already shared with " + sharingUser.getUsername(), Toast.LENGTH_LONG).show();
            } else {
                if(NetworkUtil.isAvailableNetwork(ShareActivity.this)) {
                    // construct query and send to server via web socket
                    if(application.getWebSocketConnection().isConnected()) {
                        ActivityUtils.showProgressDialog(this, "Sharing sensor...");
                        application.getWebSocketConnection().sendTextMessage(query);
                    } else {
                        Log.w(TAG, "Share: not connected to web socket");
                        Toast.makeText(ShareActivity.this, "You are disconnected from senZors service", Toast.LENGTH_LONG).show();
                    }

                    ActivityUtils.hideSoftKeyboard(this);
                } else {
                    Toast.makeText(ShareActivity.this, "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(ShareActivity.this, "Make sure non empty username", Toast.LENGTH_LONG).show();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handleMessage(Message message) {
        // we handle string messages only from here
        Log.d(TAG, "HandleMessage: message from server");
        if(message.obj instanceof String) {
            String payLoad = (String)message.obj;
            ActivityUtils.cancelProgressDialog();

            // successful sharing returns "ShareDone"
            if(payLoad.equalsIgnoreCase("ShareDone")) {
                Log.d(TAG, "HandleMessage: sharing success");
                Toast.makeText(ShareActivity.this, "Sensor has been shared successfully", Toast.LENGTH_LONG).show();

                // create sharing user, if user not in the db
                // create shared connection(sharedUser) in db
                // refresh sensor list
                SenzorsDbSource dbSource = new SenzorsDbSource(ShareActivity.this);
                User user = dbSource.getOrCreateUser(sharingUser.getPhoneNo());
                dbSource.addSharedUser(application.getCurrentSensor(), user);
                application.getCurrentSensor().getSharedUsers().add(user);

                Log.d(TAG, "HandleMessage: user get/created " + user.getUsername());
                Log.d(TAG, "HandleMessage: added shared connection");

                ShareActivity.this.finish();
                ShareActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);

                return true;
            } else {
                Log.d(TAG, "HandleMessage: sharing fail");
                Toast.makeText(ShareActivity.this, "Sharing fail", Toast.LENGTH_LONG).show();
            }
        }

        return false;
    }
}
