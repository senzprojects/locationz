package com.score.senzors.listeners;

import com.score.senzors.pojos.User;

import java.util.ArrayList;

/**
 * Listen for contact reader
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public interface ContactReaderListener {
    public void onPostReadContacts(ArrayList<User> contactList);
}
