package com.onewayit.veki.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog;
import com.onewayit.veki.Preferences.UserSessionPreferences;
import com.onewayit.veki.R;
import com.onewayit.veki.activities.LoginActivity;
import com.onewayit.veki.activities.MapsActivity;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.forgot.ForgotPasswordVerifyResponse;
import com.onewayit.veki.api.apiResponse.otp.VerifyOtpResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;
import com.onewayit.veki.utilities.Utility;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// import com.onewayit.veki.activities.AppConstant;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerificationCodeFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private View view;
    private AppCompatEditText etDigit1;
    private AppCompatEditText etDigit2;
    private AppCompatEditText etDigit3;
    private AppCompatEditText etDigit4;
    private AppCompatEditText etDigit5;
    private AppCompatEditText etDigit6;
    private AppCompatButton btnContinue;
    private AppCompatButton btnResendCode;
    private AppCompatTextView tvToolbarTitle;
    private AppCompatTextView tvCountDownTimer;
    public Button emailbutton, mobilebutton;
    private Context context;
    private AppCompatTextView tvToolbarBack;
    private GlobalClass globalClass;
    private EditText name, email, mobile_number, password, confirm_password;
    private TextView submit, login, phone;
    private CheckBox service_provider, customer;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private String strPhoneCode;
    private String strPhoneNumber;
    private String OtpCode;
    private String  userId;
    private CountDownTimer countDownTimer;
    private LinearLayout llContinue;
    private RelativeLayout rlResend;
    Spinner spinner_code;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String refreshedToken;
    private String userData,regEmail,regPhone;
    private FrameLayout mobileLayout;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_verification_code, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        setUpUI();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("deviceToken",refreshedToken);
        if (getArguments().getString(AppConstant.PhoneNumber) != null) {
            strPhoneNumber =  getArguments().getString(AppConstant.PhoneNumber);
            strPhoneCode = getArguments().getString(AppConstant.PhoneCode);
            //  userData = getArguments().getString("userData");

            tvToolbarBack.setText("< Edit Number");
            //   Log.d("dataaaa", userData);

            if( getArguments().getString("userData") != null)
            {
                userData = getArguments().getString("userData");
                try {
                    JSONObject jsonObj = new JSONObject(userData);
                    String data = jsonObj.getString("data");
                    JSONObject data2 = new JSONObject(data);
                    // String id = data.getString("id");
                    Log.d("myid", data2.getString("id"));
                    userId = data2.getString("id");
                    regEmail=data2.getString("email");
                    regPhone=data2.getString("phone");

                } catch (JSONException e) {
                    // TODO Auto-generated catch blfock
                    Log.e("erroe", e.toString());
                }
            }
            tvToolbarTitle.setText(AppConstant.PLUS + strPhoneCode + "" + strPhoneNumber + "");
        }
        //  String value = getArguments().getString("test");
        // Log.d("myTag", value);



        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Utility.log("onVerificationCompleted: " + credential);
                OtpCode = credential.getSmsCode();
                Log.d("myotyycode", OtpCode);
                Toast.makeText(getActivity(), credential.toString(),
                        Toast.LENGTH_LONG).show();
                //      signInWithPhoneAuthCredential(credential);
                //   pbVerify.setVisibility(View.GONE);
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
//                Toast.makeText(getApplicationContext(), verificationId,
//                        Toast.LENGTH_LONG).show();
                //  pbVerify.setVisibility(View.GONE);
                mVerificationId = verificationId;
                mResendToken = token;

            }
        };
        startPhoneNumberVerification(AppConstant.PLUS + strPhoneCode + strPhoneNumber + "");

        return view;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        startCounter();
    }


    private void startCounter() {
        if (countDownTimer != null)
            countDownTimer.cancel();

        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountDownTimer.setText("" + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                tvCountDownTimer.setText("");
                btnResendCode.setEnabled(true);
                setResendButtonEnableDisable();
            }

        };
        countDownTimer.start();
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d("mycode", credential.toString());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Utility.log("signInWithCredential:success");
                            //    pbVerify.setVisibility(View.GONE);
                            final FirebaseUser user = task.getResult().getUser();
                            Utility.showToast(getActivity(), user.getPhoneNumber() + " verified successfully");
                            //checking whether use has come from register or forgot screen
                            if(getArguments().getString("page").equals("register") ) {
                                callRegisterVerifyAPI();
                            }
                            else
                            {
                                callForgotPasswordAPI();

                            }

                        } else {
                            // Sign in failed, display a message and update the UI
                            //   pbVerify.setVisibility(View.GONE);
                            Utility.log("signInWithCredential:failure " + task.getException());
                            Utility.showToast(getActivity(), " Verification failed");
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

    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Verify OTP");
    }



    private void findViewById() {
        mobileLayout = (FrameLayout) view.findViewById(R.id.mobileLayout);
        emailbutton = view.findViewById(R.id.emailbutton);
        mobilebutton = view.findViewById(R.id.mobilebutton);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        emailbutton.setTextColor(Color.parseColor("#1E339E"));
        email.setVisibility(view.VISIBLE);
        mobileLayout.setVisibility(view.GONE);
        mobile_number = view.findViewById(R.id.mobile_number);
        password = view.findViewById(R.id.password);
        confirm_password = view.findViewById(R.id.confirm_password);
        submit = view.findViewById(R.id.submit);
        login = view.findViewById(R.id.login);
        phone = view.findViewById(R.id.mobile);
        service_provider = view.findViewById(R.id.service_provider);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(getActivity());
        customer = view.findViewById(R.id.customer);
       // ccp = (CountryCodePicker) view.findViewById(R.id.ccp);
//        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
//            @Override
//            public void onCountrySelected(com.rilixtech.Country selectedCountry) {
//                countryCode = selectedCountry.getPhoneCode();
//                Log.d("country", selectedCountry.getPhoneCode() );
//            }
//        });
    }

    private void setOnClickListener() {
        //  submit.setOnClickListener(this);
        // login.setOnClickListener(this);
        //    phone.setOnClickListener(this);
        emailbutton.setOnClickListener(this);
        mobilebutton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        //   registration();
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
            case R.id.emailbutton:
                email.setVisibility(view.VISIBLE);
                mobileLayout.setVisibility(view.GONE);
                emailbutton.setTextColor(getResources().getColor(R.color.white));
                mobilebutton.setTextColor(getResources().getColor(R.color.grey));
                break;
            case R.id.mobilebutton:
                email.setVisibility(view.GONE);
                mobileLayout.setVisibility(view.VISIBLE);
                emailbutton.setTextColor(getResources().getColor(R.color.grey));
                mobilebutton.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    ///////////Registration API//////////////






    ///////////Parameters for login API//////////////

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

    private void setResendButtonEnableDisable() {
        if (btnResendCode.isEnabled()) {
            rlResend.setBackgroundResource(R.drawable.rectangle_circular_ends);
            btnResendCode.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        } else {
            rlResend.setBackgroundResource(R.drawable.rectangle_circular_ends);
            btnResendCode.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        }
    }
    private void setUpUI() {
        rlResend = view.findViewById(R.id.rlResend);
        llContinue = view.findViewById(R.id.llContinue);
        llContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnContinue.isClickable())
                    btnContinue.performClick();
            }
        });
        // pbVerify = findViewById(R.id.pbVerify);

        btnContinue = view.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getArguments().getString(AppConstant.PhoneNumber) != "")
                {
                    //Utility.hideKeyBoardFromView(getActivity());
                    if (validate()) {
                        if (!TextUtils.isEmpty(mVerificationId)) {
                            verifyPhoneNumberWithCode(mVerificationId,
                                    etDigit1.getText().toString().trim() +
                                            etDigit2.getText().toString().trim() +
                                            etDigit3.getText().toString().trim() +
                                            etDigit4.getText().toString().trim() +
                                            etDigit5.getText().toString().trim() +
                                            etDigit6.getText().toString().trim());
                        } else {
                            Utility.showToast(getActivity(), "Verification id not received");
                        }
                    }
                    Log.d("verifiedemail", "this is mobile ");
                }
                else
                {
                    Log.d("verifiedemail", "this is email");
                    callForgotVerifyAPIEmail();
                }

            }
        });

        btnResendCode = view.findViewById(R.id.btnResendCode);
        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Utility.hideKeyBoardFromView(getActivity()));
                if (mResendToken != null)
                    resendVerificationCode(AppConstant.PLUS + strPhoneCode + strPhoneNumber, mResendToken);
                else {
                    Utility.showToast(getActivity(), "Resend token null");
                    //  onBackPressed();
                }
            }
        });


        tvToolbarBack = view.findViewById(R.id.tvToolbarBack);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        tvCountDownTimer = view.findViewById(R.id.tvCountDownTimer);

        etDigit1 = view.findViewById(R.id.etDigit1);
        etDigit2 = view.findViewById(R.id.etDigit2);
        etDigit3 = view.findViewById(R.id.etDigit3);
        etDigit4 = view.findViewById(R.id.etDigit4);
        etDigit5 = view.findViewById(R.id.etDigit5);
        etDigit6 = view.findViewById(R.id.etDigit6);

        setButtonContinueClickbleOrNot();
        tvToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  onBackPressed();

            }
        });
        etDigit1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit2.requestFocus();
                }
            }
        });
        etDigit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit3.requestFocus();
                } else {
                    etDigit1.requestFocus();
                }
            }
        });
        etDigit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit4.requestFocus();
                } else {
                    etDigit2.requestFocus();
                }
            }
        });
        etDigit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit5.requestFocus();
                } else {
                    etDigit3.requestFocus();
                }
            }
        });
        etDigit5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                    etDigit6.requestFocus();
                } else {
                    etDigit4.requestFocus();
                }
            }
        });
        etDigit6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setButtonContinueClickbleOrNot();
                if (editable.toString().length() == 1) {
                } else {
                    etDigit5.requestFocus();
                }
            }
        });

        etDigit1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                } else {
                    if (etDigit1.getText().toString().trim().length() == 1) {
                        etDigit2.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit2.getText().toString().trim().length() == 0)
                        etDigit1.requestFocus();
                } else {
                    if (etDigit2.getText().toString().trim().length() == 1) {
                        etDigit3.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit3.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit3.getText().toString().trim().length() == 0)
                        etDigit2.requestFocus();
                } else {
                    if (etDigit3.getText().toString().trim().length() == 1) {
                        etDigit4.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit4.getText().toString().trim().length() == 0)
                        etDigit3.requestFocus();
                } else {
                    if (etDigit4.getText().toString().trim().length() == 1) {
                        etDigit5.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit5.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit5.getText().toString().trim().length() == 0)
                        etDigit4.requestFocus();
                } else {
                    if (etDigit5.getText().toString().trim().length() == 1) {
                        etDigit6.requestFocus();
                    }
                }
                return false;
            }
        });
        etDigit6.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (etDigit6.getText().toString().trim().length() == 0)
                        etDigit5.requestFocus();
                }
                return false;
            }
        });

    }

    private boolean validate() {
        if (TextUtils.isEmpty(etDigit1.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit2.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit3.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit4.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit5.getText().toString().trim())) {
            return false;
        } else if (TextUtils.isEmpty(etDigit6.getText().toString().trim())) {
            return false;
        }
        return true;
    }

    private void setButtonContinueClickbleOrNot() {
        if (!validate()) {
            llContinue.setAlpha(.5f);
            btnContinue.setClickable(false);
        } else {
            llContinue.setAlpha(1.0f);
            btnContinue.setClickable(true);
        }
    }


    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        //   String code = phoneAuthCredential.getSmsCode();
        //  Log.d("mycodeotp", code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        startCounter();
        btnResendCode.setEnabled(false);
        setResendButtonEnableDisable();
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

    // Verify OTP API
    private void callRegisterVerifyAPI(){
        //   globalClass.cancelProgressBarInterection(true, this);
        startProgress("Verifying data..","Success.");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<VerifyOtpResponse> call = apiService.verifyOtp(getRegisterVerfifyParameters());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    closeProgress(true);
                    Log.e("RegistrationRESPONSE", "" + response.body());
                    //  Log.e("TAG", "response 33: "+new Gson().toJson(response.body()) );
                    Log.e("RegistrationOTP", "" + globalClass.getJsonString(response.body()));
                    UserSessionPreferences sessionPreferences=new UserSessionPreferences(context);
                    sessionPreferences.setUserID(userId);
                    sessionPreferences.setEmailId(regEmail);
                    sessionPreferences.setMobile(regPhone);
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(globalClass.getJsonString(response.body()));
                        String userData = jsonObj.getString("user");
                        JSONObject sessionData = new JSONObject(userData);
                        String storageData = sessionData.getString("login");
                        JSONObject myStorageData = new JSONObject(storageData);
                        sessionPreferences.setToken(myStorageData.getString("token"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Snackbar.make(view, "OTP Verified Successfully", Snackbar.LENGTH_LONG).show();
                    callDelay();
                } else {
                    Log.e("errorret", response.toString());
                    closeProgress(false);
                    Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG).show();
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, getActivity());
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                closeProgress(false);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getRegisterVerfifyParameters() {
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        Integer otp2 = pref.getInt("otp", 0);
        Log.d("MYINT", "value: " + otp2);
        JsonObject jsonObject = new JsonObject();
        //  String id =  getIntent().getStringExtra("userdata");
        jsonObject.addProperty("id", userId);
        jsonObject.addProperty("otp", otp2);
        jsonObject.addProperty("device_id", refreshedToken);
//        if (service_provider.isChecked()) {
//            jsonObject.addProperty("role", "3");
//        } else {
//            jsonObject.addProperty("role", "2");
//        }
        jsonObject.addProperty("device_token", refreshedToken);
        jsonObject.addProperty("device_type", "android");
        //  jsonObject.addProperty("c_password", confirm_password.getText().toString().trim());
        Log.e("verify parameters", jsonObject.toString());

        return jsonObject;

    }

    private void callForgotPasswordAPI(){
        //   globalClass.cancelProgressBarInterection(true, this);
        startProgress("Verifying data..","Success");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ForgotPasswordVerifyResponse> call = apiService.forgotPasswordVerify(getForgotPasswordParameters());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<ForgotPasswordVerifyResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordVerifyResponse> call, Response<ForgotPasswordVerifyResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    closeProgress(true);
                    Log.e("forgotPasswordAPI", "" + globalClass.getJsonString(response.body()));
                    Snackbar.make(view, "OTP Verified Successfully", Snackbar.LENGTH_LONG).show();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ResetPasswordFragment(), "ResetPassword").addToBackStack(null).commit();
                    // Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "ProfileFragment").addToBackStack(null).commit();


                } else {
                    closeProgress(false);
                    Log.e("errorret", response.toString());
                    Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG).show();
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, getActivity());
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordVerifyResponse> call, Throwable t) {
                // progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParameters() {
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        Integer otp2 = pref.getInt("otp", 0);
        Log.d("MYINT", "value: " + otp2);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("phone", phone.getText().toString().trim());
        jsonObject.addProperty("otp",getArguments().getInt("otp") );
        jsonObject.addProperty("phone_code", (spinner_code.getSelectedItem().toString()).substring(1,(spinner_code.getSelectedItem().toString()).indexOf("(")));
        Log.e("forgotparameters", jsonObject.toString());

        return jsonObject;

    }

    private void callForgotVerifyAPIEmail(){
        //   globalClass.cancelProgressBarInterection(true, this);
        startProgress("Verifying data..","Success");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ForgotPasswordVerifyResponse> call = apiService.forgotPasswordVerify(getForgotPasswordParametersEmail());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<ForgotPasswordVerifyResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordVerifyResponse> call, Response<ForgotPasswordVerifyResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    closeProgress(true);
                    Log.e("forgotPasswordAPI", "" + globalClass.getJsonString(response.body()));
                    Snackbar.make(view, "OTP Verified Successfully", Snackbar.LENGTH_LONG).show();
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ResetPasswordFragment(), "ResetPassword").addToBackStack(null).commit();
                    // Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ProfileFragment(), "ProfileFragment").addToBackStack(null).commit();


                } else {
                    closeProgress(false);
                    Log.e("errorret", response.toString());
                    Snackbar.make(view, response.toString(), Snackbar.LENGTH_LONG).show();
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, getActivity());
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordVerifyResponse> call, Throwable t) {
                // progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, getActivity());
                Log.e("retrofit error", "" + t.getMessage());
                if (t instanceof IOException) {
                    try {

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    @SuppressLint("HardwareIds")
    private JsonObject getForgotPasswordParametersEmail() {
        SharedPreferences pref = getActivity().getSharedPreferences("MY_PREFERENCES", Activity.MODE_PRIVATE);
        Integer otp2 = pref.getInt("otp", 0);
        Log.d("MYINT", "value: " + otp2);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email.getText().toString().trim());
        jsonObject.addProperty("otp",getArguments().getInt("otp") );
        //  jsonObject.addProperty("phone_code", "+91");
        Log.e("forgotparametersEmail", jsonObject.toString());

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

