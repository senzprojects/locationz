package com.score.senzors.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.db.SenzorsDbSource;
import com.score.senzors.pojos.User;
import com.score.senzors.R;
import com.score.senzors.utils.ActivityUtils;
import com.score.senzors.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Display friend list/ Fragment
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SharingList extends Fragment implements Handler.Callback {

    private static final String TAG = SharingList.class.getName();

    // use to populate list
    private SenzorApplication application;
    private ListView friendListView;
    private ArrayList<User> userList;
    private SharingListAdapter adapter;
    private ViewStub emptyView;

    private User unSharingUser;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // after creating fragment we initialize friend list
        application = (SenzorApplication) this.getActivity().getApplication();
        initEmptyView();
        initFriendList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.sharing_list_layout, null);
        initUi(root);

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        application.setCallback(this);

        // construct list adapter
        if(userList.size()>0) {
            adapter = new SharingListAdapter(SharingList.this, userList);
            friendListView.setAdapter(adapter);
        } else {
            friendListView.setEmptyView(emptyView);
        }
    }

    /**
     * Initialize UI components
     */
    private void initUi(View view) {
        friendListView = (ListView)view.findViewById(R.id.sharing_list);

        // add header and footer for list
        View headerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        friendListView.addHeaderView(headerView);
        friendListView.addFooterView(footerView);
    }

    /**
     * Initialize empty view for list view
     * empty view need to be display when no sensors available
     */
    private void initEmptyView() {
        Typeface typeface = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/vegur_2.otf");
        emptyView = (ViewStub) getActivity().findViewById(R.id.sharing_list_empty_view);
        View inflatedEmptyView = emptyView.inflate();
        TextView emptyText = (TextView) inflatedEmptyView.findViewById(R.id.empty_text);
        emptyText.setText("Sensor not shared with any user");
        emptyText.setTypeface(typeface);
    }

    /**
     * Create sensor list
     */
    private void initFriendList() {
        // populate sample data to list
        userList = new ArrayList<User>();

        if(application.getCurrentSensor().isMySensor()) {
            // add shared users if available
            if(application.getCurrentSensor().getSharedUsers() != null)
                userList = application.getCurrentSensor().getSharedUsers();
        } else {
            // add sensor owner
            userList.add(application.getCurrentSensor().getUser());
        }

        // construct list adapter
        if(userList.size()>0) {
            adapter = new SharingListAdapter(SharingList.this, userList);
            friendListView.setAdapter(adapter);
        } else {
            friendListView.setEmptyView(emptyView);
        }
    }

    /**
     * Remove user from sensor shared user list
     * Then we can refresh list view
     * @param removingUser user
     */
    private void removeUserFromList(User removingUser) {
        Iterator<User> it = userList.iterator();
        while (it.hasNext()) {
            User user = it.next();
            if (user.getPhoneNo().equals(removingUser.getPhoneNo())) {
                it.remove();
            }
        }
    }

    /**
     * UnShare user from sensor
     * Need to send un share query to server via web socket
     *
     * @param user user
     */
    public void unshare(User user) {
        unSharingUser = user;
        String query = ":SHARE" + " " + "#lat #lon" + " " + "@" + user.getPhoneNo().trim();
        Log.d(TAG, "UnShare: un-sharing query " + query);

        // validate share attribute first
        if(!user.getPhoneNo().equalsIgnoreCase("")) {
            if(NetworkUtil.isAvailableNetwork(this.getActivity())) {
                // construct query and send to server via web socket
                if(application.getWebSocketConnection().isConnected()) {
                    Log.w(TAG, "UnShare: sending query to server");
                    ActivityUtils.showProgressDialog(this.getActivity(), "Un-sharing sensor...");
                    application.getWebSocketConnection().sendTextMessage(query);
                } else {
                    Log.w(TAG, "UnShare: not connected to web socket");
                    Toast.makeText(this.getActivity(), "You are disconnected from senZors service", Toast.LENGTH_LONG).show();
                }

                ActivityUtils.hideSoftKeyboard(this.getActivity());
            } else {
                Log.w(TAG, "UnShare: no network connection");
                Toast.makeText(this.getActivity(), "Cannot connect to server, Please check your network connection", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "UnShare: empty username");
            Toast.makeText(this.getActivity(), "Make sure non empty username", Toast.LENGTH_LONG).show();
        }
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
            Log.d(TAG, "HandleMessage: message is a string " + payLoad);

            // successful login returns "ShareDone"
            if(payLoad.equalsIgnoreCase(":ShareDone")) {
                Log.d(TAG, "HandleMessage: un-sharing success");
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(this.getActivity(), "Sensor has been unshared successfully", Toast.LENGTH_LONG).show();

                // post un-share action differ according to sensor type(my sensor or friends sensor)
                if(application.getCurrentSensor().isMySensor()) {
                    // my sensor
                    // remove shared user from db
                    if(unSharingUser != null) {
                        new SenzorsDbSource(application.getApplicationContext()).deleteSharedUser(unSharingUser);

                        // get sensor list again
                        // refresh list
                        SharingList.this.removeUserFromList(unSharingUser);
                        SharingList.this.initFriendList();
                    }
                } else {
                    // friend sensor
                    // delete sensor from db and go back to sensor list
                    new SenzorsDbSource(application.getApplicationContext()).deleteSensorOfUser(application.getCurrentSensor());
                    application.initFriendsSensors();

                    // go back to previous activity
                    this.getActivity().finish();
                    this.getActivity().overridePendingTransition(R.anim.stay_in, R.anim.right_out);
                }

                return true;
            } else if (payLoad.equalsIgnoreCase(":ShareFailed")) {
                Log.d(TAG, "HandleMessage: sharing fail");
                ActivityUtils.cancelProgressDialog();
                Toast.makeText(this.getActivity(), "Un-sharing fail", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "HandleMessage: ignore message");
            }
        }

        return false;
    }
}
