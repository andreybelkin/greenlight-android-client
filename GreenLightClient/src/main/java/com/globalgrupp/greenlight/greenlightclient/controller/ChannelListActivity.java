package com.globalgrupp.greenlight.greenlightclient.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.Channel;
import com.globalgrupp.greenlight.greenlightclient.classes.Event;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lenovo on 24.02.2016.
 */
public class ChannelListActivity extends ActionBarActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_list);
        refreshChannelList();
    }

    private void refreshChannelList() {

        new AsyncTask<Void, Void, List<Channel>>() {
            @Override
            protected List<Channel> doInBackground(Void... voids) {

                /************ Make Post Call To Web Server ***********/
                BufferedReader reader=null;
                Log.i("doInBackground service ","doInBackground service ");
                // Send data
                List<Channel> result=new ArrayList<Channel>();
                try
                {
                    String urlString="http://192.168.1.33:8080/channel/getBaseChannels";
                    JSONObject msg=new JSONObject();
                    URL url = new URL(urlString);
                    HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
//                    conn.setDoInput(true);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent","Mozilla/5.0");
                    conn.setRequestProperty("Accept","*/*");
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(20000);

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    String str = msg.toString();
                    byte[] data=str.getBytes("UTF-8");
                    wr.write(data);
                    wr.flush();
                    wr.close();
                    // Get the server response
                    InputStream is; //todo conn.getResponseCode() for errors
                    try{
                        is= conn.getInputStream();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        is=conn.getErrorStream();
                    }
                    reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        // Append server response in string
                        sb.append(line + "\n");
                    }


                    GsonBuilder builder=new GsonBuilder();
                    builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

                            return new Date(json.getAsJsonPrimitive().getAsLong());
                        }
                    });
                    Gson gson = builder.create();
                    Type listType = new TypeToken<ArrayList<Channel>>() {
                    }.getType();
                    result=gson.fromJson(sb.toString(),listType);

                }
                catch(Exception ex)
                {
                    Log.d(ex.getMessage(),ex.getMessage());
                    ex.printStackTrace();
                }
                finally
                {
                    try
                    {
                        reader.close();
                    }
                    catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return result;
            }
        }.execute();
    }
}
