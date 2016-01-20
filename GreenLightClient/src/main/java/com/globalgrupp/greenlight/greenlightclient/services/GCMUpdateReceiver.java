package com.globalgrupp.greenlight.greenlightclient.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.globalgrupp.greenlight.greenlightclient.utils.GCMRegistrationHelper;


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
