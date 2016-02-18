package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.*;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.facebook.login.LoginManager;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by п on 31.12.2015.
 */
public class EventListActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, View.OnClickListener, AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener, GoogleApiClient.OnConnectionFailedListener {


    Toolbar mActionBarToolbar;
    ListView lvEvents;
    String keyToken;
    MediaPlayer mPlayer;

    DateFormat df = new SimpleDateFormat("HH:mm");
    EventsAdapter eventsAdapter;

    private AsyncTask<List<Event>, Void, Void> newEventAsyncTask;


    private class EventUploaderThread extends Thread {

        public boolean shouldRefresh=false;
        public boolean shouldContinue=true;
        @Override
        public void run() {

            while (shouldContinue){
                try{


                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info==null||!info.isConnected()){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                        EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                String queueString = prefs.getString("queueEvents","[]");
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putString("queueEvents", "[]");
                prefsEditor.commit();

                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Event>>() {
                }.getType();
                List<Event> queueResults=gson.fromJson(queueString,listType);
                String oldQueueString = prefs.getString("oldMessageQueue","[]");
                queueResults.addAll((ArrayList<Event>)gson.fromJson(oldQueueString,listType));
                prefsEditor = prefs.edit();
                prefsEditor.putString("oldMessageQueue",gson.toJson(queueResults));
                prefsEditor.commit();

                Iterator<Event> iter = queueResults.iterator();
                while(iter.hasNext()&&!shouldRefresh){
                    Event res=iter.next();
                    try{
                        Long audioId=new Long(0);
                        if (res.getAudioPath()!=null && res.getAudioId().equals(new Long(-1))){
                            CreateEventParams cep=new CreateEventParams();
                            cep.setURL(res.getAudioPath());
                            audioId=new UploadFileOperation().execute(cep).get();
                            res.setAudioId(audioId);
                        }



                        List<Long> photoIds=new ArrayList<Long>();
                        if (res.getPhotoPathList().size()>0){
                            for (int i=0;i<res.getPhotoPathList().size();i++){
                                if (res.getPhotoIds().get(i).equals(new Long(-1))){
                                    CreateEventParams cep=new CreateEventParams();
                                    cep.setURL(res.getPhotoPathList().get(i));
                                    Long phId=new UploadFileOperation().execute(cep).get();
                                    photoIds.add(phId);
                                }else{
                                    photoIds.add(res.getPhotoIds().get(i));
                                }
                            }
                        }
                        res.setPhotoIds(photoIds);
                        Long videoId=new Long(0);
                        if (res.getVideoPath()!=null && (res.getVideoId().equals(new Long(-1)))){
                            CreateEventParams cep=new CreateEventParams();
                            cep.setURL(res.getVideoPath());
                            videoId=new UploadFileOperation().execute(cep).get();
                            res.setVideoId(videoId);
                        }

                        Boolean result=new CreateEventOperation().execute(res).get();
                        if (result){
                            iter.remove();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        break; //connection problem again?
                    }
                }
                SharedPreferences.Editor prefsEditor2 = prefs.edit();
                String json = gson.toJson(queueResults);
                prefsEditor2.putString("oldMessageQueue", json);
                prefsEditor2.commit();
                shouldRefresh=false;
                }catch (Exception e){
                    e.printStackTrace();
                    //wtf
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };

    EventUploaderThread eventUploader=new EventUploaderThread();

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof Event) {
            Event event = (Event) v.getTag();
            Intent startIntent = new Intent(this, EventDetailsActivity.class);
            startIntent.putExtra("eventId", event.getId());
            startActivity(startIntent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        try {
            setContentView(R.layout.event_list);
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            setSupportActionBar(mActionBarToolbar);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ImageButton ibIconUp = (ImageButton) findViewById(R.id.ibIconUp);
            ibIconUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                }
            });

            if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.NONE){
                TableRow trBottomButtons=(TableRow)findViewById(R.id.trBottomButtons);
                trBottomButtons.setVisibility(View.GONE);
            } else{
                ImageButton ibRed=(ImageButton)findViewById(R.id.ibRed);
                ibRed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startNewEventActivity();
                    }
                });
                ImageButton ibYellow=(ImageButton)findViewById(R.id.ibYellow);
                ibYellow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startNewEventActivity();
                    }
                });
                ImageButton ibWhite=(ImageButton)findViewById(R.id.ibWhite);
                ibWhite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startNewEventActivity();
                    }
                });
            }
