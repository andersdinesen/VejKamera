package com.vejkamera.favorites.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;

import java.util.ArrayList;

/**
 * Created by ad on 28-10-2015.
 */
public class NavDrawerListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }*/

        NavDrawerItem currentNavDrawerItem = navDrawerItems.get(position);
        if (convertView == null) {
            convertView = currentNavDrawerItem.setupLayout(convertView, context);
        }
        /*
        if(currentNavDrawerItem instanceof NavDrawerItemMainHeading){
            imgIcon.setImageResource(((NavDrawerItemMainHeading) currentNavDrawerItem).getIcon());
        } else if (currentNavDrawerItem instanceof NavDrawerItemHeading) {
            imgIcon.setImageResource(((NavDrawerItemHeading) navDrawerItems.get(position)).getIcon());
            txtTitle.setText(((NavDrawerItemHeading) navDrawerItems.get(position)).getTitle());
            txtTitle.setTypeface(null, Typeface.BOLD);
        } else if (currentNavDrawerItem instanceof NavDrawerItemLine) {
            txtTitle.setText(((NavDrawerItemLine) navDrawerItems.get(position)).getTitle());
        }
*/
        return convertView;
    }

}
