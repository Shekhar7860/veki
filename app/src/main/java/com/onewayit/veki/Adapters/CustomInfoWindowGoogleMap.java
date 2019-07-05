package com.onewayit.veki.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.onewayit.veki.R;
import com.onewayit.veki.fragment.PaymentFragment;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    TextView tv_hire;
    FragmentManager fragmentManager;
    private Context context;
    private AppCompatActivity activity;

    public CustomInfoWindowGoogleMap(Context ctx, FragmentManager fragmentManager) {
        context = ctx;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.marker_short_desc, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(550, 450));
        tv_hire = view.findViewById(R.id.tv_hire);
        tv_hire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaymentFragment fragment2 = new PaymentFragment();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
            }
        });
        return view;
    }
}