package com.onewayit.veki.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.MarkerDragActivity;
import com.onewayit.veki.activities.PlacesAutoCompleteAdapter;
import com.onewayit.veki.utilities.GPSTracker;
import com.onewayit.veki.utilities.GlobalClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TooManyListenersException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewRequestFragment extends Fragment implements View.OnClickListener, LocationListener {
    Spinner spinnerDropDown;
    RelativeLayout footer;
    ImageView camera_image, camera_image2, camera_image3, cross, cross2, cross3, iv_gps;
    View view;
    Context context;
    TextView tv_setOnMap;
    String[] servicesList ={"car crash","car wash", "car repair", "bike repair", "car handle alignment", "car maintenance"};

    AutoCompleteTextView ac_location;
    private PlacesAutoCompleteAdapter mAdapter;
    HandlerThread mHandlerThread;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;
    Handler mThreadHandler;
    double lat=0,lng=0;
    EditText et_note;
    ArrayList<String> images=new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_new_request, container, false);
        context = getActivity();
        initViews();
        setOnClickListener();
        // findViewById();
        //setOnClickListener();
        setPlaceAdapter();
        return view;
    }

    private void initViews() {
        cross = view.findViewById(R.id.cross);
        iv_gps = view.findViewById(R.id.iv_gps);
        cross2 = view.findViewById(R.id.cross2);
        cross3 = view.findViewById(R.id.cross3);
        tv_setOnMap = view.findViewById(R.id.tv_setOnMap);
        camera_image = view.findViewById(R.id.camera_image);
        camera_image2 = view.findViewById(R.id.camera_image2);
        camera_image3 = view.findViewById(R.id.camera_image3);
        et_note=view.findViewById(R.id.et_note);
        spinnerDropDown = view.findViewById(R.id.spinner);
        ac_location=(AutoCompleteTextView)view.findViewById(R.id.ac_location);
        footer = view.findViewById(R.id.footer);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.
                R.layout.simple_spinner_dropdown_item, servicesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDropDown.setAdapter(adapter);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(getActivity());
    }

    private void setOnClickListener() {
        footer.setOnClickListener(this);
        camera_image.setOnClickListener(this);
        camera_image2.setOnClickListener(this);
        camera_image3.setOnClickListener(this);
        cross.setOnClickListener(this);
        cross2.setOnClickListener(this);
        cross3.setOnClickListener(this);
        iv_gps.setOnClickListener(this);
        tv_setOnMap.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.footer:
                if(validate()) {
                   saveRequest();
                }
                break;
            case R.id.camera_image:
                cameraIntent(0);
                break;
            case R.id.camera_image2:
                cameraIntent(1);
                break;
            case R.id.camera_image3:
                cameraIntent(2);
                break;
            case R.id.cross:
                setcamera(3);
                break;
            case R.id.cross2:
                setcamera(2);
                break;
            case R.id.cross3:
                setcamera(1);
                break;
            case R.id.iv_gps: {
                getMyLocation();
                break;
            }
            case R.id.tv_setOnMap: {
                Intent intent = new Intent(context, MarkerDragActivity.class);
                startActivityForResult(intent,5);
                break;
            }
        }
    }

    private boolean validate() {
        boolean result=true;
        if(lat==0 || lng==0){
            result=false;
            Toast.makeText(context,"Please select the location",Toast.LENGTH_SHORT).show();
        }
        else if(et_note.getText().toString().equalsIgnoreCase("")){
            Toast.makeText(context,"Please fill the required fieds",Toast.LENGTH_SHORT).show();
        }
        else if(images.size()==0){
            Toast.makeText(context,"Please add images",Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private void getMyLocation() {
        checkPermission();
    }

    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            startProgress("fetcing your location","Success");
            GPSTracker gpsTracker = new GPSTracker(context);
            if (gpsTracker.canGetLocation()) {
                Toast.makeText(context, gpsTracker.getLatitude() + " " + gpsTracker.getLongitude(), Toast.LENGTH_SHORT).show();
                // \n is for new line
                //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + lati + "\nLong: " + longi, Toast.LENGTH_LONG).show();
                 lat = gpsTracker.getLatitude();
                 lng = gpsTracker.getLongitude();

                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                StringBuilder builder = new StringBuilder();
                getAddress(lat, lng);

            } else {
                gpsTracker.showSettingsAlert();
            }
        }
    }
    private void setPlaceAdapter() {
        mAdapter=new PlacesAutoCompleteAdapter(context, R.layout.auto_complete_listitem);
        ac_location.setAdapter(mAdapter);
        if (mThreadHandler == null) {
            // Initialize and start the HandlerThread
            // which is basically a Thread with a Looper
            // attached (hence a MessageQueue)
            mHandlerThread = new HandlerThread("", android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();

            // Initialize the Handler
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        ArrayList<String> results = mAdapter.resultList;

                        if (results != null && results.size() > 0) {
                            mAdapter.notifyDataSetChanged();
                        }
                        else {
                            mAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            };
        }

    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = "";
            add = add + obj.getLocality();
            add = add + "," + obj.getAdminArea();
            add = add + "," + obj.getCountryName();


            Toast.makeText(context, add, Toast.LENGTH_SHORT).show();
            ac_location.setText(add);
            closeProgress(true);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            closeProgress(false);
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //You had this as int. It is advised to have Lat/Loing as double.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                } else {
                    Toast.makeText(context, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    private void setcamera(Integer a) {
        switch (a) {
            case 3:
                String uri = "@drawable/camera";
                int imageResource = getResources().getIdentifier(uri, null, context.getPackageName());

                // imageview= (ImageView)findViewById(R.id.imageView);
                Drawable res = getResources().getDrawable(imageResource);
                camera_image3.setImageDrawable(res);
                cross.setVisibility(View.GONE);
                break;
            case 2:
                String uri2 = "@drawable/camera";
                int imageResource2 = getResources().getIdentifier(uri2, null, context.getPackageName());
                // imageview= (ImageView)findViewById(R.id.imageView);
                Drawable res2 = getResources().getDrawable(imageResource2);
                camera_image.setImageDrawable(res2);
                cross2.setVisibility(View.GONE);
                break;
            case 1:
                String uri3 = "@drawable/camera";
                int imageResource3 = getResources().getIdentifier(uri3, null, context.getPackageName());
                // imageview= (ImageView)findViewById(R.id.imageView);
                Drawable res3 = getResources().getDrawable(imageResource3);
                camera_image2.setImageDrawable(res3);
                cross3.setVisibility(View.GONE);
                break;
        }
    }

    private void cameraIntent(Integer a) {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, a);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataReturned) {
        super.onActivityResult(requestCode, resultCode, dataReturned);
        if (dataReturned != null) {
            switch (requestCode) {
                case 0:
                    if (dataReturned.getExtras() != null) {
                        Bundle extras = dataReturned.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        camera_image.setImageBitmap(imageBitmap);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 200);
                        camera_image.setLayoutParams(layoutParams);
                        cross2.setVisibility(View.VISIBLE);
                        if(images.size()==2) {
                            images.add("data:image/png;base64,"+getEncoded64ImageStringFromBitmap(imageBitmap));
                        }
                        else {
                            images.add(1, "data:image/png;base64,"+getEncoded64ImageStringFromBitmap(imageBitmap));
                        }
                    }
                    break;
                case 1:
                    if (dataReturned.getExtras() != null) {
                        Bundle extras2 = dataReturned.getExtras();
                        Bitmap imageBitmap2 = (Bitmap) extras2.get("data");
                        camera_image2.setImageBitmap(imageBitmap2);
                        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(200, 200);
                        camera_image2.setLayoutParams(layoutParams2);
                        cross3.setVisibility(View.VISIBLE);
                        if(images.size()==3) {
                            images.add("data:image/png;base64,"+getEncoded64ImageStringFromBitmap(imageBitmap2));
                        }
                        else {
                            images.add(2, "data:image/png;base64,"+getEncoded64ImageStringFromBitmap(imageBitmap2));
                        }
                    }
                    break;
                case 2:
                    if (dataReturned.getExtras() != null) {
                        Bundle extras3 = dataReturned.getExtras();
                        Bitmap imageBitmap3 = (Bitmap) extras3.get("data");
                        camera_image3.setImageBitmap(imageBitmap3);
                        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(200, 200);
                        camera_image3.setLayoutParams(layoutParams3);
                        cross.setVisibility(View.VISIBLE);
                        if(images.size()==1) {
                            images.add("data:image/png;base64,"+getEncoded64ImageStringFromBitmap(imageBitmap3));
                        }
                        else {
                            images.add(0, "data:image/png;base64,"+getEncoded64ImageStringFromBitmap(imageBitmap3));
                        }
                    }
                    break;
                case 5:
                    if(requestCode==1 && resultCode==getActivity().RESULT_OK){
                        lat= Double.valueOf(dataReturned.getStringExtra("Lat"));
                        lng= Double.valueOf(dataReturned.getStringExtra("Long"));
                    }
            }
        }
    }
    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

    public void startProgress(String title, String result){
        progressBarGIFDialog.setCancelable(false)

                .setTitleColor(R.color.colorPrimary) // Set Title Color (int only)

                .setLoadingGifID(R.drawable.loading) // Set Loading Gif

                .setDoneGifID(R.drawable.done) // Set Done Gif

                .setDoneTitle(result) // Set Done Title


                .setLoadingTitle(title) // Set Loading Title

                .build();
    }
    public void closeProgress(boolean result){
        if(result) {
            progressBarGIFDialog.setDoneGifID(R.drawable.done);
            progressBarGIFDialog.clear();
        }
        else {
            progressBarGIFDialog.setDoneTitle("Failed.");
            progressBarGIFDialog.setDoneGifID(R.drawable.cancel_progress);
            progressBarGIFDialog.clear();
        }
        // progressBarGIFDialog.
    }
    private JSONObject getRequestParams() {
        JSONObject jsonObject=new JSONObject();
        UserSessionPreferences userSessionPreferences=new UserSessionPreferences(context);
        try {
            jsonObject.put("user_id",userSessionPreferences.getUserID());
            jsonObject.put("service_id",getServiceId(spinnerDropDown.getSelectedItem().toString()));
            jsonObject.put("latitude",lat);
            jsonObject.put("longitude",lng);
            jsonObject.put("distance_start",0);
            jsonObject.put("distance_end",1000);
            jsonObject.put("notes",et_note.getText().toString());

            JSONArray requestImages=new JSONArray();
            for (int i=0;i<images.size();i++){
                requestImages.put(images.get(i));
            }
            jsonObject.put("images",requestImages);

          //  JSONObject body=new JSONObject();


         //   body.put("images",requestImages);
         //   jsonObject.put("body",body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    private String getServiceId(String serviceName) {
        String id = null;
        if(serviceName.equalsIgnoreCase("car crash")){
            id="1";
        }
        if(serviceName.equalsIgnoreCase("car wash")){
            id="2";
        }

        if(serviceName.equalsIgnoreCase("car repair")){
            id="3";
        }

        if(serviceName.equalsIgnoreCase("bike repair") || serviceName.equalsIgnoreCase("byke repair")){
            id="4";
        }

        if(serviceName.equalsIgnoreCase("car handle alignment")){
            id="5";
        }

        if(serviceName.equalsIgnoreCase("car maintenance")){
            id="6";
        }

        return id;
    }
    public void saveRequest(){
        startProgress("Saving new request","Saved successfully");
        final UserSessionPreferences sessionPreferences=new UserSessionPreferences(context);
        RestClient restClient = new RestClient();
        String relativeUrl = "requests";
        Log.e("userID",sessionPreferences.getUserID());
        ByteArrayEntity entity = null;
        final GlobalClass globalClass = new GlobalClass();
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getRequestParams().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization",sessionPreferences.getToken()));
        Log.e("Token ",sessionPreferences.getToken());
        Log.e("Params",getRequestParams().toString());
        restClient.postWithHeader(context, relativeUrl,headers.toArray(new Header[headers.size()]), entity,"application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.d("gotres",  rawJsonResponse);

                JSONObject object=new JSONObject();
                try {
                 //   Log.d("gotres2",  response);
                    Log.d("gotres2",  rawJsonResponse);
                    object=new JSONObject(rawJsonResponse);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    if(statusCode==200 && object.getString("status").equalsIgnoreCase("Success")) {
                       Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                       closeProgress(true);
                       ProposalReceivedFragment fragment2 = new ProposalReceivedFragment();
                       FragmentManager fragmentManager = getFragmentManager();
                       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                       fragmentTransaction.replace(R.id.frame_layout, fragment2);
                       fragmentTransaction.addToBackStack(null);
                       fragmentTransaction.commit();
                   }
                    else if(statusCode==200 && object.getString("status").equalsIgnoreCase("Error")) {
                        Toast.makeText(context, object.getString("message"), Toast.LENGTH_SHORT).show();
                        closeProgress(false);
                        ProposalReceivedFragment fragment2 = new ProposalReceivedFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, fragment2);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                } catch (JSONException e) {
                    Log.e("error", e.toString());
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                Log.e("errorgot",  rawJsonData);
                if(rawJsonData!=null){
                    try {
                        Log.e("errorgot",  errorResponse.toString());
                        Toast.makeText(context,new JSONObject(rawJsonData).getString("message"),Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                //  Log.e("fail resposne",rawJsonData);
                closeProgress(false);
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

}
