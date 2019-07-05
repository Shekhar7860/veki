package com.onewayit.veki.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.onewayit.veki.Adapters.NotificationAdapter;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.HomeActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.utilities.AndroidVersion;
import com.onewayit.veki.utilities.GlobalClass;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements View.OnClickListener {
    private final String[] android_version_names = {
            "3M has accepted your offer ",
            "Speedy Auto Service Send You A Proposal ",
            "Cam's Auto Service  Send You A Proposal",

    };
    private final Integer[] android_image_urls = {R.drawable.m, R.drawable.check, R.drawable.speedy};
    ImageView back_button_home_activity;
    private View view;
    private Context context;
    private EditText mobile_number, name, email;
    private TextView submit;
    private GlobalClass globalClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        context = getActivity();
        initializeVariables();
        initViews();
        return view;
    }

    private void initViews() {
        RecyclerView recyclerView = view.findViewById(R.id.card_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<AndroidVersion> androidVersions = prepareData();
        NotificationAdapter adapter = new NotificationAdapter(context, androidVersions);
        recyclerView.setAdapter(adapter);

    }

    private ArrayList<AndroidVersion> prepareData() {

        ArrayList<AndroidVersion> android_version = new ArrayList<>();
        for (int i = 0; i < android_version_names.length; i++) {
            AndroidVersion androidVersion = new AndroidVersion();
            androidVersion.setAndroid_version_name(android_version_names[i]);
            androidVersion.setAndroid_image_url(android_image_urls[i]);
            android_version.add(androidVersion);
        }
        return android_version;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        back_button_home_activity.setOnClickListener(this);
        // (( HomeActivity) Objects.requireNonNull(getActivity())).setHeading("Notifications");
    }


    private void setOnClickListener() {
        submit.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit: {
                Toast.makeText(getActivity(), "test working",
                        Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.back_button_home_activity: {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
                break;
            }
        }
    }

    private void submit() {
        Intent intent = new Intent(context, HomeActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if (name.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Name", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (email.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Snackbar.make(view, "Please Enter a Valid Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile_number.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile_number.getText().toString().length() < 10) {
            Snackbar.make(view, "Please Enter a Valid Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
