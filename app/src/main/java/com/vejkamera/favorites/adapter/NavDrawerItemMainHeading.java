package com.vejkamera.favorites.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.vejkamera.R;

/**
 * Created by ad on 28-10-2015.
 */
public class NavDrawerItemMainHeading implements NavDrawerItem {
    private int icon;

    public NavDrawerItemMainHeading(int icon){
        this.icon = icon;
    }

    public int getIcon(){
        return this.icon;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

    @Override
    public View setupLayout(View convertView, Context context) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_main_header_item, null);
        }

        return convertView;
        //convertView.setBackgroundColor(convertView.getResources().getColor(R.color.color_primary));
    }
}
