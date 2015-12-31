package com.globalgrupp.greenlight.greenlightclient.classes;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
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
    public class CreateEventOperation extends AsyncTask<CreateEventParams, Void, Void> {

        // Required initialization

        //private final HttpClient Client = new AndroidHttpClient();
        private String Content;
        private String Error = null;
        String data ="";

    @Override
    protected Void doInBackground(CreateEventParams... params) {
        /************ Make Post Call To Web Server ***********/
        BufferedReader reader=null;
        Log.i("doInBackground service ","doInBackground service ");
        // Send data
        try
        {
            // Defined URL  where to send data
            JSONObject msg=new JSONObject();
            msg.put("message",params[0].getMessage());
            msg.put("latitude",params[0].getLatitude());
            msg.put("longitude",params[0].getLongitude());
            msg.put("audioId",params[0].getAudioId());
            msg.toString();
            Log.i("message",msg.toString());
            URL url = new URL(params[0].getURL());

            // Send POST data request

            HttpURLConnection conn =(HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent","Mozilla/5.0");
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("charset", "utf-8");

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

            // Append Server Response To Content String
            Content = sb.toString();
        }
        catch(Exception ex)
        {
            Error = ex.getMessage();
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
        return null;
    }

    protected void onPreExecute() {
            try{
                Log.i("pre execute service ","pre execute service ");
                // Set Request parameter

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d(e.getMessage(),e.getMessage());
                e.printStackTrace();
            }
        }

        protected void onPostExecute(Void unused) {

            if (Error != null) {

            } else {


                String OutputData = "";
                JSONObject jsonResponse;

                try {

                    /****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
                    jsonResponse = new JSONObject(Content);

                    /***** Returns the value mapped by name if it exists and is a JSONArray. ***/
                    /*******  Returns null otherwise.  *******/
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("Android");

                    /*********** Process each JSON Node ************/

                    int lengthJsonArr = jsonMainNode.length();

                    for(int i=0; i < lengthJsonArr; i++)
                    {
                        /****** Get Object for each JSON node.***********/
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        //todo result
                        Log.i("getsome data service ","getsome data service ");
                    }
                } catch (JSONException e) {
                    Log.i(e.getMessage(),e.getMessage());
                    e.printStackTrace();
                }


            }
        }

    }

