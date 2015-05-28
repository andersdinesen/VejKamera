package com.vejkamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ad on 27-05-2015.
 */
public class RoadCameraListAdapter extends ArrayAdapter<RoadCamera> {
    private Context context;
    private List<RoadCamera> values;

    public RoadCameraListAdapter(Context context, List<RoadCamera> values){
        super(context, R.layout.favorite_camera_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.favorite_camera_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);

        textView.setText(values.get(position).getDisplayName());
        if(values.get(position).getBitmap() != null){
            imageView.setImageBitmap(values.get(position).getBitmap());
        }
        return rowView;
    }
}
