package com.score.senz.ui;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.ISenzService;
import com.score.senz.R;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.enums.SenzTypeEnum;
import com.score.senz.exceptions.NoUserException;
import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.NetworkUtil;
import com.score.senz.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Display Contact list when sharing sensor
 *
 * @author eranga herath(erangeb@gmail.com)
 */
public class FriendListFragment extends android.support.v4.app.Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = SenzListFragment.class.getName();

    private ListView friendListView;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private FriendListAdapter friendListAdapter;
    private ArrayList<User> friendList = new ArrayList<>();

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

    // use to track share timeout
    private SenzCountDownTimer senzCountDownTimer;
    private boolean isResponseReceived;
    private User selectedUser;

    private Typeface typeface;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.list_layout, container, false);
        setHasOptionsMenu(true);

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

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");

        setActionBar("#Friend");
        initFriendListView();
        readFriends();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

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

        getActivity().unbindService(senzServiceConnection);
        getActivity().unregisterReceiver(senzMessageReceiver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        searchMenuItem = menu.add("Search");
        searchMenuItem.setIcon(android.R.drawable.ic_menu_search);
        searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchView = new FriendSearchView(getActivity());
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setIconifiedByDefault(true);
        searchMenuItem.setActionView(searchView);
    }


    /**
     * Set action bar title according to currently selected sensor type
     * Set custom font to title
     *
     * @param title action bar title
     */
    private void setActionBar(String title) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) (this.getActivity().findViewById(titleId));
        yourTextView.setTextColor(getResources().getColor(R.color.white));
        yourTextView.setTypeface(typeface);

        getActivity().getActionBar().setTitle(title);
    }

    /**
     * Read Friends data from DB
     */
    private void readFriends() {
        friendList = (ArrayList) new SenzorsDbSource(getActivity()).readAllUsers();
        friendListAdapter = new FriendListAdapter(this, friendList);
        friendListView.setAdapter(friendListAdapter);
    }

    /**
     * Initialize friend list view
     */
    private void initFriendListView() {
        friendListView = (ListView) getActivity().findViewById(R.id.list_view);

        // add header and footer for list
        View headerView = View.inflate(getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(getActivity(), R.layout.list_header, null);
        friendListView.addHeaderView(headerView);
        friendListView.addFooterView(footerView);
        friendListView.setTextFilterEnabled(false);

        // set up click listener
        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= friendList.size()) {
                    handelListItemClick((User) friendListAdapter.getItem(position - 1));
                }
            }
        });
    }

    /**
     * Navigate to share activity form here
     *
     * @param user user
     */
    private void handelListItemClick(User user) {
        selectedUser = user;

        // close search view if its visible
        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }

        if (NetworkUtil.isAvailableNetwork(getActivity())) {
            String confirmationMessage = "<font color=#000000>Are you sure you want to share senz with </font> <font color=#eada00>" + "<b>" + user.getUsername() + "</b>" + "</font>";
            displayDeleteMessageDialog(confirmationMessage, user);
        } else {
            Toast.makeText(getActivity(), "No network connection available", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        friendListAdapter.getFilter().filter(newText);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(searchView.getQuery())) {
            searchView.setQuery(null, true);
        }
        return true;
    }

    /**
     * Search view to search friend list
     */
    public static class FriendSearchView extends SearchView {
        public FriendSearchView(Context context) {
            super(context);
        }

        // The normal SearchView doesn't clear its search text when
        // collapsed, so we will do this for it.
        @Override
        public void onActionViewCollapsed() {
            setQuery("", false);
            super.onActionViewCollapsed();
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

        Log.d(TAG, "Message received with action " + action);
        if (action.equals("DATA")) {
            boolean isDone = intent.getExtras().getBoolean("extra");

            // response received
            ActivityUtils.cancelProgressDialog();
            isResponseReceived = true;
            senzCountDownTimer.cancel();

            // on successful share display notification message(Toast)
            if (isDone) {
                onPostShare();
            } else {
                String message = "<font color=#000000>Seems we couldn't share the senz with </font> <font color=#eada00>" + "<b>" + selectedUser.getUsername() + "</b>" + "</font>";
                displayInformationMessageDialog("#Share Fail", message);
            }
        }
    }


    /**
     * Share current sensor
     * Need to send share query to server via web socket
     */
    private void share(User user) {
        try {
            // create senz attributes
            HashMap<String, String> senzAttributes = new HashMap<>();
            senzAttributes.put("lat", "lat");
            senzAttributes.put("lon", "lon");
            senzAttributes.put("time", ((Long) (System.currentTimeMillis() / 1000)).toString());

            // new senz
            String id = "_ID";
            String signature = "";
            SenzTypeEnum senzType = SenzTypeEnum.SHARE;
            User sender = PreferenceUtils.getUser(getActivity());
            Senz senz = new Senz(id, signature, senzType, sender, user, senzAttributes);

            senzService.send(senz);
        } catch (NoUserException | RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear input fields and reset activity components
     */
    private void onPostShare() {
        Toast.makeText(getActivity(), "Successfully shared SenZ", Toast.LENGTH_LONG).show();
    }

    /**
     * Display message dialog when user request(click) to delete invoice
     *
     * @param message message to be display
     */
    public void displayDeleteMessageDialog(String message, final User user) {
        final Dialog dialog = new Dialog(getActivity());

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
        Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");
        messageHeaderTextView.setTypeface(face);
        messageTextView.setTypeface(face);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(face);
        okButton.setTypeface(null, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
                isResponseReceived = false;
                ActivityUtils.showProgressDialog(getActivity(), "Please wait...");
                senzCountDownTimer.start();
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
                share(selectedUser);
                Log.d(TAG, "Response not received yet");
            }
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "Finish time task");
            ActivityUtils.cancelProgressDialog();

            // display message dialog that we couldn't reach the user
            if (!isResponseReceived) {
                String message = "<font color=#000000>Seems we couldn't reach the user </font> <font color=#eada00>" + "<b>" + selectedUser.getUsername() + "</b>" + "</font> <font color=#000000> at this moment</font>";
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
