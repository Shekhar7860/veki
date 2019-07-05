package com.onewayit.veki.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.onewayit.veki.R;
import com.onewayit.veki.utilities.MultiSelectionSpinner;

public class AddUpdateServicesFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    ImageView back_button_home_activity;
    TextView tv_action, tv_cancel;
    Button btn_submit;
    MultiSelectionSpinner multiSelectionSpinner;
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
        view = inflater.inflate(R.layout.add_services, container, false);
        findViewById();
        setClickListener();
        setServiceDetails();
        return view;
    }

    private void setServiceDetails() {
        tv_action.setText(getArguments().getString("action"));
        btn_submit.setText(getArguments().getString("action"));
        String[] array = {"Car Wash", "Select multiple services", "Tyre Puncture", "Pollution Check", "Battery Problem", "Tube Replacement", "Engine Repair"};
        multiSelectionSpinner = view.findViewById(R.id.mySpinner);
        multiSelectionSpinner.setItems(array);
        multiSelectionSpinner.setSelection(new int[]{1});
    }

    private void setClickListener() {
        btn_submit.setOnClickListener(this);
        back_button_home_activity.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
    }

    private void findViewById() {
        btn_submit = view.findViewById(R.id.btn_submit);
        back_button_home_activity = view.findViewById(R.id.back_button_home_activity);
        tv_action = view.findViewById(R.id.tv_action);
        tv_cancel = view.findViewById(R.id.tv_cancel);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit: {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
            }
            case R.id.back_button_home_activity: {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
            }
            case R.id.tv_cancel:
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                break;
        }
    }
    // TODO: Rename method, update argument and hook method into UI event
}
