package com.vejkamera.area;

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
public class AreaCameraListAdapter extends ArrayAdapter<RoadCamera> {
    private static int imageWidth, imageHeight = 0;
    private Context context;
    private List<RoadCamera> roadCameras;

    public AreaCameraListAdapter(Context context, List<RoadCamera> values){
        super(context, R.layout.row_area_camera, values);
        this.context = context;
        this.roadCameras = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_area_camera, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.imgProgress);

        adjustImageSize(imageView, progressBar);

        textView.setText(roadCameras.get(position).getTitle());
        if(roadCameras.get(position).getThumbnail() != null){
            imageView.setImageBitmap(roadCameras.get(position).getThumbnail());
            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
        return rowView;
    }

    private void adjustImageSize(ImageView imageView, ProgressBar progressBar) {
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

        progressBar.getLayoutParams().width = imageWidth;
        progressBar.getLayoutParams().height = imageHeight;
    }
}
