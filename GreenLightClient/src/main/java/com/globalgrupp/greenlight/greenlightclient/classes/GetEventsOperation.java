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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by п on 28.12.2015.
 */
public class GetEventsOperation extends AsyncTask<GetEventParams, Void, List<Event>> {

    // Required initialization

    //private final HttpClient Client = new AndroidHttpClient();
    private String Content;
    private String Error = null;
    String data ="";

    @Override
    protected List<Event> doInBackground(GetEventParams... params) {
        /************ Make Post Call To Web Server ***********/
        BufferedReader reader=null;
        Log.i("doInBackground service ","doInBackground service ");
        // Send data
        List<Event> result=new ArrayList<Event>();
        try
        {
            String urlString=params[0].getURL();
            // Defined URL  where to send data
            JSONObject msg=new JSONObject();
            if (params[0].getCurrentCoords()!=null){
                msg.put("longitude",params[0].getCurrentCoords().getLongtitude());
                msg.put("latitude",params[0].getCurrentCoords().getLatitude());
                msg.put("altitude",params[0].getCurrentCoords().getAltitude());
                msg.put("radius",params[0].getRadius());
            }
            if (params[0].getEventId()!=null){
                msg.put("eventId",params[0].getEventId());
                if (params[0].getChannelId()!=null && !params[0].getChannelId().equals(new Long(0))){
                    msg.put("channelId",params[0].getChannelId());
                }

            }

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
            conn.setConnectTimeout(20000);
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

            JSONArray jsonResponseArray=new JSONArray(sb.toString());
            for (int i=0;i<jsonResponseArray.length();i++){
                JSONObject jsonObject=jsonResponseArray.getJSONObject(i);
                Event e=new Event();
                e.setAltitude(jsonObject.getDouble("altitude"));
                e.setLongitude(jsonObject.getDouble("longitude"));
                e.setLatitude(jsonObject.getDouble("latitude"));
                e.setMessage(jsonObject.getString("message"));
                e.setId(jsonObject.getLong("id"));
                e.setAudioId(!jsonObject.isNull("audioId")?jsonObject.getLong("audioId"):null);
                e.setPhotoId(!jsonObject.isNull("photoId")?jsonObject.getLong("photoId"):null);
                e.setVideoId(!jsonObject.isNull("videoId")?jsonObject.getLong("videoId"):null);
                e.setStreetName(!jsonObject.isNull("streetName")?jsonObject.getString("streetName"):null);
                e.setCreateDate(new Date(jsonObject.getLong("createDate")));
                e.setSocialType(!jsonObject.isNull("socialType")?jsonObject.getLong("socialType"):null);
                e.setUserName(!jsonObject.isNull("userName")?jsonObject.getString("userName"):"");
                List<Long> photoIds=new ArrayList<Long>();
                JSONArray photoArray=jsonObject.getJSONArray("photoIds");
                for (int k=0;k<photoArray.length(); k++){
                    Long photoId=photoArray.getLong(k);
                    photoIds.add(photoId);
                }
                e.setPhotoIds(photoIds);
                JSONArray jsonCommentsArray= jsonObject.getJSONArray("comments");
                ArrayList<Comment> comments=new ArrayList<Comment>();
                for (int k=0;k<jsonCommentsArray.length();k++){
                    JSONObject comObject=jsonCommentsArray.getJSONObject(k);
                    Comment com=new Comment();
                    com.setId(comObject.getLong("id"));
                    com.setMessage(comObject.getString("message"));
                    com.setCreateDate(new Date(comObject.getLong("createDate")));
                    com.setAudioId(!comObject.isNull("audioId")?comObject.getLong("audioId"):null);
                    com.setVideoId(!comObject.isNull("videoId")?comObject.getLong("videoId"):null);
                    com.setSocialType(!comObject.isNull("socialType")?comObject.getLong("socialType"):null);
                    com.setUserName(!comObject.isNull("userName")?comObject.getString("userName"):"");
                    List<Long> commentPhotoIds=new ArrayList<Long>();
                    JSONArray commentPhotoArray=comObject.getJSONArray("photoIds");
                    for (int z=0;z<commentPhotoArray.length(); z++){
                        Long photoId=commentPhotoArray.getLong(z);
                        commentPhotoIds.add(photoId);
                    }
                    com.setPhotoIds(commentPhotoIds);
                    comments.add(com);
                }
                e.setComments(comments);
                result.add(e);
            }
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
        return result;
    }

    protected void onPreExecute() {
        try{
            // Set Request parameter

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void onPostExecute(Void unused) {

    }
}
