package com.globalgrupp.greenlight.greenlightclient.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.globalgrupp.greenlight.greenlightclient.controller.EventListActivity;
import com.globalgrupp.greenlight.greenlightclient.utils.GCMRegistrationHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Lenovo on 15.02.2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
                //start service
//                Intent intent = new Intent(this, ItemServiceManager.class);
                startService(context);
            }
            else {
                //stop service
//                Intent intent = new Intent(this, ItemServiceManager.class);
                stopService(context);
            }
        }
    }

    private void startService(Context context){

        SharedPreferences prefs = context.getSharedPreferences(
                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String queueString = prefs.getString("messageQueue","[]");
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Event>>() {
        }.getType();
        List<Event> queueResults=gson.fromJson(queueString,listType);

        Iterator<Event> iter = queueResults.iterator();
        while(iter.hasNext()){
            Event res=iter.next();
            try{
                Long audioId=new Long(0);
                if (res.getAudioPath()!=null){
                    CreateEventParams cep=new CreateEventParams();
                    cep.setURL(res.getAudioPath());
                    audioId=new UploadFileOperation().execute(cep).get();
                }
                res.setAudioId(audioId);


                List<Long> photoIds=new ArrayList<Long>();
                if (res.getPhotoPathList().size()>0){
                    for (int i=0;i<res.getPhotoPathList().size();i++){
                        CreateEventParams cep=new CreateEventParams();
                        cep.setURL(res.getPhotoPathList().get(i));
                        Long phId=new UploadFileOperation().execute(cep).get();
                        photoIds.add(phId);
                    }
                }
                res.setPhotoIds(photoIds);
                Long videoId=new Long(0);
                if (res.getVideoPath()!=null){
                    CreateEventParams cep=new CreateEventParams();
                    cep.setURL(res.getVideoPath());
                    videoId=new UploadFileOperation().execute(cep).get();
                }
                res.setVideoId(videoId);
                new CreateEventOperation().execute(res).get();
                iter.remove();
            }catch (Exception e){
                e.printStackTrace();
                break; //connection problem again?
            }
        }
        SharedPreferences.Editor prefsEditor = prefs.edit();
        String json = gson.toJson(queueResults);
        prefsEditor.putString("messageQueue", json);
        prefsEditor.commit();
    }
    private void stopService(Context context){

    }
}
