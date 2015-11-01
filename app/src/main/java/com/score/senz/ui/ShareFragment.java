package com.score.senz.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.ISenzService;
import com.score.senz.R;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.NetworkUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;
import com.score.senzc.pojos.User;

import java.util.HashMap;

/**
 * Activity class for sharing
 * Implement sharing related functions
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class ShareFragment extends android.support.v4.app.Fragment {

    private static final String TAG = ShareFragment.class.getName();

    private TextView usernameLabel;
    private EditText usernameEditText;

    // use to track share timeout
    private SenzCountDownTimer senzCountDownTimer;
    private boolean isResponseReceived;

    // custom font
    private Typeface typeface;

    // service interface
    private ISenzService senzService = null;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("TAG", "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            senzService = null;
            Log.d("TAG", "Disconnected from senz service");
        }
    };


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.share_layout, container, false);

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        senzCountDownTimer = new SenzCountDownTimer(16000, 5000);
        isResponseReceived = false;
        initUi();
    }

    /**
     * {@inheritDoc}
     */
    public void onStart() {
        super.onResume();

        // bind with senz service
        // bind to service from here as well
        Intent intent = new Intent();
        intent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
        getActivity().bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);

        getActivity().registerReceiver(senzMessageReceiver, new IntentFilter("DATA"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();

        // Unbind from the service
        getActivity().unbindService(senzServiceConnection);

        getActivity().unregisterReceiver(senzMessageReceiver);
    }

    /**
     * Initialize UI components
     */
    private void initUi() {
        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");

        usernameLabel = (TextView) getActivity().findViewById(R.id.share_layout_phone_no_label);
        usernameEditText = (EditText) getActivity().findViewById(R.id.share_layout_phone_no);

        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("#Share");

        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (getActivity().findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typeface);
        usernameLabel.setTypeface(typeface);
        usernameEditText.setTypeface(typeface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_menu, menu);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_done:
                if (usernameEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), "Empty username", Toast.LENGTH_LONG).show();
                } else {
                    if (NetworkUtil.isAvailableNetwork(getActivity())) {
                        ActivityUtils.showProgressDialog(getActivity(), "Please wait...");
                        senzCountDownTimer.start();
                    } else {
                        Toast.makeText(getActivity(), "No network connection available", Toast.LENGTH_LONG).show();
                    }
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Share current sensor
     * Need to send share query to server via web socket
     */
    private void share() {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("lat", "lat");
            senzAttributes.put("lon", "lon");
            senzAttributes.put("msg", "msg");
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            String id = "_ID";
            String signature = "_SIGNATURE";
            SenzTypeEnum senzType = SenzTypeEnum.SHARE;
            User receiver = new User("", usernameEditText.getText().toString().trim());
            Senz senz = new Senz(id, signature, senzType, null, receiver, senzAttributes);

            senzService.send(senz);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear input fields and reset activity components
     */
    private void onPostShare() {
        usernameEditText.setText("");
        Toast.makeText(getActivity(), "Successfully shared SenZ", Toast.LENGTH_LONG).show();
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

        if (action.equalsIgnoreCase("DATA")) {
            Senz senz = intent.getExtras().getParcelable("SENZ");

            if (senz.getAttributes().containsKey("msg")) {
                // msg response received
                ActivityUtils.cancelProgressDialog();
                isResponseReceived = true;
                senzCountDownTimer.cancel();

                String msg = senz.getAttributes().get("msg");
                if (msg != null && msg.equalsIgnoreCase("ShareDone")) {
                    onPostShare();
                } else {
                    String user = usernameEditText.getText().toString().trim();
                    String message = "<font color=#000000>Seems we couldn't share the senz with </font> <font color=#eada00>" + "<b>" + user + "</b>" + "</font>";
                    displayInformationMessageDialog("#Share Fail", message);
                }
            }
        }
    }

    /**
     * Keep track with share response timeout
     */
    private class SenzCountDownTimer extends CountDownTimer {

        public SenzCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // if response not received yet, resend share
            if (!isResponseReceived) {
                share();
                Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            ActivityUtils.hideSoftKeyboard(getActivity());
            ActivityUtils.cancelProgressDialog();

            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String user = usernameEditText.getText().toString().trim();
                String message = "<font color=#000000>Seems we couldn't reach the user </font> <font color=#eada00>" + "<b>" + user + "</b>" + "</font> <font color=#000000> at this moment</font>";
                displayInformationMessageDialog("#Share Fail", message);
            }
        }
    }

    /**
     * Display message dialog when user request(click) to delete invoice
     *
     * @param message message to be display
     */
    public void displayInformationMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(getActivity());

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

}
