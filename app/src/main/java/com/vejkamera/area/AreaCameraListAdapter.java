package com.vejkamera.area;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

import java.util.List;

/**
 * Created by ad on 27-05-2015.
 */
public class AreaCameraListAdapter extends ArrayAdapter<RoadCamera> {
    private Context context;
    private List<RoadCamera> roadCameras;

    public AreaCameraListAdapter(Context context, List<RoadCamera> values){
        super(context, R.layout.area_camera_row, values);
        this.context = context;
        this.roadCameras = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.area_camera_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);

        textView.setText(roadCameras.get(position).getTitle());
        if(roadCameras.get(position).getThumbnail() != null){
            imageView.setImageBitmap(roadCameras.get(position).getThumbnail());
        }
        return rowView;
    }
}
