package com.vejkamera.favorites.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;

/**
 * Created by ad on 28-10-2015.
 */
public class NavDrawerItemHeading implements NavDrawerItem {
    private String title;
    private int icon;

    public NavDrawerItemHeading(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

    @Override
    public View setupLayout(View convertView, Context context){
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        imgIcon.setImageResource(getIcon());
        txtTitle.setText(getTitle());
        txtTitle.setTypeface(null, Typeface.BOLD);

        return convertView;
    }

    @Override
    public String toString() {
        return title;
    }
}
