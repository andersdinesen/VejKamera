package com.vejkamera.favorites;

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
public class FavoriteListAdapter extends ArrayAdapter<RoadCamera> {
    private Context context;
    private List<RoadCamera> values;

    public FavoriteListAdapter(Context context, List<RoadCamera> values){
        super(context, R.layout.row_favorite_camera, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_favorite_camera, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);

        textView.setText(values.get(position).getTitle());
        if(values.get(position).getBitmap() != null){
            imageView.setImageBitmap(values.get(position).getBitmap());
        }
        return rowView;
    }
}
