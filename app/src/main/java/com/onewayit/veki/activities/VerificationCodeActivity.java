// Copyright (C) 2018 INTUZ.

// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
// the following conditions:

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
// ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.onewayit.veki.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.onewayit.veki.R;
import com.onewayit.veki.api.ApiClient;
import com.onewayit.veki.api.ApiInterface;
import com.onewayit.veki.api.apiResponse.otp.VerifyOtpResponse;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Utility;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class VerificationCodeActivity extends AppCompatActivity {

    String android_id;
    String refreshedToken;
    String device_type = "android";
    private AppCompatActivity mActivity = VerificationCodeActivity.this;
    private AppCompatEditText etDigit1;
    private AppCompatEditText etDigit2;
    private AppCompatEditText etDigit3;
    private AppCompatEditText etDigit4;
    private AppCompatEditText etDigit5;
    private AppCompatEditText etDigit6;
    private AppCompatButton btnContinue;
    private AppCompatButton btnResendCode;
    private AppCompatTextView tvToolbarBack;
    private AppCompatTextView tvToolbarTitle;
    private AppCompatTextView tvCountDownTimer;
    private LinearLayout llContinue;
    private RelativeLayout rlResend;
    private GlobalClass globalClass;
    private ProgressBar pbVerify;
    private String strPhoneCode;
    private String strPhoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private CountDownTimer countDownTimer;
    private View view;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        setUpUI();
        setUpToolBar();
        android_id = Settings.Secure.getString(VerificationCodeActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(AppConstant.PhoneCode)) {
                strPhoneCode = getIntent().getStringExtra(AppConstant.PhoneCode);
            }
            if (getIntent().hasExtra(AppConstant.PhoneNumber)) {
                strPhoneNumber = getIntent().getStringExtra(AppConstant.PhoneNumber);
            }
            tvToolbarBack.setText("< Edit Number");
            tvToolbarTitle.setText(AppConstant.PLUS + strPhoneCode + " " + strPhoneNumber + "");
            Log.d("mydata", getIntent().getStringExtra("userdata"));
        }

        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(AppConstant.PhoneCode)) {
                strPhoneCode = getIntent().getStringExtra(AppConstant.PhoneCode);
            }
            if (getIntent().hasExtra(AppConstant.PhoneNumber)) {
                strPhoneNumber = getIntent().getStringExtra(AppConstant.PhoneNumber);
            }
            tvToolbarBack.setText("< Edit Number");
            tvToolbarTitle.setText(AppConstant.PLUS + strPhoneCode + " " + strPhoneNumber + "");
        }

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Utility.log("onVerificationCompleted: " + credential);
                Toast.makeText(getApplicationContext(), credential.toString(),
                        Toast.LENGTH_LONG).show();
                signInWithPhoneAuthCredential(credential);
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
                pbVerify.setVisibility(View.GONE);
                mVerificationId = verificationId;
                Log.d("otp", mVerificationId);
                mResendToken = token;

            }
        };
        startPhoneNumberVerification(AppConstant.PLUS + strPhoneCode + strPhoneNumber + "");


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Utility.log("signInWithCredential:success");
                            //    pbVerify.setVisibility(View.GONE);
                            final FirebaseUser user = task.getResult().getUser();
                            Utility.showToast(VerificationCodeActivity.this, user.getPhoneNumber() + " verified successfully");
                            String lastActivity = "";
                            if (getIntent().hasExtra("activity")) {
                                lastActivity = getIntent().getStringExtra("activity");
                            }
                            Log.d("activity", lastActivity);
                            if (lastActivity.equals("register")) {
                                //  callRegisterVerifyAPI();
                                Intent intent = new Intent(VerificationCodeActivity.this, MapsActivity.class);
                                startActivity(intent);
                            } else {
                                Log.d("activity", "forgotone");
                                Intent intent = new Intent(VerificationCodeActivity.this, ResetPasswordActivity.class);
                                startActivity(intent);
                            }
                            finish();
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Intent intent = new Intent();
//                                    intent.putExtra("PHONE_NUMBER", user.getPhoneNumber());
//                                    setResult(1080, intent);
//                                    finish();
//                                }
//                            }, 500);
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //   pbVerify.setVisibility(View.GONE);
                            Utility.log("signInWithCredential:failure " + task.getException());
                            Utility.showToast(VerificationCodeActivity.this, " Verification failed");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            Intent intent = new Intent();
                            intent.putExtra("PHONE_NUMBER", "");
                            setResult(1080, intent);
                            finish();
                        }
                    }
                });
    }

    private void callRegisterVerifyAPI() {
        //   globalClass.cancelProgressBarInterection(true, this);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<VerifyOtpResponse> call = apiService.verifyOtp(getRegisterVerfifyParameters());
        Log.e(" Registration url", "" + call.request().url().toString());
        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                //  globalClass.cancelProgressBarInterection(false, this);
                if (response.code() == 200) {
                    //  Log.e("RegistrationRESPONSE", "" + response.body());
                    //  Log.e("TAG", "response 33: "+new Gson().toJson(response.body()) );
                    Log.e("RegistrationOTP", "" + globalClass.getJsonString(response.body()));
                    // Intent intent = new Intent(context, HomeActivity.class);
                    //  startActivity(intent);
                    //  Objects.requireNonNull(getActivity()).finish();
//                    Intent intent = new Intent(this, VerificationCodeActivity.class);
//                    intent.putExtra(com.onewayit.veki.fragment.AppConstant.PhoneNumber, mobile_number.getText().toString().trim());
//                    intent.putExtra("userdata", globalClass.getJsonString(response.body()));
//                    intent.putExtra(com.onewayit.veki.fragment.AppConstant.PhoneCode, "91");
//                    intent.putExtra("activity", "register");
//                    startActivity(intent);
//                    Objects.requireNonNull(getActivity()).finish();

                } else {
                    globalClass.retrofitNetworkErrorHandler(response.code(), view, VerificationCodeActivity.this);
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                // progress_bar.setVisibility(View.GONE);
                globalClass.cancelProgressBarInterection(false, VerificationCodeActivity.this);
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

        JsonObject jsonObject = new JsonObject();
        String id = getIntent().getStringExtra("userdata");
        jsonObject.addProperty("id", 10);
        jsonObject.addProperty("otp", mVerificationId);
        jsonObject.addProperty("device_id", android_id);
//        if (service_provider.isChecked()) {
//            jsonObject.addProperty("role", "3");
//        } else {
//            jsonObject.addProperty("role", "2");
//        }
        jsonObject.addProperty("device_token", refreshedToken);
        jsonObject.addProperty("device_type", device_type);
        //  jsonObject.addProperty("c_password", confirm_password.getText().toString().trim());
        Log.e("registration parameters", jsonObject.toString());

        return jsonObject;

    }

    private void setUpUI() {
        rlResend = findViewById(R.id.rlResend);
        llContinue = findViewById(R.id.llContinue);
        llContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnContinue.isClickable())
                    btnContinue.performClick();
            }
        });
        pbVerify = findViewById(R.id.pbVerify);

        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyBoardFromView(mActivity);
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
                        Utility.showToast(VerificationCodeActivity.this, "Verification id not received");
                    }
                }
            }
        });

        btnResendCode = findViewById(R.id.btnResendCode);
        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyBoardFromView(mActivity);
                if (mResendToken != null)
                    resendVerificationCode(AppConstant.PLUS + strPhoneCode + strPhoneNumber, mResendToken);
                else {
                    Utility.showToast(VerificationCodeActivity.this, "Resend token null");
                    onBackPressed();
                }
            }
        });


        tvToolbarBack = findViewById(R.id.tvToolbarBack);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvCountDownTimer = findViewById(R.id.tvCountDownTimer);

        etDigit1 = findViewById(R.id.etDigit1);
        etDigit2 = findViewById(R.id.etDigit2);
        etDigit3 = findViewById(R.id.etDigit3);
        etDigit4 = findViewById(R.id.etDigit4);
        etDigit5 = findViewById(R.id.etDigit5);
        etDigit6 = findViewById(R.id.etDigit6);

        setButtonContinueClickbleOrNot();
        tvToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

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
        } else return !TextUtils.isEmpty(etDigit6.getText().toString().trim());
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

    private void setUpToolBar() {
        Toolbar mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
    }


    private void signOut() {
        mAuth.signOut();
    }

    @Override
    public void onBackPressed() {
        signOut();
        Intent intent = new Intent(mActivity, PhoneNumberActivity.class);
        intent.putExtra("TITLE", getResources().getString(R.string.app_name));
        intent.putExtra("PHONE_NUMBER", "");
        startActivity(intent);
        finish();
        super.onBackPressed();
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
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

    @Override
    public void onStart() {
        super.onStart();
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        startCounter();
        btnResendCode.setEnabled(false);
        setResendButtonEnableDisable();
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        pbVerify.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }


    private void setResendButtonEnableDisable() {
        if (btnResendCode.isEnabled()) {
            rlResend.setBackgroundResource(R.drawable.rectangle_circular_ends);
            btnResendCode.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        } else {
            rlResend.setBackgroundResource(R.drawable.rectangle_circular_ends);
            btnResendCode.setTextColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        }
    }

}
