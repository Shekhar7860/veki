package com.onewayit.veki.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog;
import com.onewayit.veki.GetterSetter.RequestsData;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.emailLogin.LoginResponse;
import com.onewayit.veki.api.apiResponse.emailLogin.User;
import com.onewayit.veki.api.apiResponse.nearByRequests.NearByRequestsResponse;
import com.onewayit.veki.fragment.MarkerDetailFragment;
import com.onewayit.veki.fragment.MyProposals;
import com.onewayit.veki.fragment.MyRequests;
import com.onewayit.veki.fragment.MyServices;
import com.onewayit.veki.fragment.NewRequestFragment;
import com.onewayit.veki.fragment.NotificationFragment;
import com.onewayit.veki.fragment.ProfileFragment;
import com.onewayit.veki.fragment.VerifyOtpFragment;
import com.onewayit.veki.utilities.GPSTracker;
import com.onewayit.veki.utilities.Geocoderlocation;
import com.onewayit.veki.utilities.GlobalClass;
import com.seatgeek.placesautocomplete.PlacesApi;
import com.seatgeek.placesautocomplete.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    TextView tv_help, view_details;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView iv_menu, filter, alert,iv_clear;
    CardView card1;
    RelativeLayout marker_details, rl_distance_bar,rl_searchLoc;
    boolean doubleBackToExitPressedOnce = false;
    private GoogleMap mMap;
    AutoCompleteTextView ac_location;
    private PlacesAutoCompleteAdapter mAdapter;
    HandlerThread mHandlerThread;
    Handler mThreadHandler;
    LatLng ac_latlng;
    Marker ac_marker;
    Double lat;
    Double lon;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;
    ArrayList<RequestsData> requestsList;

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViewIds();
        checkPermission();
        setOnClickListeners();
        setDistanceListener();
        setPlaceAdapter();
        checkExistingUser();
    }

    private void checkExistingUser() {
        UserSessionPreferences sessionPreferences=new UserSessionPreferences(this);

        if( sessionPreferences.getMobile()!=null && !sessionPreferences.getMobile().equalsIgnoreCase("")){
            navigationView.getMenu().findItem(R.id.signin).setTitle("Signout");
        }
        else{
            navigationView.getMenu().findItem(R.id.signin).setTitle("Signin");
        }
    }


    private void setPlaceAdapter() {
        mAdapter=new PlacesAutoCompleteAdapter(this, R.layout.auto_complete_listitem);
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


    private void setDistanceListener() {
        // get seekbar from view
        final CrystalRangeSeekbar rangeSeekbar = findViewById(R.id.rangeSeekbar3);

// get min and max text view
        final TextView tvMin = findViewById(R.id.tv_min1);
        final TextView tvMax = findViewById(R.id.tv_max1);
        final UserSessionPreferences userSessionPreferences=new UserSessionPreferences(this);

// set listener
        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvMin.setText(minValue + "-");
                tvMax.setText(maxValue + " km");
                userSessionPreferences.setDistanceStart(String.valueOf(minValue));
                userSessionPreferences.setDistanceEnd(String.valueOf(maxValue));
            }
        });

