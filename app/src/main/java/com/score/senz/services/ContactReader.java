package com.score.senz.services;

import android.os.AsyncTask;
import com.score.senz.listeners.ContactReaderListener;
import com.score.senz.pojos.User;
import com.score.senz.ui.FriendList;
import com.score.senz.utils.PhoneBookUtils;

import java.util.ArrayList;

/**
 * Read contact from contact database
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class ContactReader extends AsyncTask<String, String, String > {

    FriendList activity;
    ContactReaderListener listener;
    ArrayList<User> contactList;

    public ContactReader(FriendList activity) {
        this.activity = activity;
        this.listener = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doInBackground(String... params) {
        contactList = PhoneBookUtils.readContacts(activity.getActivity());

        return "READ";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(String status) {
        super.onPostExecute(status);

        listener.onPostReadContacts(this.contactList);
    }

}
