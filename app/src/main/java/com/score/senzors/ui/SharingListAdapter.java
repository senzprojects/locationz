package com.score.senzors.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.score.senzors.pojos.User;
import com.score.senzors.R;

import java.util.ArrayList;

/**
 * Adapter class to display friend/user list
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SharingListAdapter extends BaseAdapter {

    private SharingList sharingList;
    private ArrayList<User> userList;

    // set custom font
    Typeface face;

    /**
     * Initialize context variables
     * @param sharingList activity
     * @param userList user list
     */
    public SharingListAdapter(SharingList sharingList, ArrayList<User> userList) {
        face = Typeface.createFromAsset(sharingList.getActivity().getAssets(), "fonts/vegur_2.otf");

        this.sharingList = sharingList;
        this.userList = userList;
    }

    /**
     * Get size of user list
     * @return userList size
     */
    @Override
    public int getCount() {
        return userList.size();
    }

    /**
     * Get specific item from user list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return userList.get(i);
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

        final User user = (User) getItem(i);

        if (view == null) {
            // inflate sharing_list_row_layout
            LayoutInflater layoutInflater = (LayoutInflater) sharingList.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.sharing_list_row_layout, viewGroup, false);

            holder = new ViewHolder();
            holder.phone = (TextView) view.findViewById(R.id.sharing_list_row_layout_phone);
            holder.phone.setTypeface(face, Typeface.BOLD);

            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        holder.phone.setText(user.getUsername());
        view.setBackgroundResource(R.drawable.friend_list_selector);

        // set up un-share click action
        RelativeLayout unshare = (RelativeLayout)view.findViewById(R.id.sharing_list_row_layout_unshare);
        unshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharingList.unshare(user);
            }
        });

        return view;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView phone;
    }
}
