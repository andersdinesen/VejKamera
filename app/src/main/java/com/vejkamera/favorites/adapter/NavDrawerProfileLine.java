package com.vejkamera.favorites.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.vejkamera.R;

/**
 * Created by ad on 28-10-2015.
 */
public class NavDrawerProfileLine implements NavDrawerItem{
    private String title;
    private int profileId;
    private View view;
    private boolean isSelected = false;

    public NavDrawerProfileLine(String title, int profileId) {
        this.title = title;
        this.profileId = profileId;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public int getProfileId() {
        return profileId;
    }

    public boolean isSelected() { return isSelected; }

    public void setIsSelected(boolean isSelected) { this.isSelected = isSelected; }

    @Override
    public View setupLayout(View convertView, Context context){
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_profile_line, null);
            view = convertView;
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        txtTitle.setText(getTitle());
        if(isSelected){
            handleSelected();
        }

        return convertView;
    }

    public void handleSelected(){
        isSelected = true;
        TextView txtTitle = (TextView) view.findViewById(R.id.title);
        txtTitle.setTypeface(txtTitle.getTypeface(), Typeface.BOLD);
    }

    public void handleNotSelected(){
        if(isSelected) {
            TextView txtTitle = (TextView) view.findViewById(R.id.title);
            txtTitle.setTypeface(null, Typeface.NORMAL);
            isSelected = false;
        }
    }


        @Override
    public String toString() {
        return title;
    }
}
