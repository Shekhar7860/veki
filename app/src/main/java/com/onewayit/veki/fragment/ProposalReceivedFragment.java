package com.onewayit.veki.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.onewayit.veki.Adapters.CustomInfoWindowGoogleMap;
import com.onewayit.veki.Adapters.ServicesAdapter;
import com.onewayit.veki.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class ProposalReceivedFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, View.OnClickListener, SlidingUpPanelLayout.PanelSlideListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Context context;
    View view;
    SlidingUpPanelLayout slidingUpPanelLayout;
    ImageView iv_downup;
    RecyclerView recyclerView;
    ArrayList<String> items;
    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_proposal_received, container, false);
        context = getActivity();
        findViewIds();
        // checkPermission();
        setOnClickListener();
        loadMap();
        setAdapter();
        return view;
    }

    private void setAdapter() {
        for (int i = 0; i < 15; i++) {
            items.add(String.valueOf(i));
        }
        ServicesAdapter servicesAdapter = new ServicesAdapter(items, getFragmentManager());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(servicesAdapter);
    }

    private void setOnClickListener() {
        slidingUpPanelLayout.addPanelSlideListener(this);
    }


    private void findViewIds() {
        slidingUpPanelLayout = view.findViewById(R.id.sliding_layout);
        //slidingUpPanelLayout.setAnchorPoint(0.5f);
        iv_downup = view.findViewById(R.id.iv_downup);
        recyclerView = view.findViewById(R.id.recycler_view);
        items = new ArrayList<>();

    }

    public void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        addMarkers();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
    }

    private void addMarkers() {
        LatLng latLng1 = new LatLng(30.7017322, 76.722553);
        LatLng latLng2 = new LatLng(30.69241, 76.7318493);
        LatLng latLng3 = new LatLng(30.7567589, 76.7164291);
        LatLng latLng4 = new LatLng(30.8050769, 76.7269012);

        Marker m1 = mMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_app))
                .title("Mohali 7 Phase"));
        Marker m2 = mMap.addMarker(new MarkerOptions()
                .position(latLng2)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_app))
                .title("11 Phase bypass"));
        Marker m3 = mMap.addMarker(new MarkerOptions()
                .position(latLng3)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_app))
                .title("11 Phase bypass"));
        Marker m4 = mMap.addMarker(new MarkerOptions()
                .position(latLng4)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_app))
                .title("11 Phase bypass"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 12.0f));

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context, getFragmentManager());
        mMap.setInfoWindowAdapter(customInfoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                PaymentFragment fragment2 = new PaymentFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else {
                    getActivity().finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        return false;
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED || newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            iv_downup.setRotation((float) 0);
            recyclerView.setNestedScrollingEnabled(false);
        }
        if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED || newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
            iv_downup.setRotation((float) 180);
            recyclerView.setNestedScrollingEnabled(true);
        }
    }
}
