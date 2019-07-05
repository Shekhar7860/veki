package com.onewayit.veki.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.registration.FbRegistrationResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;
import com.onewayit.veki.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private View view;
    private Context context;
    private TextView login, register, request_otp, facebook;

    private EditText mobile_number;
    private GlobalClass globalClass;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    public String SmsCode;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    Spinner spinner_code;
    private String refreshedToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;
    CallbackManager callbackManager = CallbackManager.Factory.create();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        onBackPressed();
    }

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        (( LoginActivity) Objects.requireNonNull(getActivity())).setHeading("");
        FacebookSdk.sdkInitialize(getActivity());
    }

    private void findViewById() {
        login = view.findViewById(R.id.login);
        register = view.findViewById(R.id.register);
        request_otp = view.findViewById(R.id.request_otp);
        mobile_number = view.findViewById(R.id.mobile_number);
        facebook = view.findViewById(R.id.facebook);
        spinner_code=view.findViewById(R.id.spinner_code);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(getActivity());
    }

    private void setOnClickListener() {
        login.setOnClickListener(this);
        facebook.setOnClickListener(this);
        register.setOnClickListener(this);
        request_otp.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.facebook:
                LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //   Toast.makeText(getActivity(), "its working",
                        //       Toast.LENGTH_LONG).show();
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        try {
                                            registerLoginFb(object.getString("id"));
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
            case R.id.login:
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new EmailLoginFragment(), "EmailLoginFragment").addToBackStack(null).commit();
                break;
            case R.id.register:
                goToRegisterationFragment();
                break;
            case R.id.request_otp:
                startProgress("Sending OTP...","Code sent.");
                globalClass.cancelProgressBarInterection(true, getActivity());
                sendFirebaseOtp();
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        sendFirebaseOtp();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;

        }


    }

    private void sendFirebaseOtp() {
        Random rnd = new Random();
        Integer number = rnd.nextInt(99999) + 99999;
        sendVerificationCode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void goToRegisterationFragment(){
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new RegistrationFragment(), "RegistrationFragment").addToBackStack(null).commit();
    }

    private void onBackPressed() {
        Objects.requireNonNull(getView()).setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    Objects.requireNonNull(getActivity()).onBackPressed();
                    return true;
                }

                return false;
            }
        });
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if (mobile_number.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (mobile_number.getText().toString().length() < 10) {
            Snackbar.make(view, "Please Enter a Valid Mobile Number", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JSONObject getOtpParameters() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", mobile_number.getText().toString().trim());
            jsonObject.put("phone_code",(spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
            jsonObject.put("otp",SmsCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public void generateOtp(){

            RestClient restClient = new RestClient();
            String relativeUrl = "users/send/otp";
            ByteArrayEntity entity = null;
            final GlobalClass globalClass = new GlobalClass();
            //Log.e("password is=",encrptPass);
            try {
                entity = new ByteArrayEntity((getOtpParameters().toString()).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("Params", getOtpParameters().toString());
            restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                    Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
                    Log.e("Params", rawJsonResponse);
                    closeProgress(true);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                    try {
                        JSONObject object = new JSONObject(rawJsonResponse);
                        Bundle bundle=new Bundle();
                        bundle.putString("mobile_number", mobile_number.getText().toString());
                        bundle.putString("otp",""+ SmsCode);
                        bundle.putString("phone_code",(spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
                        bundle.putString("verificationid",""+ mVerificationId);
                        VerifyOtpFragment verifyOtpFragment = new VerifyOtpFragment();
                        verifyOtpFragment.setArguments(bundle);
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, verifyOtpFragment, "VerifyOtpFragment").addToBackStack(null).commit();
                    } catch (JSONException e) {

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONArray errorResponse) {
                    Toast.makeText(context,rawJsonData,Toast.LENGTH_SHORT).show();
                    closeProgress(false);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                }

                @Override
                protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    return null;
                }
            });
        }

        public void sendVerificationCode(){
            mAuth = FirebaseAuth.getInstance();

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    Utility.log("onVerificationCompleted: " + credential);
                    SmsCode = credential.getSmsCode();
                    generateOtp();
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    closeProgress(false);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                    Utility.log("onVerificationFailed" + e);
                    Snackbar.make(view, "Failed to send OTP", Snackbar.LENGTH_LONG).show();
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                    }
                    //   pbVerify.setVisibility(View.GONE);
                }

                @Override
                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                    Utility.log("onCodeSent: " + verificationId);
                    Utility.log("token: " + token);
                    mVerificationId = verificationId;
                    SmsCode=SmsCode;
                    mResendToken = token;
                }
            };
            startPhoneNumberVerification(AppConstant.PLUS + (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")) + mobile_number.getText().toString() + "");
        }
    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
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

  private void registerLoginFb(String id) {
            startProgress("Registering with facebook.","Registered successfully");
            globalClass.cancelProgressBarInterection(true, getActivity());
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<FbRegistrationResponse> call = apiService.fbRegisteration(getRegistrationFbParameters(id));
            Log.e("Registration parameters", "" + getRegistrationFbParameters(id));
            Log.e(" Registration url", "" + call.request().url().toString());
            call.enqueue(new Callback<FbRegistrationResponse>() {
      @Override
      public void onResponse(Call<FbRegistrationResponse> call, Response<FbRegistrationResponse> response) {
                        globalClass.cancelProgressBarInterection(false, getActivity());
                        if (response.code() == 200) {
                            closeProgress(true);
                            Log.e("RegistrationFb", "" + globalClass.getJsonString(response.body()));
                            Snackbar.make(view, "Registered Successfully", Snackbar.LENGTH_LONG).show();
                            callDelay();
                        } else {
                            closeProgress(false);
                              globalClass.retrofitNetworkErrorHandler(response.code(), view, context);
                            }
                      }
              @Override
      public void onFailure(Call<FbRegistrationResponse> call, Throwable t) {
                        //progress_bar.setVisibility(View.GONE);
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
@SuppressLint("HardwareIds")
  private JsonObject getRegistrationFbParameters(String id) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("facebook_id", id);
            jsonObject.addProperty("device_id", refreshedToken);
            jsonObject.addProperty("device_token", refreshedToken);
            jsonObject.addProperty("device_type", "android");
            Log.e("registration parameters", jsonObject.toString());
            return jsonObject;
          }
    private void callDelay() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), MapsActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
            }
        }, 3000);
    }

}

