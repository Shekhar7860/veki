package com.onewayit.veki.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.emailLogin.LoginResponse;
import com.onewayit.veki.api.apiResponse.forgot.ForgotUpdatePasswordResponse;
import com.onewayit.veki.fragment.LoginFragment;
import com.onewayit.veki.fragment.ProfileFragment;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ResetPasswordFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Context context;
    public Button emailbutton, mobilebutton;
    private TextView sign_up, submit;
    private ProgressBar progress_bar;
    private EditText email, password, phone, mobile_number;
    private GlobalClass globalClass;
    private String refreshedToken;
    private FrameLayout mobileLayout;

    private String countryCode = "91";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_resetpassword, container, false);
        initializeVariables();
        findViewById();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("gotdeviceToken",refreshedToken);
        setOnClickListener();


        return view;
    }


    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Reset Password");
    }

    private void findViewById() {
        mobileLayout = (FrameLayout) view.findViewById(R.id.mobileLayout);
        emailbutton = view.findViewById(R.id.emailbutton);
        mobilebutton = view.findViewById(R.id.mobilebutton);
        email = view.findViewById(R.id.email);
        emailbutton.setTextColor(Color.parseColor("#1E339E"));
        email.setVisibility(view.VISIBLE);
        mobileLayout.setVisibility(view.GONE);
        sign_up = view.findViewById(R.id.sign_up);
        mobile_number = view.findViewById(R.id.mobile_number);
        progress_bar = view.findViewById(R.id.progress_bar);
        password = view.findViewById(R.id.password);
        submit = view.findViewById(R.id.submit);
        //ccp = (CountryCodePicker) view.findViewById(R.id.ccp);
//        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
//            @Override
//            public void onCountrySelected(Country selectedCountry) {
//                countryCode = selectedCountry.getPhoneCode();
//                Log.d("country", selectedCountry.getPhoneCode() );
//            }
//        });
    }

    private void setOnClickListener() {
        sign_up.setOnClickListener(this);
        submit.setOnClickListener(this);
        emailbutton.setOnClickListener(this);
        mobilebutton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up:
                goToRegisterationFragment();
                break;
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
//                  if (validation()) {
                    resetPassword();
                    //   }
                } else {
                    network.noInternetAlertBox(getActivity(),false);
                }
                break;
            case R.id.emailbutton:
                email.setVisibility(view.VISIBLE);
                mobileLayout.setVisibility(view.GONE);
                emailbutton.setTextColor(Color.parseColor("#1E339E"));
                mobilebutton.setTextColor(Color.parseColor("#000000"));
                break;
            case R.id.mobilebutton:
                email.setVisibility(view.GONE);
                mobileLayout.setVisibility(view.VISIBLE);
                mobilebutton.setTextColor(Color.parseColor("#1E339E"));
                emailbutton.setTextColor(Color.parseColor("#000000"));
                break;
        }
    }


    private void goToRegisterationFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
    }


    ///////////login API//////////////
    private void resetPassword() {
        progress_bar.setVisibility(View.VISIBLE);
        globalClass.cancelProgressBarInterection(true,getActivity());
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ForgotUpdatePasswordResponse> call = apiService.forgotPasswordUpdate(getResetPasswordParameters());
        Log.e(" Login url", "" + call.request().url().toString());
        call.enqueue(new Callback<ForgotUpdatePasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotUpdatePasswordResponse> call, Response<ForgotUpdatePasswordResponse> response) {
                progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false,getActivity());
                if (response.code() == 200) {
                    Log.e("ForgotPasswordResponse", "" + globalClass.getJsonString(response.body()));
                    Snackbar.make(view, "Password Updated Successfully", Snackbar.LENGTH_LONG).show();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "ProfileFragment").addToBackStack(null).commit();

                } else if(response.code()==409||response.code()==404){
                    try {
                        JSONObject jsonObject = new JSONObject(globalClass.getErrorResponseBody(response.errorBody()));
                        if (jsonObject.has("error_message") && !jsonObject.getString("error_message").isEmpty()) {
                            try{
                                Snackbar.make(view, jsonObject.getString("error_message"), Snackbar.LENGTH_LONG).show();
                            }catch (IllegalArgumentException e){
                                e.printStackTrace();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    globalClass.retrofitNetworkErrorHandler(response.code(),view,context);
                }
            }

            @Override
            public void onFailure(Call<ForgotUpdatePasswordResponse> call, Throwable t) {
                progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false,getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try{
                        Snackbar.make(view, "Network Failure! Please Check Internet Connection", Snackbar.LENGTH_LONG).show();
                    }catch (IllegalArgumentException e){
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getResetPasswordParameters() {

        JsonObject jsonObject = new JsonObject();
        if( email.getText().toString().trim() == "") {
            jsonObject.addProperty("phone",  mobile_number.getText().toString().trim());
            jsonObject.addProperty("password", password.getText().toString().trim());
            jsonObject.addProperty("device_id", refreshedToken);
            jsonObject.addProperty("device_token", refreshedToken);
            jsonObject.addProperty("device_type", "android");
            jsonObject.addProperty("phone_code", AppConstant.PLUS + countryCode);
            Log.e("login parameters", jsonObject.toString());
        }
        else
        {
            jsonObject.addProperty("email", email.getText().toString().trim());
            jsonObject.addProperty("password", password.getText().toString().trim());
            jsonObject.addProperty("device_id", refreshedToken);
            jsonObject.addProperty("device_token", refreshedToken);
            jsonObject.addProperty("device_type", "android");
            Log.e("reset parameters", jsonObject.toString());
            //  jsonObject.addProperty("phone_code", AppConstant.PLUS + countryCode);
        }
        return jsonObject;
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if (mobile_number.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter a Valid Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

}