// set final value listener
        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                tvMin.setText(minValue + "-");
                tvMax.setText(maxValue + " km");
            }
        });

    }

    private void setOnClickListeners() {
        card1.setOnClickListener(this);
        tv_help.setOnClickListener(this);
        filter.setOnClickListener(this);
        iv_menu.setOnClickListener(this);
        alert.setOnClickListener(this);
        iv_clear.setOnClickListener(this);
        view_details.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawerLayout.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    drawerLayout.bringToFront();
                    drawerLayout.requestLayout();
                }
            });
        }
        requestsList=new ArrayList<>();
    }

    private void findViewIds() {
        navigationView = findViewById(R.id.nav_view);
        tv_help = findViewById(R.id.tv_help);
        drawerLayout = findViewById(R.id.drawer_layout);
        tv_help.setVisibility(View.VISIBLE);
        marker_details = findViewById(R.id.marker_details);
        rl_distance_bar = findViewById(R.id.rl_distance_bar);
        view_details = findViewById(R.id.view_details);
        filter = findViewById(R.id.filter);
        card1 = findViewById(R.id.card1);
        iv_menu = findViewById(R.id.menu);
        alert = findViewById(R.id.alert);
        ac_location=findViewById(R.id.ac_location);
        ac_location.setOnItemClickListener(this);
        rl_searchLoc=findViewById(R.id.rl_searchLoc);
        iv_clear=findViewById(R.id.iv_clear);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(this);
    }

    public void checkPermission() {

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);


            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UserSessionPreferences sessionPreferences=new UserSessionPreferences(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        GPSTracker gpsTracker=new GPSTracker(this);

        lat=gpsTracker.getLatitude();;
        lon=gpsTracker.getLongitude();
        sessionPreferences.setLat(String.valueOf(lat));
        sessionPreferences.setLon(String.valueOf(lon));
        addMarkers();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        getNearByRequests();
    }

    private void addMarkers() {
        LatLng latLng1 = new LatLng(30.7017322, 76.722553);
        LatLng latLng2 = new LatLng(30.69241, 76.7318493);
        LatLng latLng3 = new LatLng(30.7567589, 76.7164291);
        LatLng latLng4 = new LatLng(30.8050769, 76.7269012);
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView iv_user = marker.findViewById(R.id.iv_user);
        iv_user.setImageDrawable(getResources().getDrawable(R.drawable.user1));
        View marker1 = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView iv_user1 = marker1.findViewById(R.id.iv_user);
        iv_user1.setImageDrawable(getResources().getDrawable(R.drawable.user2));
        Marker m1 = mMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                .title("Mohali 7 Phase"));
        Marker m2 = mMap.addMarker(new MarkerOptions()
                .position(latLng2)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker1)))
                .title("11 Phase bypass"));
        Marker m3 = mMap.addMarker(new MarkerOptions()
                .position(latLng3)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                .title("11 Phase bypass"));
        Marker m4 = mMap.addMarker(new MarkerOptions()
                .position(latLng4)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker1)))
                .title("11 Phase bypass"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng1));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng3, 12.0f));

        for (int i=0;i<requestsList.size();i++){
            LatLng latLng=new LatLng(Double.parseDouble(requestsList.get(i).getLatitude()),Double.parseDouble(requestsList.get(i).getLongitude()));
            String location="Unknown location";
            if(requestsList.get(i).getAddress()==null){
                location=requestsList.get(i).getAddress();
            }
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                    .title(location));
        }

        // CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
        // mMap.setInfoWindowAdapter(customInfoWindow);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(marker.getTag()!="ac_marker") {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MarkerDetailFragment(), "markerFragment").addToBackStack(null).commit();
                    tv_help.setVisibility(View.GONE);
                    marker_details.setVisibility(View.GONE);
                }
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
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);

                } else {
                    this.finish();
                }
                return;
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTag()!="ac_marker") {
            marker.showInfoWindow();
            marker_details.setVisibility(View.VISIBLE);
            rl_distance_bar.setVisibility(View.GONE);
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        marker_details.setVisibility(View.GONE);
        rl_distance_bar.setVisibility(View.GONE);
        tv_help.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        marker_details.setVisibility(View.GONE);
        rl_searchLoc.setVisibility(View.GONE);
        tv_help.setVisibility(View.GONE);
        UserSessionPreferences sessionPreferences=new UserSessionPreferences(this);
        switch (v.getId()) {
            case R.id.view_details: {
                if(sessionPreferences.getMobile()!=null && sessionPreferences.getMobile()!="") {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MarkerDetailFragment(), "markerFragment").addToBackStack(null).commit();
                    card1.setVisibility(View.GONE);

                }
                else{
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.tv_help: {

                if(sessionPreferences.getMobile()==""|| sessionPreferences.getMobile()==null){
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new NewRequestFragment(), "myProfile").addToBackStack(null).commit();
                    card1.setVisibility(View.GONE);
                    tv_help.setVisibility(View.GONE);
                }
                break;
            }

            case R.id.menu: {
                drawerLayout.openDrawer(Gravity.START);
                break;
            }
            case R.id.alert: {
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new NotificationFragment(), "myProfile").addToBackStack(null).commit();
                card1.setVisibility(View.GONE);
                break;
            }
            case R.id.filter: {
                rl_distance_bar.setVisibility(View.VISIBLE);
                marker_details.setVisibility(View.VISIBLE);
                rl_searchLoc.setVisibility(View.VISIBLE);
                tv_help.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.iv_clear: {
                ac_location.setText("");
                ac_location.setVisibility(View.VISIBLE);
                ac_location.setFocusable(true);
                rl_searchLoc.setVisibility(View.VISIBLE);
                break;
            }
//            case R.id.btn_setLoc:{
//                btn_setLoc.setVisibility(View.VISIBLE);
//                tv_help.setVisibility(View.VISIBLE);
//                Intent intent=new Intent(this,MarkerDragActivity.class);
//                startActivity(intent);
//                break;
//            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        navigationView.getMenu().findItem(menuItem.getItemId()).setCheckable(true).setChecked(true);
        Intent intent;
        UserSessionPreferences sessionPreferences=new UserSessionPreferences(this);
        hideFields();
        switch (menuItem.getItemId()) {
            case R.id.Home: {
                //do somthing
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(this).finish();
                break;
            }
            case R.id.MyRequests: {
                if(sessionPreferences.getMobile()==""|| sessionPreferences.getMobile()==null){
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyRequests(), "requestFragment").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.MyServices: {
                if(sessionPreferences.getMobile()==""|| sessionPreferences.getMobile()==null){
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyServices(), "requestFragment").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.Proposal: {
                if(sessionPreferences.getMobile()=="" ||  sessionPreferences.getMobile()==null){
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new MyProposals(), "requestFragment").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.Profile: {
                if(sessionPreferences.getMobile()=="" || sessionPreferences.getMobile()==null){
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "myProfile").addToBackStack(null).commit();
                    //do somthing
                }
                break;
            }
            case R.id.Support: {
                if(sessionPreferences.getMobile()==""|| sessionPreferences.getMobile()==null){
                    intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("text/plain");
                    startActivity(emailIntent);
                    //do somthing
                }
                break;
            }
            case R.id.signout: {
                //do somthing
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(this).finish();
                break;
            }
            case R.id.signin: {
                //do somthing
                sessionPreferences=new UserSessionPreferences(this);
                if(sessionPreferences.getMobile()!=null && sessionPreferences.getMobile()!="" ){
                    sessionPreferences.setMobile("");
                }
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            }

        }
        //close navigation drawer
        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        getSupportFragmentManager().popBackStackImmediate();
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
           showFields();
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void showFields(){
        card1.setVisibility(View.VISIBLE);
        tv_help.setVisibility(View.VISIBLE);
        rl_searchLoc.setVisibility(View.VISIBLE);
    }

    public void hideFields(){
        marker_details.setVisibility(View.GONE);
        rl_searchLoc.setVisibility(View.GONE);
        card1.setVisibility(View.GONE);
        tv_help.setVisibility(View.GONE);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Get rid of our Place API Handlers
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
        }
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        hideKeyboard(this);
        if(i==0){
            Intent intent=new Intent(this,MarkerDragActivity.class);
            startActivityForResult(intent,1);
        }
        else {
            getLatLongFromLocation(mAdapter.resultList.get(i));
        }
    }

    public void setLocationOnMap(String locName,double Lat, double Long) {
        if(ac_marker!=null){
            ac_marker.remove();
        }
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView iv_user = marker.findViewById(R.id.iv_user);
        iv_user.setImageDrawable(getResources().getDrawable(R.drawable.veki_logo));
        ac_marker=mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Lat,Long))
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                .title(locName));
        ac_marker.setTag("ac_marker");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Lat,Long)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat,Long), 12.0f));
    }
    public void getLatLongFromLocation(String locName){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String result = null;
        // Toast.makeText(context,"hereee",Toast.LENGTH_SHORT).show();
        try {
            List addressList = geocoder.getFromLocationName(locName, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = (Address) addressList.get(0);
                StringBuilder sb = new StringBuilder();
                sb.append(address.getLatitude()).append("\n");
                sb.append(address.getLongitude()).append("\n");
                lat=address.getLatitude();
                lon=address.getLongitude();
                setLocationOnMap(locName,address.getLatitude(),address.getLongitude());
            }
        } catch (IOException e) {
            Log.e("", "Unable to connect to Geocoder", e);
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK){
            lat= Double.valueOf(data.getStringExtra("Lat"));
            lon= Double.valueOf(data.getStringExtra("Long"));
            setLocationOnMap("My Location",lat,lon);
        }
    }
    private void getNearByRequests() {
        startProgress();
        final GlobalClass globalClass=new GlobalClass();
        globalClass.cancelProgressBarInterection(true,this);
        RestClient restClient = new RestClient();
        final String relativeUrl = "requests/nearby";
        ByteArrayEntity entity = null;
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getNearByRequestParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e("Params :", getNearByRequestParameters().toString());
        restClient.getRequestJson(this, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("response", rawJsonResponse);
                globalClass.cancelProgressBarInterection(false,MapsActivity.this);
                try {
                    JSONObject object=new JSONObject(rawJsonResponse);
                    if(object.getString("status").equalsIgnoreCase("Success")){
                        JSONArray requests_data=object.getJSONArray("data");
                        if(requests_data.length()==0){
                            closeProgress(false);
                            Toast.makeText(MapsActivity.this,"No nearby request found",Toast.LENGTH_SHORT).show();
                        }
                        else if(requests_data.length()>0){
                            closeProgress(false);
                            for (int i=0;i<requests_data.length();i++){
                                RequestsData requestsData=new RequestsData();
                                JSONObject request=new JSONObject(String.valueOf(requests_data.get(i)));
                                requestsData.setLatitude(request.getString("latitude"));
                                requestsData.setLongitude(request.getString("longitude"));
                                requestsData.setName(request.getString("name"));
                                requestsData.setEmail(request.getString("email"));
                                requestsData.setPhone_code(request.getString("phone_code"));
                                requestsData.setPhone(request.getString("phone"));
                                requestsData.setAddress(request.getString("address"));
                                requestsData.setId(request.getString("id"));
                                requestsData.setMinServicePrice(request.getString("minServicePrice"));
                                requestsData.setPhoto(request.getString("photo"));
                                requestsList.add(requestsData);
                            }
                        }
                        if(requests_data.length()>0){
                            addMarkers();
                        }
                    }
                    else{
                        closeProgress(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                Toast.makeText(MapsActivity.this,"Error while fetching requests...",Toast.LENGTH_SHORT).show();
                globalClass.cancelProgressBarInterection(false,MapsActivity.this);
                closeProgress(false);
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
    private JSONObject getNearByRequestParameters() {
        JSONObject jsonObject=new JSONObject();
        UserSessionPreferences userSessionPreferences=new UserSessionPreferences(this);
        try {
            jsonObject.put("latitude",lat);
            jsonObject.put("longitude",lon);
            jsonObject.put("user_id",userSessionPreferences.getUserID());
            jsonObject.put("distance_start",userSessionPreferences.getDistanceStart());
            jsonObject.put("distance_end",userSessionPreferences.getDistanceEnd());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public void startProgress(){
        progressBarGIFDialog.setCancelable(false)

                .setTitleColor(R.color.colorPrimary) // Set Title Color (int only)

                .setLoadingGifID(R.drawable.loading) // Set Loading Gif

                .setDoneGifID(R.drawable.done) // Set Done Gif

                .setDoneTitle("Success") // Set Done Title


                .setLoadingTitle("Searching for requests...") // Set Loading Title

                .build();
    }
    public void closeProgress(boolean result){
        if(result) {
            progressBarGIFDialog.setDoneGifID(R.drawable.done);
            progressBarGIFDialog.clear();
        }
        else {
            progressBarGIFDialog.setDoneTitle("No request found...");
            progressBarGIFDialog.setDoneGifID(1);
            progressBarGIFDialog.clear();

        }
        // progressBarGIFDialog.
    }


}
