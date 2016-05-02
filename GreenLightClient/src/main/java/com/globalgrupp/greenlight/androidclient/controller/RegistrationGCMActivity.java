package com.globalgrupp.greenlight.androidclient.controller;

/**
 * Created by Lenovo on 19.01.2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.util.GCMRegistrationHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegistrationGCMActivity extends Activity {

    Button btnGCMRegister;
    Button btnAppShare;
    GoogleCloudMessaging gcm;
    Context context;
    String regId;

    public static final String REG_ID = "regId";
    private static final String APP_VERSION = "appVersion";

    static final String TAG = "Register Activity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = getApplicationContext();

        btnGCMRegister = (Button) findViewById(R.id.btnGCMRegister);
        btnGCMRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (TextUtils.isEmpty(regId)) {
                    GCMRegistrationHelper regHelper = new GCMRegistrationHelper(getApplicationContext());
                    regId = regHelper.registerGCM();
                    Log.d("RegisterActivity", "GCM RegId: " + regId);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Already Registered with GCM Server!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        btnAppShare = (Button) findViewById(R.id.btnAppShare);
        btnAppShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (TextUtils.isEmpty(regId)) {
                    Toast.makeText(getApplicationContext(), "RegId is empty!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(getApplicationContext(),
                            EventListActivity.class);
                    i.putExtra("regId", regId);
                    Log.d("RegisterActivity",
                            "onClick of Share: Before starting main activity.");
                    startActivity(i);
                    finish();
                    Log.d("RegisterActivity", "onClick of Share: After finish.");
                }
            }
        });
    }
}