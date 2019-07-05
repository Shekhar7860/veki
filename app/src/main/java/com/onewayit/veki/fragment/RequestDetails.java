package com.onewayit.veki.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.onewayit.veki.R;

public class RequestDetails extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    Context context;
    View view;
    ImageView back_button_home_activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_request_detail, container, false);
        findViewByID();
        setClickListeners();
        setMapView();
        return view;
    }

    private void setClickListeners() {
        back_button_home_activity.setOnClickListener(this);
    }

    private void findViewByID() {
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
    }

    private void setMapView() {

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        LatLng myloc = new LatLng(30.7352102, 76.6934882);
        googleMap.addMarker(new MarkerOptions()
                .position(myloc)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker))
                .title("Mohali 7 Phase"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.7352102, 76.6934882), 12.0f));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button_home_activity: {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
            }
        }
    }
}
