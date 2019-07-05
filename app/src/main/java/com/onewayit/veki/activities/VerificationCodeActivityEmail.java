package com.onewayit.veki.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.onewayit.veki.R;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.Mail;
import com.onewayit.veki.utilities.Utility;

import java.util.Random;


public class VerificationCodeActivityEmail extends AppCompatActivity {

    private AppCompatActivity mActivity = VerificationCodeActivityEmail.this;
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
    private ProgressBar pbVerify;
    private String strPhoneCode;
    private String strEmail;
    private View view;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private CountDownTimer countDownTimer;

    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code_email);
        setUpUI();
        setUpToolBar();
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra(AppConstant.PhoneCode)) {
                strPhoneCode = getIntent().getStringExtra(AppConstant.PhoneCode);
            }
            if (getIntent().hasExtra("email")) {
                strEmail = getIntent().getStringExtra("email");
            }
            tvToolbarBack.setText("< Edit Number");
            tvToolbarTitle.setText(" ");
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
                mResendToken = token;

            }
        };
//        startPhoneNumberVerification(AppConstant.PLUS + strPhoneCode + strPhoneNumber + "");


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
                            Utility.showToast(VerificationCodeActivityEmail.this, user.getPhoneNumber() + " verified successfully");
                            Intent intent = new Intent(VerificationCodeActivityEmail.this, HomeActivity.class);
                            startActivity(intent);
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
                            Utility.showToast(VerificationCodeActivityEmail.this, " Verification failed");
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
                    //     if (!TextUtils.isEmpty(mVerificationId)) {
                    SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
                    int Otp = prefs.getInt("Otp", 0);
                    String code = etDigit1.getText().toString().trim() +
                            etDigit2.getText().toString().trim() +
                            etDigit3.getText().toString().trim() +
                            etDigit4.getText().toString().trim() +
                            etDigit5.getText().toString().trim() +
                            etDigit6.getText().toString().trim();
                    Log.d("MYINT", "value: " + Otp);
                    Log.d("myacitivitycode", code);
                    if (Integer.parseInt(code) == Otp) {
                        Utility.showToast(VerificationCodeActivityEmail.this, "Verification Successful");
                        Intent intent = new Intent(VerificationCodeActivityEmail.this, ResetPasswordActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Utility.showToast(VerificationCodeActivityEmail.this, "Verification Unsuccessfull");
                    }
                }
            }
        });

        btnResendCode = findViewById(R.id.btnResendCode);
        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Log.d("CLICK", "working ");
                resendEmail(strEmail);
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
            }

        };
        countDownTimer.start();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // [START resend_verification]
    private void resendEmail(String email) {
        //  Log.d("testing", "working ");
        Mail m = new Mail("vekiappnew@gmail.com", "9646407363");
        String[] toArr = {email};
        m.setTo(toArr);
        m.setFrom("AKASH");
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        Log.d("testagaincode", "value: " + number);
        SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
        editor.putInt("Otp", number);
        editor.apply();
        m.setSubject("Your Subject");
        m.setBody("Six Digit Verification Code is" + " " + number);

        try {
            boolean i = m.send();
            if (i == true) {
                Toast.makeText(getApplicationContext(), "Email was sent successfully ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Email was not sent successfully ", Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e2) {
            // TODO Auto-generated catch block
            Log.d("enteredtext", e2.toString());
        }
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

