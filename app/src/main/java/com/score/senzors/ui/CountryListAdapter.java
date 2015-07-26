package com.score.senzors.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.pojos.Country;
import com.score.senzors.pojos.User;

import java.util.ArrayList;

/**
 * Adapter class to display countries
 */
public class CountryListAdapter extends BaseAdapter implements Filterable {

    private CountryListActivity activity;
    private CountryFilter countryFilter;
    private Typeface typeface;
    private ArrayList<Country> countryList;
    private ArrayList filteredList;

    /**
     * Initialize context variables
     * @param activity list activity
     * @param countryList friend list
     */
    public CountryListAdapter(CountryListActivity activity, ArrayList<Country> countryList) {
        this.activity = activity;
        this.countryList = countryList;
        this.filteredList = countryList;
        typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/vegur_2.otf");

        getFilter();
    }

    /**
     * Get size of user list
     * @return userList size
     */
    @Override
    public int getCount() {
        return filteredList.size();
    }

    /**
     * Get specific item from user list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return filteredList.get(i);
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
     * @param position index
     * @param view current list item view
     * @param parent parent
     * @return view
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;
        final Country country = (Country) getItem(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.country_list_row_layout, parent, false);
            holder = new ViewHolder();
            holder.iconText = (TextView) view.findViewById(R.id.icon_text);
            holder.name = (TextView) view.findViewById(R.id.country_list_row_layout_name);
            holder.iconText.setTypeface(typeface, Typeface.BOLD);
            holder.iconText.setTextColor(activity.getResources().getColor(R.color.white));
            holder.name.setTypeface(typeface, Typeface.NORMAL);

            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        holder.iconText.setText("#");
        holder.name.setText(country.getName());
        view.setBackgroundResource(R.drawable.friend_list_selector);

        return view;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView iconText;
        TextView name;
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (countryFilter == null) {
            countryFilter = new CountryFilter();
        }

        return countryFilter;
    }

    /**
     * Custom filter for country list
     * Filter content in country list according to the search text
     */
    private class CountryFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<Country> tempList = new ArrayList<Country>();

                // search content in friend list
                for (Country country : countryList) {
                    if (country.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(country);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = countryList.size();
                filterResults.values = countryList;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    }

}
