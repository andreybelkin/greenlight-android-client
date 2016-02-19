package com.globalgrupp.greenlight.greenlightclient.classes;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by п on 21.12.2015.
 */
    // Class with extends AsyncTask class
    public class CreateEventOperation extends AsyncTask<Event, Void, Boolean> {

        // Required initialization

        //private final HttpClient Client = new AndroidHttpClient();
        private String Content;
        private String Error = null;
        String data ="";

    @Override
    protected Boolean doInBackground(Event... params) {
        /************ Make Post Call To Web Server ***********/
        BufferedReader reader=null;
        Log.i("doInBackground service ","doInBackground service ");
        // Send data
        try
        {
            String serverURL = "http://46.146.171.6:8080/event/createEvent";
            // Defined URL  where to send data
            JSONObject msg=new JSONObject();
            msg.put("message",params[0].getMessage());
            msg.put("latitude",params[0].getLatitude());
            msg.put("longitude",params[0].getLongitude());
            msg.put("audioId",params[0].getAudioId());
            msg.put("videoId",params[0].getVideoId());
            msg.put("photoId",params[0].getPhotoId());
            msg.put("streetName",params[0].getStreetName());
            msg.put("senderAppId",params[0].getSenderAppId());
            msg.put("socialType",params[0].getSocialType());
            msg.put("userName",params[0].getUserName());
            JSONArray array=new JSONArray(params[0].getPhotoIds());
            msg.put("photoIds",array);
            msg.put("createDate",params[0].getCreateDate().getTime());
            msg.put("uniqueGUID",params[0].getUniqueGUID());


            Log.i("message",msg.toString());
            URL url = new URL(serverURL);

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
                return false;

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

            // Append Server Response To Content String
            Content = sb.toString();
            return true;
        }
        catch(Exception ex)
        {
            Error = ex.getMessage();
            Log.d(ex.getMessage(),ex.getMessage());
            ex.printStackTrace();
            return false;
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
    }



    }

