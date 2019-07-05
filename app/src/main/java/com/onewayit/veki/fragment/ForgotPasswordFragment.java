package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.onewayit.veki.R;
// import com.onewayit.veki.activities.AppConstant;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.forgot.ForgotPasswordResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Random;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private View view;
    public EditText email, mobile, otp;
    public TextView submit;
    private Context context;
    public Button emailbutton, mobilebutton;
    private ProgressBar progress_bar;
    private GlobalClass globalClass;
    private EditText name,  mobile_number, password, confirm_password;
    private TextView login, phone;
    private CheckBox service_provider, customer;
    private Integer number;
    private FrameLayout mobileLayout;
    Spinner spinner_code;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_forgotpassword, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        Random rnd = new Random();
        number = rnd.nextInt(99999) + 99999;
        // setOnCheckedChangeListener();


        return view;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();

        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Forgot Password");
    }



    private void findViewById() {
        mobileLayout = (FrameLayout) view.findViewById(R.id.mobileLayout);
       // ccp = (CountryCodePicker) view.findViewById(R.id.ccp);
//        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
//            @Override
//            public void onCountrySelected(Country selectedCountry) {
//                countryCode = selectedCountry.getPhoneCode();
//                Log.d("country", selectedCountry.getPhoneCode() );
//            }
//        });
        progress_bar = view.findViewById(R.id.progress_bar);
        //   name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        mobile = view.findViewById(R.id.mobile);
        //     otp = view.findViewById(R.id.otp);
        emailbutton = view.findViewById(R.id.emailbutton);
        mobilebutton = view.findViewById(R.id.mobilebutton);
        email.setVisibility(view.VISIBLE);
        mobileLayout.setVisibility(view.GONE);
        //      emailbutton.setBackgroundColor(Color.parseColor("#1E339E"));
        emailbutton.setTextColor(Color.parseColor("#1E339E"));
        //  mobile_number = view.findViewById(R.id.mobile_number);
        //  password = view.findViewById(R.id.password);
        //  confirm_password = view.findViewById(R.id.confirm_password);
        submit = view.findViewById(R.id.submit);
        spinner_code=view.findViewById(R.id.spinner_code);
        //  login = view.findViewById(R.id.login);
        // phone = view.findViewById(R.id.phone);
        // service_provider = view.findViewById(R.id.service_provider);
        //  customer = view.findViewById(R.id.customer);
    }

    private void setOnClickListener() {
        submit.setOnClickListener(this);
        emailbutton.setOnClickListener(this);
        mobilebutton.setOnClickListener(this);

        //   login.setOnClickListener(this);
        //  phone.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        registration();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
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

            case R.id.phone:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
                break;
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    ///////////Registration API//////////////
    private void registration() {

        // progress_bar.setVisibility(View.VISIBLE);
        isValidMobile(mobile.getText().toString().trim());
        callforgotPasswordAPI();
    }



    ///////////Parameters for login API//////////////
    private void callforgotPasswordAPI(){
        //   globalClass.cancelProgressBarInterection(true, this);
        progress_bar.setVisibility(View.VISIBLE);
        RestClient restClient = new RestClient();
        String relativeUrl = "users/reset/password";
        ByteArrayEntity entity = null;

        Log.e("Params: ", String.valueOf(getForgotPasswordParameters()));
        try {
            entity = new ByteArrayEntity((getForgotPasswordParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
                Log.e("Params", rawJsonResponse);
                progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                if(statusCode==200){
                    VerificationCodeFragment ldf = new VerificationCodeFragment();
                    if( isValidMobile(mobile.getText().toString().trim())) {
                        Bundle args = new Bundle();
                        args.putString(AppConstant.PhoneNumber, mobile.getText().toString().trim());
                        args.putString(AppConstant.PhoneCode, (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
                        args.putString("page", "forgot");
                        args.putInt("otp", number);
                        //  args.putString("userData",  globalClass.getJsonString(response.body()));
                        ldf.setArguments(args);
                    }
                    else
                    {
                        Bundle args = new Bundle();
                        args.putString(AppConstant.PhoneNumber, "");
                        args.putString(AppConstant.PhoneCode, "");
                        args.putString("page", "forgot");
                        args.putInt("otp", number);
                        //  args.putString("userData",  globalClass.getJsonString(response.body()));
                        ldf.setArguments(args);
                    }
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,  ldf).addToBackStack(null).commit();
                    Snackbar.make(view, "OTP Sent Successfully", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                if (statusCode == 400) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonData);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progress_bar.setVisibility(View.GONE);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParameters() {
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        Integer otp2 = pref.getInt("otp", 0);
        Log.d("MYINT", "value: " + otp2);
        JsonObject jsonObject = new JsonObject();
        //  String id =  getIntent().getStringExtra("userdata");
        jsonObject.addProperty("phone", mobile.getText().toString().trim());
        jsonObject.addProperty("otp", number );
        jsonObject.addProperty("email", email.getText().toString().trim() );
        if(   isValidMobile(mobile.getText().toString().trim())) {
            jsonObject.addProperty("phone_code", AppConstant.PLUS + (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
        }
        else
        {
            jsonObject.addProperty("phone_code", "");
        }

//        if (service_provider.isChecked()) {
//            jsonObject.addProperty("role", "3");
//        } else {
//            jsonObject.addProperty("role", "2");
//        }
        //  jsonObject.addProperty("email", refreshedToken);
        //  jsonObject.addProperty("device_type", "android");
        //  jsonObject.addProperty("c_password", confirm_password.getText().toString().trim());
        Log.e("verify parameters", jsonObject.toString());

        return jsonObject;

    }



    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.service_provider:
                if (b) {
                    customer.setChecked(false);
                }
                break;
            case R.id.customer:
                if (b) {
                    service_provider.setChecked(false);
                }
                break;

        }


    }
}

