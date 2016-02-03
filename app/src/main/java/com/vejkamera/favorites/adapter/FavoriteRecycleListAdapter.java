package com.vejkamera.favorites.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.vejkamera.R;
import com.vejkamera.RoadCamera;
import com.vejkamera.details.RoadCameraDetailsActivity;
import com.vejkamera.favorites.RoadCameraArchiveHandler;
import com.vejkamera.services.RoadCameraImageReaderService;
import com.vejkamera.services.RoadCameraReadRequest;

import java.util.List;

/**
 * Created by Anders on 02-11-2015.
 */
public class FavoriteRecycleListAdapter extends RecyclerView.Adapter<FavoriteRecycleListAdapter.ViewHolder>{
    private static int imageHeightGrid1, imageHeightGrid2, imageHeightGrid3 = 0;
    private List<RoadCamera> roadCameras;
    private ViewGroup parent;

    public FavoriteRecycleListAdapter(List<RoadCamera> roadCameras){
        this.roadCameras = roadCameras;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            this.name = (TextView) itemView.findViewById(R.id.label);
            this.image = (ImageView) itemView.findViewById(R.id.image);

            itemView.setOnClickListener(this);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RoadCamera roadCamera = roadCameras.get(getLayoutPosition());

            //Setting Camera image to null, because it may be too big for the internal parcel bundle
            roadCamera.setBitmap(null);
            Intent intent = new Intent(v.getContext(), RoadCameraDetailsActivity.class);
            //intent.putExtra(RoadCameraDetailsActivity.ROAD_CAMERA_KEY, roadCamera);
            //intent.putExtra(RoadCameraDetailsActivity.ROAD_CAMERA_SYNC_ID_KEY, roadCamera.getSyncId());
            RoadCameraReadRequest readRequest = new RoadCameraReadRequest(RoadCameraReadRequest.READ_TYPE_SYNC_IDS, roadCamera.getSyncId());
            intent.putExtra(RoadCameraImageReaderService.READ_REQUEST_KEY, readRequest);
            v.getContext().startActivity(intent);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        View roadCameraView = inflater.inflate(R.layout.row_favorite_camera, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(roadCameraView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RoadCamera roadCamera = roadCameras.get(position);

        holder.name.setText(roadCamera.getTitle());
        holder.image.setImageBitmap(roadCamera.getBitmap());

        adjustImageHeight(holder.image);
    }

    private void adjustImageHeight(ImageView image) {
         if(imageHeightGrid1 == 0 || imageHeightGrid2 ==0 || imageHeightGrid3 == 0){
            WindowManager wm = (WindowManager) parent.getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);

            // Adjusting to wide-screen format 16:9
            imageHeightGrid1 = Math.round(metrics.widthPixels * 0.5625f);
            imageHeightGrid2 = Math.round(metrics.widthPixels/2 * 0.5625f);
            imageHeightGrid3 = Math.round(metrics.widthPixels/3 * 0.5625f);
        }
        switch (RoadCameraArchiveHandler.getFavoritesGridLayout(parent.getContext())) {
            case 1:
                image.getLayoutParams().height = imageHeightGrid1;
                break;
            case 2:
                image.getLayoutParams().height = imageHeightGrid2;
                break;
            case 3:
                image.getLayoutParams().height = imageHeightGrid3;
                break;
        }
    }

    @Override
    public int getItemCount() {
        return roadCameras.size();
    }
}
