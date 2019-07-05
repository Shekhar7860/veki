package com.onewayit.veki.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.onewayit.veki.R;
import com.onewayit.veki.utilities.AndroidVersion;

import java.util.ArrayList;

// import com.squareup.picasso.Picasso;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private ArrayList<AndroidVersion> android_versions;
    private Context context;


    public NotificationAdapter(Context context, ArrayList<AndroidVersion> android_versions) {
        this.context = context;
        this.android_versions = android_versions;

    }


    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notifications_row_layout, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

        viewHolder.tv_android.setText(android_versions.get(i).get_name());
        //  Picasso.with(context).load("https://i.imgur.com/tGbaZCY.jpg").into(viewHolder.img_android);
        viewHolder.img_android.setImageResource(android_versions.get(i).get_image_url());
        viewHolder.img_android.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final Intent intent;
//                intent =  new Intent(context, RequestDetails.class);
//                intent.putExtra("Name", android_versions.get(i).get_name());
//                Bitmap bitmap = BitmapFactory.decodeResource
//                        (context.getResources(), android_versions.get(i).get_image_url()); // your bitmap
//                ByteArrayOutputStream bs = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
//                intent.putExtra("Img", bs.toByteArray());
//                // intent.putExtra("Img", android_versions.get(i).get_image_url());
//                context.startActivity(intent);
            }
            //your action
        });

        //  Picasso.with(context).load(android_versions.get(i).getAndroid_image_url()).resize(120, 60).into(viewHolder.img_android);
    }


    @Override
    public int getItemCount() {
        return android_versions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_android;
        ImageView img_android;

        public ViewHolder(View view) {
            super(view);
            tv_android = view.findViewById(R.id.tv_android);
            img_android = view.findViewById(R.id.img_android);


        }
    }


}