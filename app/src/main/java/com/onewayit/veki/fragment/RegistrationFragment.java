package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
// import com.onewayit.veki.activities.AppConstant;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.registration.FbRegistrationResponse;
import com.onewayit.veki.api.apiResponse.registration.RegistrationResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private View view;
    private Context context;
    private GlobalClass globalClass;
    private EditText name, email, mobile_number, password, confirm_password;
    private TextView submit, login, phone,facebook;
    private CheckBox service_provider, customer;
    private Integer number;
    Spinner spinner_code;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;
    CallbackManager callbackManager = CallbackManager.Factory.create();
    private String refreshedToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registeration, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
       // setOnCheckedChangeListener();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Random rnd = new Random();
        number = rnd.nextInt(99999) + 99999;
        return view;
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Register");
    }

    private void setOnCheckedChangeListener() {
        service_provider.setOnCheckedChangeListener(this);
        customer.setOnCheckedChangeListener(this);
    }

    private void findViewById() {
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        mobile_number = view.findViewById(R.id.mobile_number);
        password = view.findViewById(R.id.password);
        confirm_password = view.findViewById(R.id.confirm_password);
        submit = view.findViewById(R.id.submit);
        login = view.findViewById(R.id.login);
        facebook = view.findViewById(R.id.facebook);
        phone = view.findViewById(R.id.phone);
        service_provider = view.findViewById(R.id.service_provider);
        customer = view.findViewById(R.id.customer);
        spinner_code=view.findViewById(R.id.spinner_code);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(getActivity());
    }

    private void setOnClickListener() {
        submit.setOnClickListener(this);
        login.setOnClickListener(this);
        phone.setOnClickListener(this);
        facebook.setOnClickListener(this);
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
            case R.id.login:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new EmailLoginFragment(), "EmailLoginFragment").addToBackStack(null).commit();
                break;
            case R.id.phone:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new LoginFragment(), "LoginFragment").addToBackStack(null).commit();
                break;

            case R.id.facebook:
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("result", loginResult.toString());
                        Toast.makeText(getActivity(), "its working",
                                Toast.LENGTH_LONG).show();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        try {
                                            Log.v("Loginresponse", object.toString());
                                            registerLoginFb(object.getString("id"));
                                            //  String name = object.getString("name");
                                            // Toast.makeText(getActivity(), name,
                                            //        Toast.LENGTH_LONG).show();
//                                            Intent intent = new Intent(getActivity(), HomeActivity.class);
//                                            intent.putExtra("MyClass", object.toString());
//                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        request.executeAsync();
                    }


                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(getActivity(), e.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
        }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
                super.onActivityResult(requestCode, resultCode, data);
            }
    ///////////Registration API//////////////
    private void registration() {

        startProgress("Registering....","Code sent..");
        globalClass.cancelProgressBarInterection(true, getActivity());

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RegistrationResponse> call = apiService.registerUser(getRegistrationParameters());
        Log.e("Registration parameters", "" + getRegistrationParameters());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                globalClass.cancelProgressBarInterection(false, getActivity());
                if (response.code() == 200) {
                    closeProgress(true);
                    //  Log.e("RegistrationRESPONSE", "" + response.body());
                    //  Log.e("TAG", "response 33: "+new Gson().toJson(response.body()) );
                    Log.e("Registration", "" + globalClass.getJsonString(response.body()));
                    // Intent intent = new Intent(context, HomeActivity.class);
                    //  startActivity(intent);
                    //  Objects.requireNonNull(getActivity()).finish();
                    VerificationCodeFragment ldf = new VerificationCodeFragment();
                    Bundle args = new Bundle();
                    args.putString(AppConstant.PhoneNumber,  mobile_number.getText().toString().trim());
                    args.putString(AppConstant.PhoneCode,  (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
                    args.putString("page",  "register");
                    args.putString("userData",  globalClass.getJsonString(response.body()));
                    ldf.setArguments(args);
                    Snackbar.make(view, "User Registered Successfully", Snackbar.LENGTH_LONG).show();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,  ldf).addToBackStack(null).commit();

                }
                else if(response.code()==400){
                    closeProgress(false);
                    Snackbar.make(view,"User already exists", Snackbar.LENGTH_LONG).show();
                }

                else {
                    closeProgress(false);
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, context);
                }
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                closeProgress(false);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {
                        Snackbar.make(view, "Network Failure! Please Check Internet Connection", Snackbar.LENGTH_LONG).show();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getRegistrationParameters() {

        Log.d("testagaincode", "value: " + number);
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        pref.edit().putInt("otp", number).apply();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name.getText().toString().trim());
        jsonObject.addProperty("email", email.getText().toString().trim());
        jsonObject.addProperty("phone", mobile_number.getText().toString().trim());
        jsonObject.addProperty("phone_code", AppConstant.PLUS + (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
        jsonObject.addProperty("otp", number);
        jsonObject.addProperty("password", password.getText().toString().trim());
        Log.e("registration parameters", jsonObject.toString());
        return jsonObject;
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
        } else if (password.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Password", Snackbar.LENGTH_LONG).show();
            return false;
        }
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
    private void registerLoginFb(String id) {
        final UserSessionPreferences sessionPreferences=new UserSessionPreferences(context);
        startProgress("Logging in..","Success.");
        globalClass.cancelProgressBarInterection(true, getActivity());
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<FbRegistrationResponse> call = apiService.fbRegisteration(getRegistrationFbParameters(id));
        Log.e("Registration parameters", "" + getRegistrationFbParameters(id));
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<FbRegistrationResponse>() {
            @Override
            public void onResponse(Call<FbRegistrationResponse> call, Response<FbRegistrationResponse> response) {
                //progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());

                if (response.code() == 200) {
                    closeProgress(true);
                    Log.e("RegistrationFb", "" + globalClass.getJsonString(response.body()));

                    Snackbar.make(view, "Registered Successfully", Snackbar.LENGTH_LONG).show();
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(globalClass.getJsonString(response.body()));
                        String data = jsonObj.getString("data");
                        JSONObject data2 = new JSONObject(data);
                        String storageData = data2.getString("login");
                        JSONObject myStorageData = new JSONObject(storageData);
                        sessionPreferences.setToken(myStorageData.getString("token"));
                        sessionPreferences.setEmailId(email.getText().toString());
                        sessionPreferences.setMobile(data2.getString("phone"));
                        sessionPreferences.setCountryCode(data2.getString("phone_code"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getContext(), MapsActivity.class);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).finish();
                  } else {
                    closeProgress(false);
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, context);
                }
            }

            @Override
            public void onFailure(Call<FbRegistrationResponse> call, Throwable t) {
                //progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                closeProgress(false);
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {
                        Snackbar.make(view, "Network Failure! Please Check Internet Connection", Snackbar.LENGTH_LONG).show();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getRegistrationFbParameters(String id) {

                        Log.d("testagaincode", "value: " + number);
                        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
                pref.edit().putInt("otp", number).apply();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("facebook_id", id);
                jsonObject.addProperty("email", email.getText().toString().trim());
                jsonObject.addProperty("phone", mobile_number.getText().toString().trim());
                jsonObject.addProperty("phone_code", AppConstant.PLUS + (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
                jsonObject.addProperty("device_id", refreshedToken);
                jsonObject.addProperty("device_token", refreshedToken);
                jsonObject.addProperty("device_type", "android");
                jsonObject.addProperty("password", password.getText().toString().trim());
                Log.e("registration parameters", jsonObject.toString());
                return jsonObject;
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
}
