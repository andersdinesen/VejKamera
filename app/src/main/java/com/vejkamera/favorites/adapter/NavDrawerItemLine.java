package com.vejkamera.favorites.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;

/**
 * Created by ad on 28-10-2015.
 */
public class NavDrawerItemLine implements NavDrawerItem{
    private String title;

    public NavDrawerItemLine(String title){
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    @Override
    public View setupLayout(View convertView, Context context){
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        txtTitle.setText(getTitle());

        return convertView;
    }


        @Override
    public String toString() {
        return title;
    }
}
