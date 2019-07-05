package com.onewayit.veki.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.onewayit.veki.R;
import com.onewayit.veki.utilities.AppConstant;
import com.onewayit.veki.utilities.Mail;

import java.util.Random;


public class ForgotPassword extends AppCompatActivity {
    final String username = "shekharshanky20@gmail.com";
    final String password = "9646407363";
    public EditText email;
    public TextView submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        initViews();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    private void initViews() {
        email = findViewById(R.id.email);
        submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("enteredtext", email.getText().toString().trim());
                // Log.d("emailValid", Boolean.toString(isValidEmail(email.getText().toString().trim())));
                //     Log.d("mobileValid", Boolean.toString(isValidMobile(email.getText().toString().trim())));
                // isValidEmail(email.getText().toString().trim())
                // checking whether user has typed mobile number or forgot password
                isValidMobile(email.getText().toString().trim());
                if (isValidMobile(email.getText().toString().trim())) {
                    Intent verificationIntent = new Intent(ForgotPassword.this, VerificationCodeActivity.class);
                    verificationIntent.putExtra(AppConstant.PhoneNumber, email.getText().toString().trim());
                    verificationIntent.putExtra(AppConstant.PhoneCode, "91");
                    verificationIntent.putExtra("activity", "forgot");
                    verificationIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(verificationIntent);
                    finish();
                } else {
                    Mail m = new Mail("vekiappnew@gmail.com", "9646407363");
                    String[] toArr = {email.getText().toString().trim()};
                    m.setTo(toArr);
                    m.setFrom("AKASH");
                    Random rnd = new Random();
                    int number = rnd.nextInt(999999);
                    Log.d("MYINT2", "value: " + number);
                    SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
                    editor.putInt("Otp", number);
                    editor.apply();
                    m.setSubject("Your Subject");
                    m.setBody("Six Digit Verification Code is" + " " + number);

                    try {
                        boolean i = m.send();
                        if (i == true) {
                            Toast.makeText(getApplicationContext(), "Email was sent successfully ", Toast.LENGTH_SHORT).show();
                            Intent verificationIntent = new Intent(ForgotPassword.this, VerificationCodeActivityEmail.class);
                            verificationIntent.putExtra("email", email.getText().toString().trim());
                            startActivity(verificationIntent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Email was not sent successfully ", Toast.LENGTH_SHORT).show();

                        }

                    } catch (Exception e2) {
                        // TODO Auto-generated catch block
                        Log.d("enteredtext", e2.toString());
                    }

                }
                Log.d("enteredtext", "email sent");
            }
        });


    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

}

