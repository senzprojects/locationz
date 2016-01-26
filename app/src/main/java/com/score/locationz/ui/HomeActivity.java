package com.score.locationz.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.score.locationz.R;
import com.score.locationz.exceptions.NoUserException;
import com.score.locationz.utils.ActivityUtils;
import com.score.locationz.utils.PreferenceUtils;
import com.score.senz.ISenzService;
import com.score.senzc.pojos.DrawerItem;
import com.score.senzc.pojos.User;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Main activity class of MY.sensors
 * Implement navigation drawer here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class HomeActivity extends FragmentActivity {

    private static final String TAG = HomeActivity.class.getName();

    // Ui components
    private ListView drawerListView;
    private DrawerLayout drawerLayout;
    private RelativeLayout drawerContainer;
    private HomeActionBarDrawerToggle homeActionBarDrawerToggle;

    // drawer components
    private ArrayList<DrawerItem> drawerItemList;
    private DrawerAdapter drawerAdapter;

    // type face
    private Typeface typeface;

    // user components
    private CircularImageView userImage;
    private TextView username;
    private HomeActivity curActivity;

    // service interface
    private ISenzService senzService = null;

    // service status
    boolean isServiceBound = false;

    // service connection
    private ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
            try {
                String user = senzService.getUser();
                PreferenceUtils.saveUser(HomeActivity.this, new User("id", user));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            initDrawerUser();

            // unbind after config
            if (isServiceBound) {
                unbindService(senzServiceConnection);
                isServiceBound = false;
            }
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
        setContentView(R.layout.home_layout);

        initConfig();
        initDrawer();
        initDrawerUser();
        initDrawerList();
        loadSensors();
        curActivity = this;
    }

    /**
     * {@inheritDoc}
     */
    protected void onResume() {
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind from the service
        if (isServiceBound) {
            unbindService(senzServiceConnection);
            isServiceBound = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (homeActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initConfig() {
        try {
            PreferenceUtils.getUser(this);
        } catch (NoUserException e) {
            e.printStackTrace();

            // get user from service
            // bind to senz service
            if (!isServiceBound) {
                Intent intent = new Intent();
                intent.setClassName("com.score.senz", "com.score.senz.services.RemoteSenzService");
                bindService(intent, senzServiceConnection, BIND_AUTO_CREATE);
                isServiceBound = true;
            }
        }
    }

    /**
     * Initialize Drawer UI components
     */
    private void initDrawer() {
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerContainer = (RelativeLayout) findViewById(R.id.drawer_container);

        // set a custom shadow that overlays the senz_map_layout content when the drawer opens
        // set up drawer listener
        //drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        homeActionBarDrawerToggle = new HomeActionBarDrawerToggle(this, drawerLayout);
        drawerLayout.setDrawerListener(homeActionBarDrawerToggle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1888 && resultCode == -1) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            userImage.setImageBitmap(photo);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytephoto = byteArrayOutputStream.toByteArray();
            String encodeddata = Base64.encodeToString(bytephoto, Base64.DEFAULT);

            // save image in shared preference
            PreferenceUtils.saveUserImage(this, encodeddata);
        }
    }

    private void initDrawerUser() {
        userImage = (CircularImageView) findViewById(R.id.contact_image);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_icon);
        String encodedImage = PreferenceUtils.getUserImage(this);

        if (!encodedImage.isEmpty()) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            userImage.setImageBitmap(decodedByte);
        } else {
            userImage.setImageBitmap(largeIcon);
        }

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_LONG).show();
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(camera, 1888);
            }
        });

        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        //typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Black.ttf");

        try {
            User user = PreferenceUtils.getUser(this);
            username = (TextView) findViewById(R.id.home_user_text);
            username.setText("@" + user.getUsername());
            username.setTextColor(Color.parseColor("#eada00"));
            //username.setTextColor(Color.parseColor("#4a4a4a"));
            username.setTypeface(typeface, Typeface.BOLD);
        } catch (NoUserException e) {
            e.printStackTrace();
        }

    }

    /**
     * Initialize Drawer list
     */
    private void initDrawerList() {
        // initialize drawer content
        // need to determine selected item according to the currently selected sensor type
        drawerItemList = new ArrayList();
        drawerItemList.add(new DrawerItem("#LocationZ", R.drawable.my_sensz_normal, R.drawable.my_sensz_selected, true));
        drawerItemList.add(new DrawerItem("#Friend", R.drawable.friends_normal, R.drawable.friends_selected, false));
        drawerItemList.add(new DrawerItem("#Share", R.drawable.friends_normal, R.drawable.friends_selected, false));

        drawerAdapter = new DrawerAdapter(HomeActivity.this, drawerItemList);
        drawerListView = (ListView) findViewById(R.id.drawer);

        if (drawerListView != null)
            drawerListView.setAdapter(drawerAdapter);

        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        homeActionBarDrawerToggle.syncState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        homeActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Handle open/close behaviours of Navigation Drawer
     */
    private class HomeActionBarDrawerToggle extends ActionBarDrawerToggle {

        public HomeActionBarDrawerToggle(Activity mActivity, DrawerLayout mDrawerLayout) {
            super(mActivity, mDrawerLayout, R.drawable.ic_navigation_drawer, R.string.ns_menu_open, R.string.ns_menu_close);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDrawerClosed(View view) {
            invalidateOptionsMenu();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDrawerOpened(View drawerView) {
            invalidateOptionsMenu();
        }
    }

    /**
     * Drawer click event handler
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Highlight the selected item, update the title, and close the drawer
            // update selected item and title, then close the drawer
            drawerLayout.closeDrawer(drawerContainer);

            //  reset content in drawer list
            for (DrawerItem drawerItem : drawerItemList) {
                drawerItem.setSelected(false);
            }

            if (position == 0) {
                drawerItemList.get(0).setSelected(true);
                loadSensors();
            } else if (position == 1) {
                drawerItemList.get(1).setSelected(true);
                loadFriends();
            } else if (position == 2) {
                drawerItemList.get(2).setSelected(true);
                loadShare();
            }

            drawerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Load my sensor list fragment
     */
    private void loadSensors() {
        SenzListFragment sensorListFragment = new SenzListFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, sensorListFragment);
        transaction.commit();
    }

    private void loadFriends() {
        FriendListFragment shareFragment = new FriendListFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, shareFragment);
        transaction.commit();
    }

    private void loadShare() {
        ShareFragment shareFragment = new ShareFragment();

        // fragment transitions
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, shareFragment);
        transaction.commit();
    }

    /**
     * Exit from activity
     */
    private void exit() {
        HomeActivity.this.finish();
    }

}
