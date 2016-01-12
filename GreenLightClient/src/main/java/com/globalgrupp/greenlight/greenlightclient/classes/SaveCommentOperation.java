package com.globalgrupp.greenlight.greenlightclient.classes;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by п on 29.12.2015.
 */
public class SaveCommentOperation extends AsyncTask<Comment,Void,Void> {
    @Override
    protected Void doInBackground(Comment... params) {


        BufferedReader reader=null;
        Log.i("doInBackground service ","doInBackground service ");
        // Send data
        try
        {
            JSONObject event=new JSONObject();
            event.put("id",params[0].getEvent().getId());
            // Defined URL  where to send data
            JSONObject msg=new JSONObject();
            msg.put("message",params[0].getMessage());
            msg.put("event", event);
            Log.i("message",msg.toString());
            URL url = new URL("http://192.168.100.14:8080/event/addComment");

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
            String res= sb.toString();
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
        return null;
    }
}
