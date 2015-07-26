package com.score.senzors.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.score.senzors.pojos.DrawerItem;
import com.score.senzors.R;

import java.util.ArrayList;

/**
 * Adapter class that using to generate Drawer List
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class DrawerAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<DrawerItem> itemList;

    //Typeface typefaceThin;
    //Typeface typefaceBlack;
    Typeface face;

    /**
     * Initialize context variables
     * @param context activity context
     * @param itemList sharing user list
     */
    public DrawerAdapter(Context context, ArrayList<DrawerItem> itemList) {
        this.context = context;
        this.itemList = itemList;
        //typefaceThin = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
        //typefaceBlack = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");
        face = Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");
    }

    /**
     * Get size of user list
     * @return userList size
     */
    @Override
    public int getCount() {
        return itemList.size();
    }

    /**
     * Get specific item from user list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return itemList.get(i);
    }

    /**
     * Get user list item id
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

        final DrawerItem item = (DrawerItem) getItem(i);

        if (view == null) {
            //inflate sharing_list_row_layout
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.drawer_row_layout, viewGroup, false);

            //create view holder to store reference to child views
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.menurow_icon);
            holder.name = (TextView) view.findViewById(R.id.menurow_title);

            view.setTag(holder);
        } else {
            //get view holder back
            holder = (ViewHolder) view.getTag();
        }

        if(item.isSelected()) {
            holder.imageView.setImageResource(item.getSelectedResourceId());
            holder.name.setTextColor(Color.parseColor("#ffc027"));
            holder.name.setTypeface(face, Typeface.BOLD);
        } else {
            holder.imageView.setImageResource(item.getResourceId());
            holder.name.setTextColor(Color.parseColor("#4a4a4a"));
            holder.name.setTypeface(face, Typeface.NORMAL);
        }

        // bind text with view holder content view for efficient use
        holder.name.setText(item.getName());

        return view;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        ImageView imageView;
        TextView name;
    }
}