//            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
//            File dir = new File(root + "/gl");
//            deleteFolder(dir);
            eventUploader.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initChannels() {
        try {
            String url = "http://192.168.100.16:8080/channel/getBaseChannels";
            List<Channel> channels = new AsyncTask<String, Void, List<Channel>>() {
                @Override
                protected List<Channel> doInBackground(String... params) {
                    /************ Make Post Call To Web Server ***********/
                    BufferedReader reader = null;
                    List<Channel> result = new ArrayList<Channel>();
                    try {
                        String urlString = params[0];
                        JSONObject msg = new JSONObject();
                        URL url = new URL(urlString);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        conn.setRequestProperty("Accept", "*/*");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("charset", "utf-8");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(20000);

                        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                        String str = msg.toString();
                        byte[] data = str.getBytes("UTF-8");
                        wr.write(data);
                        wr.flush();
                        wr.close();
                        InputStream is; //todo conn.getResponseCode() for errors
                        try {
                            is = conn.getInputStream();
                        } catch (Exception e) {
                            e.printStackTrace();
                            is = conn.getErrorStream();
                        }
                        reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder sb = new StringBuilder();
                        String line = null;

                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }

                        JSONArray jsonResponseArray = new JSONArray(sb.toString());
                        for (int i = 0; i < jsonResponseArray.length(); i++) {
                            JSONObject jsonObject = jsonResponseArray.getJSONObject(i);
                            Channel e = new Channel();
                            e.setId(jsonObject.getLong("id"));
                            e.setChannelName(jsonObject.getString("name"));
                            result.add(e);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        try {
                            reader.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    return result;

                }
            }.execute(url).get();

            ArrayAdapter<Channel> adapter = new ArrayAdapter<Channel>(this, R.layout.channel_spinner_item, channels);
            adapter.setDropDownViewResource(R.layout.channel_spinner_dropdown_item);
            Spinner spinner = (Spinner) findViewById(R.id.spinnerChannel);
            spinner.setAdapter(adapter);
            spinner.setPrompt("Title");
            channels.add(0, new Channel(new Long(0), "Все"));
            for (int i = 0; i < channels.size(); i++) {
                if (channels.get(i).getId().equals(ApplicationSettings.getInstance().getChannelId())) {
                    spinner.setSelection(i);
                }
            }
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> parent, View view,
                                           final int position, long id) {
                    try {
                        Channel item = (Channel) parent.getItemAtPosition(position);
                        ApplicationSettings.getInstance().setChannelId(item.getId());
                        if (item.getId().equals(new Long(0))) {
                            refreshEventList(null);
                        } else {
                            refreshEventList(item.getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ApplicationSettings.getInstance().getmGoogleApiClient() == null) {
            ApplicationSettings.getInstance().setmGoogleApiClient(new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build());
            try {
                ApplicationSettings.getInstance().getmGoogleApiClient().connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getApplicationContext().registerReceiver(mMessageReceiver, new IntentFilter("newEventBroadCast"));
        initChannels();
        try{
            eventUploader.shouldRefresh=true;
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        if (newEventAsyncTask != null && !newEventAsyncTask.isCancelled()) {
            newEventAsyncTask.cancel(true);
            newEventAsyncTask = null;
            if (mPlayer!=null){
                try{
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer=null;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        super.onPause();
        getApplicationContext().unregisterReceiver(mMessageReceiver);
    }

    ArrayList<Event> allEvents = new ArrayList<Event>();

    public void refreshEventList(final Long channelId) {
        if (ApplicationSettings.getInstance().getmGoogleApiClient() == null) {
            ApplicationSettings.getInstance().setmGoogleApiClient(new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            refreshEventList(channelId);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build());
            try {
                ApplicationSettings.getInstance().getmGoogleApiClient().connect();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {

            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);

            String ids = prefs.getString("oldEventsId", "1");
            String[] idsArray = ids.split(",");
            List<Long> ll = new ArrayList<Long>();
            for (int i = 0; i < idsArray.length; i++) {
                try {
                    ll.add(new Long(idsArray[i]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ApplicationSettings.getInstance().setOldEventsId(ll);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SimpleGeoCoords eLocation = null;
        if (getIntent().hasExtra("location")) {
            eLocation = (SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
        }
        try {
            GetEventParams params = new GetEventParams();
            if (channelId != null) {
                params.setURL("http://192.168.100.16:8080/event/getEventsByChannel/" + channelId.toString());
            } else {
                params.setURL("http://192.168.100.16:8080/event/getNearestEvents");
            }

            if (eLocation == null) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        ApplicationSettings.getInstance().getmGoogleApiClient());
                if (mLastLocation == null) {
                    return;
                }
                eLocation = new SimpleGeoCoords(mLastLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getAltitude());
            }
            params.setCurrentCoords(eLocation);
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            params.setRadius(new Long(prefs.getLong("event_radius", 5)));

//            final ArrayList<Event> events = (ArrayList<Event>) new GetEventsOperation().execute(params).get();


            final ArrayList<Event> events =(ArrayList<Event>) new AsyncTask<GetEventParams, Void, List<Event>>(){
                @Override
                protected List<Event> doInBackground(GetEventParams... params) {

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

                        JSONArray jsonResponseArray=new JSONArray(sb.toString());
                        GsonBuilder builder=new GsonBuilder();
                        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

                                return new Date(json.getAsJsonPrimitive().getAsLong());
                            }
                        });
                        Gson gson = builder.create();
                        List<Event> queueResults=new ArrayList<Event>();
                        Type listType = new TypeToken<ArrayList<Event>>() {
                        }.getType();
                        queueResults=gson.fromJson(sb.toString(),listType);
                        result=queueResults;
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
            }.execute(params).get();


            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            String queueString = sharedPreferences.getString("queueEvents","[]");

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Event>>() {
            }.getType();
            List<Event> queueResults=gson.fromJson(queueString,listType);
            String oldQueueString = prefs.getString("oldMessageQueue","[]");
            queueResults.addAll((ArrayList<Event>)gson.fromJson(oldQueueString,listType));

            for(int q=0;q<queueResults.size();q++){
                Event param=queueResults.get(q);
                //вставляем очередь в список
                int position=0;
                for(int k=0;k<events.size();k++){
                    if (events.get(k).getCreateDate().compareTo(param.getCreateDate())==-1 ){
                        position=k;
                        break;
                    }
                    position=k;
                }
                events.add(position,param);
            }
            allEvents=events;

            eventsAdapter=new EventsAdapter(getApplicationContext(),events);
            ListView listView=(ListView)findViewById(R.id.listViewEvents);
            listView.setAdapter(eventsAdapter);

            newEventAsyncTask = new AsyncTask<List<Event>, Void, Void>() {
                private boolean isplayed = false;
                //MediaPlayer mPlayer;

                @Override
                protected Void doInBackground(List<Event>... lists) {
                    try{
                        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                        String sharedGuids=prefs.getString("oldEventGuid","");
                        String sharedIds=prefs.getString("oldEventsId","");
                        List<Event> events = lists[0];
                        List<String> oldId = new ArrayList<String>(Arrays.asList(sharedIds.split(",")));
                        if (oldId==null){
                            oldId=new ArrayList<String>();
                        }
                        List<String> oldGuids= new ArrayList<String>(Arrays.asList(sharedGuids.split(",")));
                        if (oldGuids==null){
                            oldGuids=new ArrayList<String>();
                        }
                        for (int i = events.size() - 1; i >= 0; i--) {
                            String guid=events.get(i).getUniqueGUID()!=null?events.get(i).getUniqueGUID().toString():"";
                            String id=events.get(i).getId()!=null?events.get(i).getId().toString():"";
                            if (!oldId.contains(id) &&(!oldGuids.contains(guid))) {
                                oldId.add(id);
                                oldGuids.add(guid);
                                String listString = TextUtils.join(",",oldId);
                                String oldEventsGuid=TextUtils.join(",",oldGuids);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("oldEventsId", listString);
                                editor.putString("oldEventGuid",oldEventsGuid);
                                editor.commit();


                                if (events.get(i).getAudioId() == null || events.get(i).getAudioId().equals(new Long(0))) {
                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                    r.play();
                                } else {
                                    File file = null;
                                    try {
                                        String DownloadUrl = "http://192.168.100.16:8080/utils/getFile/" + events.get(i).getAudioId().toString();
                                        String fileName = "newEventAudio.3gp";
                                        String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                                        File dir = new File(root + "/gl");
                                        if (dir.exists() == false) {
                                            dir.mkdirs();
                                        }
                                        URL url = new URL(DownloadUrl);
                                        file = new File(dir, fileName);

                                        URLConnection ucon = url.openConnection();
                                        InputStream is = ucon.getInputStream();
                                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                        int nRead;
                                        byte[] data = new byte[16384];

                                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                                            buffer.write(data, 0, nRead);
                                        }
                                        buffer.flush();

                                        byte[] dataFile = buffer.toByteArray();
                                        FileOutputStream fos = new FileOutputStream(file);
                                        fos.write(dataFile);
                                        fos.flush();
                                        fos.close();
                                        String audioPath = file.toString();
                                        mPlayer = new MediaPlayer();

                                        mPlayer.setDataSource(audioPath);
                                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                isplayed = false;
                                            }
                                        });
                                        mPlayer.prepare();
                                        isplayed = true;
                                        mPlayer.start();

                                        while (isplayed && !isCancelled()) {
                                            if (isCancelled()) break;
                                        }
                                        isplayed = false;
                                        if (isCancelled()) {
                                            try{
                                                mPlayer.stop();
                                                mPlayer.release();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                            break;
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                        if (mPlayer!=null){
                            try{
                                mPlayer.stop();
                                mPlayer.release();
                                mPlayer=null;
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    return null;
                }

                @Override
                protected void onCancelled() {
                    isplayed = false;
                    super.onCancelled();
                }

                @Override
                protected void onCancelled(Void aVoid) {
                    isplayed = false;
                    try {
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    super.onCancelled(aVoid);
                }
            }.execute(allEvents);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Event event = (Event) parent.getItemAtPosition(position);
            Intent startIntent = new Intent(this, EventDetailsActivity.class);
            startIntent.putExtra("eventId", event.getId());
            startActivity(startIntent);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (newEventAsyncTask != null && !newEventAsyncTask.isCancelled()) {
            newEventAsyncTask.cancel(true);
            newEventAsyncTask = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        if (ApplicationSettings.getInstance().getAuthorizationType()!=AuthorizationType.NONE){
            MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
            editMenuItem.setOnMenuItemClickListener(this);
        }else {
            MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
            editMenuItem.setVisible(false);
        }

        MenuItem eventListMenuItem = menu.findItem(R.id.action_event_list);
        eventListMenuItem.setOnMenuItemClickListener(this);
        eventListMenuItem.setVisible(false);
        MenuItem mapItem = menu.findItem(R.id.action_map);
        mapItem.setOnMenuItemClickListener(this);
        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        logoutItem.setOnMenuItemClickListener(this);
        MenuItem settingItem = menu.findItem(R.id.action_settings);
        settingItem.setOnMenuItemClickListener(this);
        return true;
    }


    private void startNewEventActivity(){
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    ApplicationSettings.getInstance().getmGoogleApiClient());
            SimpleGeoCoords coords = new SimpleGeoCoords(mLastLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getAltitude());
            Intent startIntent = new Intent(this, NewEventActivity.class);
            startIntent.putExtra("location", coords);
            startActivityForResult(startIntent, 1);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        try {
            Intent startIntent;
            if (menuItem.getItemId() == R.id.action_new_event) {
                startNewEventActivity();
            } else if (menuItem.getItemId() == R.id.action_event_list) {
                startIntent = new Intent(this, EventListActivity.class);
//                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            } else if (menuItem.getItemId() == R.id.action_map) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        ApplicationSettings.getInstance().getmGoogleApiClient());
                SimpleGeoCoords coords = new SimpleGeoCoords(mLastLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getAltitude());
                startIntent = new Intent(this, MainActivity.class);
                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            } else if (menuItem.getItemId() == R.id.action_logout) {
                final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                        EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("FacebookToken","");
                editor.putString("VKToken","");
                editor.putString("TwitterToken","");
                editor.putString("GreenLightToken","");
                editor.commit();
                if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.FACEBOOK){
                    LoginManager.getInstance().logOut();
                } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.VK){
                    VKSdk.logout();
                } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.TWITTER){
//                    Twitter twitter=new TwitterFactory().getInstance();
//                    twitter.setOAuthAccessToken(ApplicationSettings.getInstance().getTwitterAccessToken());
//                    twitter.shutdown();
                }
                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.NONE);
                startIntent = new Intent(this, AuthorizationActivity.class);
                startActivity(startIntent);
            } else if (menuItem.getItemId() == R.id.action_settings) {
                startIntent = new Intent(this, SettingsActivity.class);
                startActivity(startIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            initChannels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ApplicationSettings.getInstance().getAuthorizationType() == AuthorizationType.VK) {
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    // Пользователь успешно авторизовался
                    keyToken = res.accessToken;
                }

                @Override
                public void onError(VKError error) {
                    // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                    keyToken = error.errorMessage + error.errorReason;
                }
            })) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            //refreshEventList(null);
        }

    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        //super.onBackPressed();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long newEventId = intent.getLongExtra("eventId", 0);
            addEventToList(newEventId);
        }
    };

    private void addEventToList(final Long eventId) {

        for (int i = 0; i < allEvents.size(); i++) {
            if (eventId.equals(allEvents.get(i).getId())) {
                return;
            }
        }
        try {
            GetEventParams params = new GetEventParams();
            params.setURL("http://192.168.100.16:8080/event/getEvent");
            params.setEventId(eventId);
            if (ApplicationSettings.getInstance().getChannelId() != null &&
                    !ApplicationSettings.getInstance().getChannelId().equals(new Long(0)))
                params.setChannelId(ApplicationSettings.getInstance().getChannelId());

            List<Event> events = new GetEventsOperation().execute(params).get();
            if (events.size() == 0) return;
            final Event newEvent = events.get(0);
//            Thread.sleep(1000);
            for (int k=0;k<allEvents.size();k++){
                if (allEvents.get(k).getUniqueGUID()!=null&&allEvents.get(k).getUniqueGUID().equals(newEvent.getUniqueGUID())){
                    allEvents.remove(k);
                    allEvents.add(k,newEvent);
                    eventsAdapter.remove(eventsAdapter.getItem(k));
                    eventsAdapter.insert(newEvent,k);
                    break;
                }
            }

            if (newEventAsyncTask!=null){
                newEventAsyncTask.cancel(true);
                newEventAsyncTask=null;
            }
            newEventAsyncTask = new AsyncTask<List<Event>, Void, Void>() {
                private boolean isplayed = false;
                MediaPlayer mPlayer;

                @Override
                protected Void doInBackground(List<Event>... lists) {
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                            EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                    String sharedGuids=prefs.getString("oldEventGuid","");
                    String sharedIds=prefs.getString("oldEventsId","");
                    List<Event> events = lists[0];
                    List<String> oldId = new ArrayList<String>(Arrays.asList(sharedIds.split(",")));

                    List<String> oldGuids=new ArrayList<String>(Arrays.asList(sharedGuids.split(",")));
                    for (int i = events.size() - 1; i >= 0; i--) {
                        String guid=events.get(i).getUniqueGUID()!=null?events.get(i).getUniqueGUID().toString():"";
                        String id=events.get(i).getId()!=null?events.get(i).getId().toString():"";
                        if (!oldId.contains(id) &&(!oldGuids.contains(guid))) {
                            oldId.add(id);
                            oldGuids.add(guid);
                            String listString = TextUtils.join(",",oldId);
                            String oldEventsGuid=TextUtils.join(",",oldGuids);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("oldEventsId", listString);
                            editor.putString("oldEventGuid",oldEventsGuid);
                            editor.commit();

                            if (events.get(i).getAudioId() == null || events.get(i).getAudioId().equals(new Long(0))) {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();
                            } else {
                                File file = null;
                                try {
                                    String DownloadUrl = "http://192.168.100.16:8080/utils/getFile/" + events.get(i).getAudioId().toString();
                                    String fileName = "newEventAudio.3gp";
                                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                                    File dir = new File(root + "/gl");
                                    if (dir.exists() == false) {
                                        dir.mkdirs();
                                    }
                                    URL url = new URL(DownloadUrl); //you can write here any link
                                    file = new File(dir, fileName);

                                    URLConnection ucon = url.openConnection();
                                    InputStream is = ucon.getInputStream();
                                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                    int nRead;
                                    byte[] data = new byte[16384];

                                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                                        buffer.write(data, 0, nRead);
                                    }
                                    buffer.flush();

                                    byte[] dataFile = buffer.toByteArray();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(dataFile);
                                    fos.flush();
                                    fos.close();
                                    String audioPath = file.toString();
                                    mPlayer = new MediaPlayer();

                                    mPlayer.setDataSource(audioPath);
                                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            isplayed = false;
                                        }
                                    });
                                    mPlayer.prepare();
                                    isplayed = true;
                                    mPlayer.start();

                                    while (isplayed && !isCancelled()) {
                                        if (isCancelled()) break;
                                    }
                                    isplayed = false;
                                    if (isCancelled()) {
                                        try{
                                            mPlayer.stop();
                                            mPlayer.release();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        break;

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                    return null;
                }

                @Override
                protected void onCancelled() {
                    isplayed = false;
                    super.onCancelled();
                }

                @Override
                protected void onCancelled(Void aVoid) {
                    isplayed = false;
                    try {
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    super.onCancelled(aVoid);
                }
            }.execute(events);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    //deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}

