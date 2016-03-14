package com.globalgrupp.greenlight.androidclient.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.globalgrupp.greenlight.androidclient.util.GCMRegistrationHelper;


/**
 * Created by Lenovo on 20.01.2016.
 */
public class GCMUpdateReceiver extends BroadcastReceiver {
    Context currentContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        currentContext=context;
        GCMRegistrationHelper helper=new GCMRegistrationHelper(context);
        helper.registerGCM();
    }
}
