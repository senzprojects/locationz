package com.score.senz.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.score.senz.R;
import com.score.senz.pojos.Senz;
import com.score.senz.utils.PhoneBookUtils;

import java.util.ArrayList;

/**
 * Adapter class to display sensor list
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SensorListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Senz> senzList;

    // set custom font
    Typeface typefaceThin;
    Typeface typefaceBlack;

    /**
     * Initialize context variables
     *
     * @param context  activity context
     * @param senzList sharing user list
     */
    public SensorListAdapter(Context context, ArrayList<Senz> senzList) {
        typefaceThin = Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");
        typefaceBlack = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");

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

        setUpSenzRow(senz, view, holder);

        return view;
    }

    private void setUpSenzRow(Senz senz, View view, ViewHolder viewHolder) {
        // enable share and change color of view
        view.setBackgroundResource(R.drawable.my_sensor_background);
        //viewHolder.sensorName.setBackgroundResource(R.drawable.circle_shape_green);
        //viewHolder.sensorUser.setTextColor(Color.parseColor("#11b29c"));
        viewHolder.user.setText("@" + senz.getSenderName());
        viewHolder.lastSeen.setText("Last seen from Kaluthara");
        //viewHolder.contactImage.setImageBitmap(getRoundedShape(PhoneBookUtils.getImage(context, "+94715991422")));
        viewHolder.image.setImageBitmap(PhoneBookUtils.getImage(context, "+94715991422"));
        //viewHolder.contactImage.setImageURI(PhoneBookUtils.getImage(context, "+94715991422"));
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView user;
        TextView lastSeen;
        CircularImageView image;
    }

    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 50;
        int targetHeight = 50;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

}
