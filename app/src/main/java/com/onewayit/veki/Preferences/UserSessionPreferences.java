package com.onewayit.veki.Preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionPreferences {
    private static final String USER_PREFS = "PREFS";
    SharedPreferences mScreenNamePrefs;
    SharedPreferences.Editor mScreenNamePrefsEditor;
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private String user_id = "user_id";
    private String userFirstName = "docFirsName";
    private String userLastName = "docLastName";
    private String email_id = "email_id";
    private String user_Type = "user_Type";
    private String user_status = "user_status";
    private String user_reg_id = "user_reg_Id";
    private String profile_status = "profile_status";
    private String location = "location";
    private String mobile = "mobile";
    private String countryCode = "countryCode";

    private String distance_start = "distance_start";
    private String distance_end = "distance_end";
    private String lat = "lat";
    private String lon = "lon";
    private String token = "token";

    public UserSessionPreferences(Context context) {
        appSharedPrefs = context.getSharedPreferences(USER_PREFS,
                Activity.MODE_PRIVATE);
        prefsEditor = appSharedPrefs.edit();
        mScreenNamePrefs = context.getSharedPreferences("ScreenName", Activity.MODE_PRIVATE);
        mScreenNamePrefsEditor = mScreenNamePrefs.edit();
        mobile="";
    }

    public String getUserID() {
        return appSharedPrefs.getString(user_id, null);
    }

    public void setUserID(String userid) {
        prefsEditor.putString(user_id, userid).commit();
    }

    public String getUserFirstName() {
        return appSharedPrefs.getString(userFirstName, null);
    }

    public void setUserFirstName(String userFName) {
        prefsEditor.putString(userFirstName, userFName).commit();
    }

    public String getUserLastName() {
        return appSharedPrefs.getString(userLastName, null);
    }

    public void setUserLastName(String userLName) {
        prefsEditor.putString(userLastName, userLName).commit();
    }

    public String getUserType() {
        return appSharedPrefs.getString(user_Type, null);
    }

    public void setUserType(String type) {
        prefsEditor.putString(user_Type, type).commit();
    }

    public String getMobile() {
        return appSharedPrefs.getString(mobile, null);
    }

    public void setMobile(String mobile) {
        prefsEditor.putString(this.mobile, mobile).commit();
    }

    public String getCountryCode() {
        return appSharedPrefs.getString(countryCode, null);
    }

    public void setCountryCode(String countryCode) {
        prefsEditor.putString(this.countryCode, countryCode).commit();
    }



    public String getUserStatus() {
        return appSharedPrefs.getString(user_status, null);
    }

    public void setUserStatus(String status) {
        prefsEditor.putString(user_status, status).commit();
    }

    public String getEmailId() {
        return appSharedPrefs.getString(email_id, null);
    }

    public void setEmailId(String email) {
        prefsEditor.putString(email_id, email).commit();
    }

    public String getProfile_Status() {
        return appSharedPrefs.getString(profile_status, null);
    }

    public void setProfile_Status(String prof_stat) {
        prefsEditor.putString(profile_status, prof_stat).commit();
    }

    public String getUser_reg_id() {
        return appSharedPrefs.getString(user_reg_id, null);
    }

    public void setUser_reg_id(String user_reg) {
        prefsEditor.putString(user_reg_id, user_reg).commit();
    }

    public String getUser_Location() {
        return appSharedPrefs.getString(location, null);
    }

    public void setUser_Location(String user_loc) {
        prefsEditor.putString(location, user_loc).commit();
    }

    public String getLat() {
        return appSharedPrefs.getString(lat, null);
    }

    public void setLat(String lat) {
        prefsEditor.putString(this.lat, lat).commit();
    }

    public String getLon() {
        return appSharedPrefs.getString(lon, null);
    }

    public void setLon(String lon) {
        prefsEditor.putString(this.lon, lon).commit();
    }

    public String getDistanceStart() {
        return appSharedPrefs.getString(distance_start, null);
    }

    public void setDistanceStart(String distance_start) {
        prefsEditor.putString(this.distance_start, distance_start).commit();
    }
    public String getDistanceEnd() {
        return appSharedPrefs.getString(distance_end, null);
    }

    public void setDistanceEnd(String distance_end) {
        prefsEditor.putString(this.distance_end, distance_end).commit();
    }

    public String getToken() {
        return appSharedPrefs.getString(token, null);
    }

    public void setToken(String token) {
        prefsEditor.putString(this.token, token).commit();
    }



    public void clearPrefernces() {
        setEmailId("");
        setProfile_Status("");
        setUser_Location("");
        setUser_reg_id("");
        setUserFirstName("");
        setUserID("");
        setUserLastName("");
        setUserStatus("");
        setUserType("");
    }

}

