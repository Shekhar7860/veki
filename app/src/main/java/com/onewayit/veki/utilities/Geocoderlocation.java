package com.onewayit.veki.utilities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.onewayit.veki.activities.MapsActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Geocoderlocation {

    private static final String TAG = "GeocodingLocation";
    private LatLng[] latLng = new LatLng[1];

    public LatLng getAddressFromLocation(final String locationAddress,
                                         final Context context, final Handler handler) {

        Thread thread = new Thread() {
            @Override
            public void run() {

                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
               // Toast.makeText(context,"hereee",Toast.LENGTH_SHORT).show();
                try {
                    List addressList = geocoder.getFromLocationName(locationAddress, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = (Address) addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        sb.append(address.getLatitude()).append("\n");
                        sb.append(address.getLongitude()).append("\n");
                        latLng[0] =new LatLng(address.getLatitude(),address.getLongitude());
                        Log.e("lat lng is :", String.valueOf(latLng[0].latitude));
                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Address: " + locationAddress +
                                "\n\nLatitude and Longitude :\n" + result;
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Address: " + locationAddress +
                                "\n Unable to get Latitude and Longitude for this address location.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                }
            }
        };
        thread.start();
        return latLng[0];
    }
}