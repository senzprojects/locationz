package com.score.senz.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.score.senz.R;
import com.score.senz.listeners.ContactReaderListener;
import com.score.senz.pojos.User;
import com.score.senz.services.ContactReader;
import com.score.senz.utils.ActivityUtils;

import java.util.ArrayList;

/**
 * Display Contact list when sharing sensor
 *
 * @author eranga herath(erangeb@gmail.com)
 */
public class FriendList extends android.support.v4.app.Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, ContactReaderListener {

    private ListView friendListView;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private FriendListAdapter friendListAdapter;
    private ArrayList<User> friendList = new ArrayList<>();

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

        setActionBar("#Friends");
        initFriendListView();
        readContacts();
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();
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
     * Read contact from contact DB
     */
    private void readContacts() {
        // read contact list in background
        ActivityUtils.showProgressDialog(getActivity(), "Searching friends...");
        new ContactReader(this).execute("READ");
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
        // close search view if its visible
        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }

        // launch share activity
//        Intent intent = new Intent(getActivity(), ShareActivity.class);
//        intent.putExtra("extra", user.getPhoneNo());
//        getActivity().startActivity(intent);
//        getActivity().overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);
        showShareConfirmDialog(user);
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
     * Trigger contact reader task finish
     *
     * @param contactList user list
     */
    @Override
    public void onPostReadContacts(ArrayList<User> contactList) {
        ActivityUtils.cancelProgressDialog();

        friendList = contactList;
        friendListAdapter = new FriendListAdapter(this, friendList);
        friendListView.setAdapter(friendListAdapter);
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

    private void showShareConfirmDialog(User user) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder
                .setMessage("Are you sure you want to share senz with '" + user.getUsername() + "'")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // share senz to server
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
