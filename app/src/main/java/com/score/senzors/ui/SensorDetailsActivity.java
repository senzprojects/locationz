package com.score.senzors.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.utils.ActivityUtils;

/**
 * Activity class for displaying sensor details
 * Implement tabs with swipe view
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorDetailsActivity extends FragmentActivity {
    ActionBar actionBar;
    ViewPager viewPager;
    TabPagerAdapter tabPagerAdapter;

    // activity interact with this Sensor
    Sensor thisSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_details_layout);

        initThisSensor();
        setUpActionBar();
        setUpViewPager();
        setUpTabListener();
    }

    /**
     * Sensor coming through bundle,
     * extract it and initialize the thisInvoice
     */
    private void initThisSensor() {
        Bundle bundle = getIntent().getExtras();
        thisSensor = bundle.getParcelable("com.score.senzors.pojos.Sensor");
    }

    /**
     * Set action bar title and font
     */
    private void setUpActionBar() {
        actionBar = getActionBar();
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (this.findViewById(titleId));

        Typeface typefaceThin = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typefaceThin);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("#Location @" + thisSensor.getUser().getUsername());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    /**
     * Set view pager components
     */
    private void setUpViewPager() {
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });
        viewPager.setAdapter(tabPagerAdapter);
    }

    /**
     * Set actionbar tab listener
     * Set custom view for tab
     */
    private void setUpTabListener() {
        // tab listener
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
            }
        };

        // custom view for tab
        TextView locationTabTextView = ActivityUtils.getCustomTextView(this, "Location");
        TextView sharingTabTextView = ActivityUtils.getCustomTextView(this, thisSensor.isMySensor() ? "Shared with" : "Shared by");
        actionBar.addTab(actionBar.newTab().setCustomView(locationTabTextView).setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setCustomView(sharingTabTextView).setTabListener(tabListener));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);

        // show hide share item according to sensor type
        MenuItem share = menu.findItem(R.id.action_share);
        if (thisSensor.isMySensor()) {
            share.setVisible(true);
        } else {
            share.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // navigate to home with effective navigation
                NavUtils.navigateUpFromSameTask(this);
                this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
            case R.id.action_share:
                Intent intent = new Intent(this, FriendListActivity.class);
                intent.putExtra("com.score.senzors.pojos.Sensor", thisSensor);
                this.startActivity(intent);
                this.overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.right_out);
    }

    /**
     * Tab adapter to implement swipe view
     * Pass data to fragment on selection
     */
    public class TabPagerAdapter extends FragmentStatePagerAdapter {
        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new SensorMapFragment();
                case 1:
                    return new SharingList();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }

}
