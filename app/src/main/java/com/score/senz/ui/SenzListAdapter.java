package com.score.senz.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.score.senz.R;
import com.score.senz.pojos.Senz;

import java.util.ArrayList;

/**
 * Adapter class to display sensor list
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Senz> senzList;

    // set custom font
    private Typeface typefaceThin;

    /**
     * Initialize context variables
     *
     * @param context  activity context
     * @param senzList sharing user list
     */
    public SenzListAdapter(Context context, ArrayList<Senz> senzList) {
        typefaceThin = Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");

        this.context = context;
        this.senzList = senzList;
    }

    /**
     * Get size of sensor list
     *
     * @return userList size
     */
    @Override
    public int getCount() {
        return senzList.size();
    }

    /**
     * Get specific item from sensor list
     *
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return senzList.get(i);
    }

    /**
     * Get sensor list item id
     *
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Create list row view
     *
     * @param i         index
     * @param view      current list item view
     * @param viewGroup parent
     * @return view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;

        final Senz senz = (Senz) getItem(i);

        if (view == null) {
            //inflate sensor list row layout
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.sensor_list_row_layout, viewGroup, false);

            //create view holder to store reference to child views
            holder = new ViewHolder();
            holder.user = (TextView) view.findViewById(R.id.sensor_list_row_layout_sensor_user);
            holder.lastSeen = (TextView) view.findViewById(R.id.sensor_list_row_layout_sensor_value);
            holder.image = (CircularImageView) view.findViewById(R.id.contact_image);

            // set custom font
            holder.user.setTypeface(typefaceThin, Typeface.BOLD);
            holder.lastSeen.setTypeface(typefaceThin, Typeface.BOLD);

            view.setTag(holder);
        } else {
            //get view holder back_icon
            holder = (ViewHolder) view.getTag();
        }

        setUpSenzRow(i, senz, view, holder);

        return view;
    }

    private void setUpSenzRow(int i, Senz senz, View view, ViewHolder viewHolder) {
        // enable share and change color of view
        view.setBackgroundResource(R.drawable.my_sensor_background);
        viewHolder.user.setText("@" + senz.getSender().getUsername());

        if (senz.getAttributes().containsKey("Location")) {
            // Location senz
            if (senz.getAttributes().get("Location") != null && !senz.getAttributes().get("Location").isEmpty()) {
                String locationText = "<font color=#4a4a4a>Last seen in</font> <font color=#ffc027>" + "<b>" + senz.getAttributes().get("Location") + "</b>" + "</font>";
                viewHolder.lastSeen.setText(Html.fromHtml(locationText));
                viewHolder.lastSeen.setTextColor(Color.parseColor("#ffc027"));
            } else {
                viewHolder.lastSeen.setText("No last seen location available");
                viewHolder.lastSeen.setTextColor(Color.parseColor("#4a4a4a"));
            }
        } else if (senz.getAttributes().containsKey("GPIO3")) {
            // GPIO senz
            if (!senz.getAttributes().get("GPIO3").isEmpty()) {
                String locationText = "<font color=#4a4a4a>Switch is </font> <font color=#ffc027>" + "<b>" + senz.getAttributes().get("GPIO3") + "</b>" + "</font>";
                viewHolder.lastSeen.setText(Html.fromHtml(locationText));
                viewHolder.lastSeen.setTextColor(Color.parseColor("#ffc027"));
            } else {
                viewHolder.lastSeen.setText("No switch status available");
                viewHolder.lastSeen.setTextColor(Color.parseColor("#4a4a4a"));
            }
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_user_icon);
        viewHolder.image.setImageBitmap(largeIcon);
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView user;
        TextView lastSeen;
        CircularImageView image;
    }

}
