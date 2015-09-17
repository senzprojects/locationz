package com.score.senz.ui;

import android.content.Context;
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
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();

        setActionBar("#Share");
        readContacts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.search_menu, menu);

//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        searchMenuItem = menu.findItem(R.id.search);
//        searchView = (SearchView) searchMenuItem.getActionView();
//
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setSubmitButtonEnabled(true);
//        searchView.setOnQueryTextListener(this);

        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchView = new MySearchView(getActivity());
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setIconifiedByDefault(true);
        item.setActionView(searchView);
    }

    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(searchView.getQuery())) {
            searchView.setQuery(null, true);
        }
        return true;
    }

    public static class MySearchView extends SearchView {
        public MySearchView(Context context) {
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
     * Initialize friend list
     */
    private void initFriendList() {
        friendListView = (ListView) getActivity().findViewById(R.id.list_view);
        friendListAdapter = new FriendListAdapter(this, friendList);

        // add header and footer for list
        View headerView = View.inflate(getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(getActivity(), R.layout.list_header, null);
        friendListView.addHeaderView(headerView);
        friendListView.addFooterView(footerView);
        friendListView.setAdapter(friendListAdapter);
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

        // pass selected user and sensor to share activity
        //Intent intent = new Intent(this, ShareActivity.class);
        //intent.putExtra("com.score.senz.pojos.User", user);
        //intent.putExtra("com.score.senz.pojos.Sensor", application.getCurrentSensor());
        //this.startActivity(intent);
        //this.overridePendingTransition(R.anim.right_in, R.anim.stay_in);
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
     * Trigger contact reader task finish
     *
     * @param contactList user list
     */
    @Override
    public void onPostReadContacts(ArrayList<User> contactList) {
        ActivityUtils.cancelProgressDialog();

        this.friendList = contactList;
        initFriendList();
    }
}
