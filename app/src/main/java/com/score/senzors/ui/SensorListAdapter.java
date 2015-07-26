package com.score.senzors.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.score.senzors.pojos.Sensor;
import com.score.senzors.R;

import java.util.ArrayList;

/**
 * Adapter class to display sensor list
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Sensor> sensorList;

    // set custom font
    Typeface typefaceThin;
    Typeface typefaceBlack;

    /**
     * Initialize context variables
     *
     * @param context activity context
     * @param sensorList sharing user list
     */
    public SensorListAdapter(Context context, ArrayList<Sensor> sensorList) {
        typefaceThin = Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");
        typefaceBlack = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");

        this.context = context;
        this.sensorList = sensorList;
    }

    /**
     * Get size of sensor list
     * @return userList size
     */
    @Override
    public int getCount() {
        return sensorList.size();
    }

    /**
     * Get specific item from sensor list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return sensorList.get(i);
    }

    /**
     * Get sensor list item id
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Create list row view
     * @param i index
     * @param view current list item view
     * @param viewGroup parent
     * @return view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;

        final Sensor sensor = (Sensor) getItem(i);

        if (view == null) {
            //inflate sensor list row layout
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.sensor_list_row_layout, viewGroup, false);

            //create view holder to store reference to child views
            holder = new ViewHolder();
            holder.sensorName = (TextView) view.findViewById(R.id.sensor_name_text);
            holder.sensorUser = (TextView) view.findViewById(R.id.sensor_list_row_layout_sensor_user);
            holder.sensorValue = (TextView) view.findViewById(R.id.sensor_list_row_layout_sensor_value);
            holder.share = (RelativeLayout) view.findViewById(R.id.sensor_list_row_layout_share);

            // set custom font
            holder.sensorName.setTypeface(typefaceThin);
            holder.sensorUser.setTypeface(typefaceBlack);
            holder.sensorValue.setTypeface(typefaceThin);

            view.setTag(holder);
        } else {
            //get view holder back_icon
            holder = (ViewHolder) view.getTag();
        }

        // handle my/friend sensors
        if(sensor.isMySensor()) {
            setUpMySensor(sensor, view, holder);
        } else {
            setUpFriendSensor(sensor, view, holder);
        }

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start share activity
                Intent intent = new Intent(context, ShareActivity.class);
                context.startActivity(intent);
                ((HomeActivity) context).overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);
            }
        });

        return view;
    }

    private void setUpMySensor(Sensor sensor, View view, ViewHolder viewHolder) {
        // enable share and change color of view
        view.setBackgroundResource(R.drawable.my_sensor_background);
        viewHolder.share.setVisibility(View.GONE);
        viewHolder.sensorName.setBackgroundResource(R.drawable.circle_shape_red);
        viewHolder.sensorUser.setTextColor(Color.parseColor("#d96459"));
        String userText = (sensor.getUser().getUsername().equals("") ? sensor.getUser().getPhoneNo() : sensor.getUser().getUsername());
        viewHolder.sensorUser.setText("@" + userText);
        viewHolder.sensorValue.setText(R.string.tap_here);
    }

    private void setUpFriendSensor(Sensor sensor, View view, ViewHolder viewHolder) {
        // disable share and change color of view
        view.setBackgroundResource(R.drawable.my_sensor_background);
        viewHolder.share.setVisibility(View.GONE);
        viewHolder.sensorName.setBackgroundResource(R.drawable.circle_shape_green);
        viewHolder.sensorUser.setTextColor(Color.parseColor("#11b29c"));
        viewHolder.sensorUser.setText("@" + sensor.getUser().getUsername());
        viewHolder.sensorValue.setText(R.string.tap_here);
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView sensorName;
        TextView sensorUser;
        TextView sensorValue;
        RelativeLayout share;
    }

}
