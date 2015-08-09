package com.score.senz.listeners;

import com.score.senz.pojos.User;

import java.util.ArrayList;

/**
 * Listen for contact reader
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public interface ContactReaderListener {
    public void onPostReadContacts(ArrayList<User> contactList);
}
