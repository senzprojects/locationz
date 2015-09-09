package com.score.senz.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.score.senz.R;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.pojos.Sensor;
import com.score.senz.pojos.Senz;

import java.util.ArrayList;

/**
 * Display sensor list/ Fragment
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorListFragment extends Fragment {

    private static final String TAG = SensorListFragment.class.getName();

    // list view components
    private ListView sensorListView;
    private ArrayList<Senz> senzList;
    private SensorListAdapter adapter;

    // empty view to display when no sensors available
    private ViewStub emptyView;

    // use custom font here
    private Typeface typeface;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.sensor_list_layout, container, false);

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/vegur_2.otf");

        initEmptyView();
        initSensorListView();
    }

    /**
     * {@inheritDoc}
     */
    public void onResume() {
        super.onResume();

        setUpActionBarTitle("#Friend #Senz");
        displaySensorList();
    }

    /**
     * {@inheritDoc}
     */
    public void onPause() {
        super.onPause();
    }

    /**
     * Initialize UI components
     */
    private void initSensorListView() {
        sensorListView = (ListView) getActivity().findViewById(R.id.sensor_list_layout_sensor_list);

        // add header and footer for list
        View headerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        sensorListView.addHeaderView(headerView);
        sensorListView.addFooterView(footerView);

        // set up click listener
        sensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: click on sensor list item");
                if (position > 0 && position <= senzList.size()) {
                    Senz senz = senzList.get(position - 1);
                    // TODO GET Senz to server
                }
            }
        });
    }

    /**
     * Initialize empty view for list view
     * empty view need to be display when no sensors available
     */
    private void initEmptyView() {
        emptyView = (ViewStub) getActivity().findViewById(R.id.sensor_list_layout_empty_view);
        View inflatedEmptyView = emptyView.inflate();
        TextView emptyText = (TextView) inflatedEmptyView.findViewById(R.id.empty_text);
        emptyText.setText("No Friends.SenZors available");
        emptyText.setTypeface(typeface);
    }

    /**
     * Display sensor list
     * Basically setup list adapter if have items to display otherwise display empty view
     */
    private void displaySensorList() {
        // get sensors from db
        senzList = (ArrayList<Senz>) new SenzorsDbSource(this.getActivity()).getSenzes();

        // construct list adapter
        if (senzList.size() > 0) {
            adapter = new SensorListAdapter(SensorListFragment.this.getActivity(), senzList);
            adapter.notifyDataSetChanged();
            sensorListView.setAdapter(adapter);
        } else {
            adapter = new SensorListAdapter(SensorListFragment.this.getActivity(), senzList);
            sensorListView.setAdapter(adapter);
            sensorListView.setEmptyView(emptyView);
        }
    }

    /**
     * Set action bar title according to currently selected sensor type
     * Set custom font to title
     *
     * @param title action bar title
     */
    private void setUpActionBarTitle(String title) {
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView yourTextView = (TextView) (this.getActivity().findViewById(titleId));
        yourTextView.setTextColor(getResources().getColor(R.color.white));
        yourTextView.setTypeface(typeface);

        getActivity().getActionBar().setTitle(title);
    }

}
