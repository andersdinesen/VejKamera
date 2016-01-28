package com.vejkamera.map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;

import java.util.List;

/**
 * Created by ad on 27-05-2015.
 */
public class MapCameraListAdapter extends ArrayAdapter<RoadCamera> {
    private static int imageWidth, imageHeight = 0;
    private Context context;
    private List<RoadCamera> roadCameras;

    public MapCameraListAdapter(Context context, List<RoadCamera> values){
        super(context, R.layout.map_camera_info, values);
        this.context = context;
        this.roadCameras = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.map_camera_info, parent, false);
        RoadCamera roadCamera = roadCameras.get(position);

        ImageView thumbnail = ((ImageView) rowView.findViewById(R.id.thumbnail_in_map_info_view));
        adjustImageSize(thumbnail);
        TextView title = ((TextView) rowView.findViewById(R.id.title_in_map_info_view));
        title.setText(roadCamera.getTitle());

        ImageView mapPin = ((ImageView) rowView.findViewById(R.id.map_pin_in_map_info_view));
        mapPin.setImageResource(DirectionToMapPin.getMapPinIconFromRoadCamera(roadCamera, context));

        if(roadCamera.getThumbnail() != null) {
            thumbnail.setImageBitmap(roadCamera.getThumbnail());
        }

        return rowView;
    }

    private void adjustImageSize(ImageView imageView) {
        if(imageWidth == 0){
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);

            // Adjusting with to 1/4 of screen width
            imageWidth = Math.round(metrics.widthPixels/4);

            // Adjusting height to wide-screen format 16:9
            imageHeight = Math.round(imageWidth * 0.5625f);
        }

        imageView.getLayoutParams().width = imageWidth;
        imageView.getLayoutParams().height = imageHeight;
/*
        progressBar.getLayoutParams().width = imageWidth;
        progressBar.getLayoutParams().height = imageHeight;
        */
    }
}
