package com.score.senzors.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.Country;
import com.score.senzors.utils.ActivityUtils;

import java.util.ArrayList;

/**
 * Activity class to display countries and Country codes
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class CountryListActivity extends Activity implements SearchView.OnQueryTextListener {
    private SenzorApplication application;
    private ListView countryListView;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    private CountryListAdapter countryListAdapter;
    private ArrayList<Country> countryList;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        application = (SenzorApplication) this.getApplication();

        setActionBar();
        initCountryList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CountryListActivity.this.finish();
                CountryListActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set action bar
     *      1. properties
     *      2. title with custom font
     */
    private void setActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Select your country");

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (this.findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typeface);
    }

    /**
     * Initialize country list
     */
    private void initCountryList() {
        countryList = new ArrayList<Country>();
        countryListView = (ListView) findViewById(R.id.list_view);
        countryListAdapter = new CountryListAdapter(this, countryList);

        // add header and footer for list
        View headerView = View.inflate(this, R.layout.list_header, null);
        View footerView = View.inflate(this, R.layout.list_header, null);
        countryListView.addHeaderView(headerView);
        countryListView.addFooterView(footerView);
        countryListView.setAdapter(countryListAdapter);
        countryListView.setTextFilterEnabled(false);

        // set up click listener
        countryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position>0 && position <= countryList.size()) {
                    handelListItemClick((Country)countryListAdapter.getItem(position - 1));
                }
            }
        });
    }

    /**
     * Navigate to register activity form here
     * @param country country
     */
    private void handelListItemClick(Country country) {
        // close search view if its visible
        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }

        // pass selected user and sensor to share activity
        Intent intent = new Intent(this, ShareActivity.class);
        //intent.putExtra("com.score.senzors.pojos.User", user);
        //intent.putExtra("com.score.senzors.pojos.Sensor", application.getCurrentSensor());
        this.startActivity(intent);
        this.overridePendingTransition(R.anim.right_in, R.anim.stay_in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
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
        countryListAdapter.getFilter().filter(newText);

        return true;
    }

}
