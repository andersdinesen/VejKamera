package com.vejkamera.favorites.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;

import java.util.List;

/**
 * Created by Anders on 02-11-2015.
 */
public class FavoriteRecycleListAdapter extends RecyclerView.Adapter<FavoriteRecycleListAdapter.ViewHolder>{
    private List<RoadCamera> roadCameras;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            this.name = (TextView) itemView.findViewById(R.id.label);
            this.image = (ImageView) itemView.findViewById(R.id.image);
        }

        @Override
        public void onClick(View v) {
            RoadCamera roadCamera = roadCameras.get(getLayoutPosition());

            //Setting Camera image to null, because it may be too big for the internal parcel bundle
            roadCamera.setBitmap(null);
            Intent intent = new Intent(v.getContext(), RoadCameraDetailsActivity.class);
            intent.putExtra(RoadCameraDetailsActivity.ROAD_CAMERA_KEY, roadCamera);
            v.getContext().startActivity(intent);
        }
    }

    public FavoriteRecycleListAdapter(List<RoadCamera> roadCameras){
        this.roadCameras = roadCameras;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.favorite_camera_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RoadCamera roadCamera = roadCameras.get(position);

        holder.name.setText(roadCamera.getTitle());
        holder.image.setImageBitmap(roadCamera.getBitmap());
    }

    @Override
    public int getItemCount() {
        return roadCameras.size();
    }
}