package com.onewayit.veki.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
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
import com.onewayit.veki.api.apiResponse.emailLogin.LoginResponse;
import com.onewayit.veki.utilities.GlobalClass;
import com.onewayit.veki.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EmailLoginFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Context context;
    private TextView sign_up, submit,forgot;
    private EditText email, password;
    private GlobalClass globalClass;
    ProgressBarGIFDialog.Builder progressBarGIFDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_email_login, container, false);
        initializeVariables();
        findViewById();
        setOnClickListener();
        return view;
    }


    private void initializeVariables() {
        context = getActivity();
        globalClass = new GlobalClass();
        ((LoginActivity) Objects.requireNonNull(getActivity())).setHeading("Log In");
    }

    private void findViewById() {
        sign_up = view.findViewById(R.id.sign_up);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        forgot=view.findViewById(R.id.forgot);
        submit = view.findViewById(R.id.submit);
        progressBarGIFDialog= new ProgressBarGIFDialog.Builder(getActivity());
    }

    private void setOnClickListener() {
        sign_up.setOnClickListener(this);
        forgot.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up:
                goToRegisterationFragment();
                break;
            case R.id.forgot:
                gotoForgotPassword();
                break;
            case R.id.submit:
                Network network = new Network(context);
                if (network.isConnectedToInternet()) {
                    if (validation()) {
                        login();
                    }
                } else {
                    network.noInternetAlertBox(getActivity(), false);
                }
                break;
        }
    }

    private void goToRegisterationFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new RegistrationFragment(), "RegistrationFragment").addToBackStack(null).commit();
    }
    private void gotoForgotPassword(){
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new ForgotPasswordFragment(), "ForgotPasswordFragment").addToBackStack(null).commit();
    }

    ///////////login API//////////////
    private void login() {
        startProgress("Logging in..","Success");
        globalClass.cancelProgressBarInterection(true, getActivity());

        RestClient restClient = new RestClient();
        String relativeUrl = "users/login";
        ByteArrayEntity entity = null;
        Log.e("Params: ", String.valueOf(getLoginParameters()));
        try {
            entity = new ByteArrayEntity((getLoginParameters().toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restClient.postRequestJson(context, relativeUrl, entity, "application/json", new BaseJsonHttpResponseHandler<JSONArray>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONArray response) {
                Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
                Log.e("Params", rawJsonResponse);

                globalClass.cancelProgressBarInterection(false, getActivity());

                if(statusCode==200){
                    closeProgress(true);
                    try {
                        JSONObject jsonObject=new JSONObject(rawJsonResponse);
                        if(jsonObject.getString("status").equalsIgnoreCase("Success")) {
                            JSONObject data = new JSONObject(jsonObject.getString("data"));
                            UserSessionPreferences sessionPreferences = new UserSessionPreferences(context);
                            sessionPreferences.setEmailId(data.getString("email"));
                            sessionPreferences.setMobile(data.getString("phone"));
                            sessionPreferences.setUserID(data.getString("id"));
                            JSONObject tokenObject=new JSONObject(data.getString("login"));
                            sessionPreferences.setToken("Bearer "+tokenObject.getString("token"));
                            Intent intent = new Intent(getContext(), MapsActivity.class);
                            startActivity(intent);
                            Objects.requireNonNull(getActivity()).finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else if(statusCode==400){
                    closeProgress(false);
                    try {
                        JSONObject jsonObject=new JSONObject(rawJsonResponse);
                        Snackbar.make(view, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    closeProgress(false);
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
                    closeProgress(false);
                    globalClass.cancelProgressBarInterection(false, getActivity());
                    view.setClickable(false);
                }
            }

            @Override
            protected JSONArray parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });

    }

    ///////////Parameters for login API//////////////
    @SuppressLint("HardwareIds")
    private JsonObject getLoginParameters() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email.getText().toString().trim());
        jsonObject.addProperty("password", password.getText().toString().trim());
        jsonObject.addProperty("device_id", android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        jsonObject.addProperty("device_token", getToken());
        jsonObject.addProperty("device_type", "android");
        return jsonObject;
    }

    public String getToken(){
        String token = null;
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                    }
                });
        return token;
    }

    ///////////////Screen Validation, it will return true if user has fielded all required details/////////////
    private boolean validation() {
        if (email.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Snackbar.make(view, "Please Enter a Valid Email Id", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (password.getText().toString().isEmpty()) {
            Snackbar.make(view, "Please Enter Password", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
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
