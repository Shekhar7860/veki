package com.onewayit.veki.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.onewayit.veki.R;
import com.onewayit.veki.activities.MapsActivity;

import java.util.Objects;

public class SendProposalFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    com.spark.submitbutton.SubmitButton btn_submit;
    View view;
    ImageView back_button_home_activity;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.send_proposal, container, false);
        findViewById();
        setClickListener();
        return view;
    }

    private void setClickListener() {
        btn_submit.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);
    }

    private void findViewById() {
        btn_submit = view.findViewById(R.id.btn_submit);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit: {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getContext(), MapsActivity.class);
                        startActivity(intent);
                        Objects.requireNonNull(getActivity()).finish();
                    }
                }, 4000);
                break;
            }
            case R.id.back_button_home_activity: {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
            }


        }

    }

    // TODO: Rename method, update argument and hook method into UI event

}
