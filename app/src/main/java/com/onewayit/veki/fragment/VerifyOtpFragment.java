package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.JsonObject;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.RestClient.RestClient;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.otp.VerifyOtpResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyOtpFragment extends Fragment implements View.OnClickListener {

    private View view;
    private Context context;
    private EditText et_otp;
    private TextView mobile_number,submit,tv_resend;
    private GlobalClass globalClass;
    private String mobileNumber = "", otp = "";
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String verificationId;
    private String mVerificationId;
    private String OtpCode,phone_code;
    private CountDownTimer countDownTimer;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_verify_otp, container, false);
        context = getActivity();
        initializeVariables();
        findViewById();
        setClickListeners();
        getOtpData();
        textWatcher();

        return view;
    }

    private void setClickListeners() {
        submit.setOnClickListener(this);
        tv_resend.setOnClickListener(this);
    }

    private void getOtpData() {
        mobileNumber =  getArguments().getString("mobile_number");
        otp = getArguments().getString("otp");
        verificationId = getArguments().getString("verificationid");
        phone_code = getArguments().getString("phone_code");
        mobile_number.setText(("+"+phone_code + mobileNumber));
    }

    private void initializeVariables() {
        globalClass = new GlobalClass();
        mobileNumber = Objects.requireNonNull(getArguments()).getString("mobile_number");
        otp = Objects.requireNonNull(getArguments()).getString("otp");
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Verify");
    }

    private void findViewById() {
        et_otp = view.findViewById(R.id.et_otp);
        submit=view.findViewById(R.id.submit);
        mobile_number = view.findViewById(R.id.mobile_number);
        tv_resend=view.findViewById(R.id.tv_resend);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(getActivity());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:{
                if(et_otp.getText().length()==6) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, et_otp.getText().toString());
                    //   String code = phoneAuthCredential.getSmsCode();
                    //  Log.d("mycodeotp", code);
                    signInWithPhoneAuthCredential(credential);
                }
                else{
                    Snackbar.make(view, "Please enter valid OTP", Snackbar.LENGTH_LONG).show();

                }

                break;
            }
            case R.id.tv_resend:
                resendFirebaseOTP();

                break;
        }
    }

    private void resendFirebaseOTP() {
            Random rnd = new Random();
            Integer number = rnd.nextInt(99999) + 99999;
            sendVerificationCode();
    }
    private void textWatcher() {
        et_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int length) {
                    if (et_otp.getText().length() == 6) {
                        Log.e("verfic id and otp",verificationId +" "+et_otp.getText().toString());
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, et_otp.getText().toString());
                        //   String code = phoneAuthCredential.getSmsCode();
                        //  Log.d("mycodeotp", code);
                        startProgress();
                        signInWithPhoneAuthCredential(credential);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d("mycode", credential.toString());
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Utility.log("signInWithCredential:success");
                            //    pbVerify.setVisibility(View.GONE);
                            final FirebaseUser user = task.getResult().getUser();
                            //checking whether use has come from register or forgot screen
                            verifyOtp();

                        } else {
                            // Sign in failed, display a message and update the UI
                            //   pbVerify.setVisibility(View.GONE);
                            Utility.log("signInWithCredential:failure " + task.getException());
                            closeProgress(false);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                            }
                            //  Intent intent = new Intent();
                            //  intent.putExtra("PHONE_NUMBER", "");
                            //  setResult(1080, intent);
                            //   finish();
                        }
                    }
                });
    }


    ///////////OTP API//////////////
    private void verifyOtp() {
        globalClass.cancelProgressBarInterection(true, getActivity());
        RestClient restClient = new RestClient();
        String relativeUrl = "users/verify/otp";
        ByteArrayEntity entity = null;
        final GlobalClass globalClass = new GlobalClass();
        //Log.e("password is=",encrptPass);
        try {
            entity = new ByteArrayEntity((getVerifyOtpParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.e("Params", getVerifyOtpParameters().toString());
        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Log.e("Params", rawJsonResponse);
                closeProgress(true);
                globalClass.cancelProgressBarInterection(false, getActivity());
                try {
                    JSONObject object = new JSONObject(rawJsonResponse);
                    if(object.getString("status").equalsIgnoreCase("Success")){
                        UserSessionPreferences userSessionPreferences=new UserSessionPreferences(context);
                        JSONObject jsonObject=new JSONObject(object.getString("user"));
                        userSessionPreferences.setCountryCode(jsonObject.getString("phone_code"));
                        userSessionPreferences.setUserID(jsonObject.getString("id"));
                        userSessionPreferences.setMobile(jsonObject.getString("phone"));
                        JSONObject tokenObject=new JSONObject(jsonObject.getString("login"));
                        userSessionPreferences.setToken("Bearer "+tokenObject.getString("token"));
                        callDelay();
                    }
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


    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getVerifyOtpParameters() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("phone", "" + mobileNumber);
        jsonObject.addProperty("phone_code", "91");
        jsonObject.addProperty("otp", "" + otp);
        jsonObject.addProperty("device_id", android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        jsonObject.addProperty("device_token", "43523");
        jsonObject.addProperty("device_type", "android");
        Log.e("verify Otp parameters", jsonObject.toString());
        return jsonObject;
    }
    public void sendVerificationCode(){
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Utility.log("onVerificationCompleted: " + credential);
                OtpCode = credential.getSmsCode();
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Utility.log("onVerificationFailed" + e);
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
                mResendToken = token;
               }
        };
        startPhoneNumberVerification(AppConstant.PLUS + "91" + mobileNumber + "");
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        tv_resend.setEnabled(false);
        tv_resend.setTextColor(getResources().getColor(R.color.gray));
        startCounter();
    }
    private void startCounter() {
        if (countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {

                tv_resend.setEnabled(true);
                tv_resend.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }

        };
        countDownTimer.start();
    }
    public void startProgress(){
        progressBarGIFDialog.setCancelable(false)

                .setTitleColor(R.color.colorPrimary) // Set Title Color (int only)

                .setLoadingGifID(R.drawable.loading) // Set Loading Gif

                .setDoneGifID(R.drawable.done) // Set Done Gif

                .setDoneTitle("Verified successfully") // Set Done Title

                .setLoadingTitle("Verifying...") // Set Loading Title

                .build();
    }
    public void closeProgress(boolean result){
        if(result) {
            progressBarGIFDialog.setDoneGifID(R.drawable.done);
            progressBarGIFDialog.clear();
        }
        else {
            progressBarGIFDialog.setDoneTitle("Invalid OTP.");
            progressBarGIFDialog.setDoneGifID(R.drawable.cancel_progress);
            progressBarGIFDialog.clear();
        }
        // progressBarGIFDialog.
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
