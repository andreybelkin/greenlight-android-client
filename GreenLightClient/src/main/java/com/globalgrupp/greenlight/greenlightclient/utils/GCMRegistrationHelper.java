package com.globalgrupp.greenlight.greenlightclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.globalgrupp.greenlight.greenlightclient.controller.EventListActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Lenovo on 20.01.2016.
 */
public class GCMRegistrationHelper {
    GoogleCloudMessaging gcm;
    Context context;
    String regId;

    public static final String REG_ID = "regId";
    private static final String APP_VERSION = "appVersion";


    static final String TAG = "Register Activity";

    public GCMRegistrationHelper(Context context) {
        this.context = context;
    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(context);
        regId = getRegistrationId(context);

        if (TextUtils.isEmpty(regId)) {
            registerInBackground(context);
            Log.d("RegisterActivity",
                    "registerGCM - regId: "
                            + regId);
        }
        return regId;
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(
                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            GCMRegistrationHelper.registerInBackground(context);
            return "";
        }
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            GCMRegistrationHelper.registerInBackground(context);
            return "";
        }
        if (!prefs.getBoolean("IS_PUSH_ID_SENDED",false)){
            GCMRegistrationHelper.registerInBackground(context);
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("RegisterActivity",
                    "I never expected this! Going down, going down!" + e);
            throw new RuntimeException(e);
        }
    }

    private static void registerInBackground(Context context) {
        new AsyncTask<Context,Void,String>() {
            @Override
            protected String doInBackground(Context... params) {
                String msg = "";
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(params[0]);

                    String regId = gcm.register("364386966248");
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + regId);
                    msg = "Device registered, registration ID=" + regId;

                    storeRegistrationId(params[0], regId);

                    final SharedPreferences prefs = params[0].getSharedPreferences(EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                    int appVersion = getAppVersion(params[0]);
                    Log.i(TAG, "Saving regId on app version " + appVersion);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("IS_PUSH_ID_SENDED",sendRegId(regId));
                    editor.commit();



                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                } catch (Exception e){
                    Log.d("RegisterActivity", "Error: " +e.getMessage());
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }
        }.execute(context, null, null);
    }

    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = context.getSharedPreferences(
                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();
    }
    private static Boolean sendRegId(String regId) throws Exception {
        String urlString="http://192.168.1.33:8080/utils/savePushAppId/"+regId;
        URL url = new URL(urlString);
        // Send POST data request
        HttpURLConnection conn =(HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent","Mozilla/5.0");
        conn.setRequestProperty("Accept","*/*");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("charset", "utf-8");
        conn.setConnectTimeout(5000);

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        String str = regId.toString();
        byte[] data=str.getBytes("UTF-8");
        wr.write(data);
        wr.flush();
        wr.close();
        // Get the server response
        InputStream is; //todo conn.getResponseCode() for errors
        try{
            is= conn.getInputStream();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            is=conn.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }
            String q=sb.toString();
            return false;
        }

    }
}
