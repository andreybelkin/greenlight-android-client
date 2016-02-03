package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.*;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by п on 31.12.2015.
 */
public class EventListActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, View.OnClickListener,AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener, GoogleApiClient.OnConnectionFailedListener {


    Toolbar mActionBarToolbar;
    ListView lvEvents;
    String keyToken;
    //MediaPlayer mPlayer;
    EventsAdapter eventsAdapter;
    DateFormat df = new SimpleDateFormat("HH:mm");

    private  AsyncTask<List<Event>, Void, Void> newEventAsyncTask;

//    protected GoogleApiClient mGoogleApiClient;

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof Event){
            Event event=(Event)v.getTag();
            Intent startIntent = new Intent(this, EventDetailsActivity.class);
            startIntent.putExtra("eventId", event.getId());
            startActivity(startIntent);
        }
        //startIntent.putExtra("eventId", event.getId());
        //startActivity(startIntent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        try{
            setContentView(R.layout.event_list);
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

            //mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            setSupportActionBar(mActionBarToolbar);
            //getSupportActionBar().setTitle("");
            //getSupportActionBar().setIcon(R.drawable.ic_launcher);
            //getSupportActionBar().setHomeButtonEnabled(true);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ImageButton ibIconUp=(ImageButton)findViewById(R.id.ibIconUp);
            ibIconUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ScrollView scrollView=(ScrollView)findViewById(R.id.scrollView);
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                }
            });
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File (root + "/gl");
            deleteFolder(dir);
           //refreshEventList();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void initChannels(){
        try{
            String url="http://192.168.1.38:8081/channel/getBaseChannels";
            //подгрузка каналов
            List<Channel> channels= new AsyncTask<String, Void, List<Channel>>() {
                @Override
                protected List<Channel> doInBackground(String... params) {
                    /************ Make Post Call To Web Server ***********/
                    BufferedReader reader=null;
                    Log.i("doInBackground service ","doInBackground service ");
                    // Send data
                    List<Channel> result=new ArrayList<Channel>();
                    try
                    {
                        String urlString=params[0];
                        // Defined URL  where to send data
                        JSONObject msg=new JSONObject();
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
                            Channel e=new Channel();
                            e.setId(jsonObject.getLong("id"));
                            e.setChannelName(jsonObject.getString("name"));

                            result.add(e);
                        }
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
            }.execute(url).get();

            ArrayAdapter<Channel> adapter = new ArrayAdapter<Channel>(this, R.layout.channel_spinner_item, channels );

            adapter.setDropDownViewResource(R.layout.channel_spinner_dropdown_item);

            Spinner spinner = (Spinner) findViewById(R.id.spinnerChannel);
            spinner.setAdapter(adapter);
            // заголовок
            spinner.setPrompt("Title");

            //spinner.setPopupBackgroundDrawable();
            // выделяем элемент
            channels.add(0,new Channel(new Long(0),"Все"));
            for(int i=0;i<channels.size();i++){
                if (channels.get(i).getId().equals(ApplicationSettings.getInstance().getChannelId())){
                    spinner.setSelection(i);

                }
            }
            // устанавливаем обработчик нажатия
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> parent, View view,
                                           final int position, long id) {
                    Thread myThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Channel item = (Channel)parent.getItemAtPosition(position);
                                ApplicationSettings.getInstance().setChannelId(item.getId());
                                if (item.getId().equals(new Long(0))){
                                    refreshEventList(null);
                                }else{
                                    refreshEventList(item.getId());
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    myThread.start();

                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (ApplicationSettings.getInstance().getmGoogleApiClient()==null){
            ApplicationSettings.getInstance().setmGoogleApiClient( new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build());
            try{
                ApplicationSettings.getInstance().getmGoogleApiClient().connect();
            }catch (Exception e){
                e.printStackTrace();
            }

//            ApplicationSettings.getInstance().startLocationTimer();
        }
        getApplicationContext().registerReceiver(mMessageReceiver, new IntentFilter("newEventBroadCast"));
        initChannels();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(mMessageReceiver);
    }

    ArrayList<Event> allEvents=new ArrayList<Event>();

    public void refreshEventList(final Long channelId){
        if (ApplicationSettings.getInstance().getmGoogleApiClient()==null){
            ApplicationSettings.getInstance().setmGoogleApiClient( new GoogleApiClient.Builder(this)
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
                    .addApi(Places.GEO_DATA_API)
                    .build());
            try{
                ApplicationSettings.getInstance().getmGoogleApiClient().connect();
            }catch (Exception e){
                e.printStackTrace();
            }

//            ApplicationSettings.getInstance().startLocationTimer();
        }
        File cacheDir= getCacheDir();
        File oldEventsId=new File(cacheDir,"oldEventsId");
        if (oldEventsId.exists()){
            try{
                ApplicationSettings.getInstance().setOldEventsIdFilePath(oldEventsId.toString());
                InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(oldEventsId));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                bufferedReader.close();
                inputStreamReader.close();
                String ids=stringBuilder.toString();
                String[] idsArray=ids.split(",");
                List<Long> ll =new ArrayList<Long>();
                for(int i=0;i<idsArray.length;i++){
                    try{
                        ll.add(new Long(idsArray[i]));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                ApplicationSettings.getInstance().setOldEventsId(ll);
//                ApplicationSettings.getInstance().setOldEventsId(new ArrayList<Long>());
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            try{
                oldEventsId.createNewFile();
                ApplicationSettings.getInstance().setOldEventsIdFilePath(oldEventsId.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //ApplicationSettings.getInstance().setOldEventsId(new ArrayList<Long>());
        SimpleGeoCoords eLocation=null;
        if (getIntent().hasExtra("location")) {
            eLocation = (SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
        }
        try{
            GetEventParams params=new GetEventParams();
            if (channelId!=null){
                params.setURL("http://192.168.1.38:8081/event/getEventsByChannel/"+channelId.toString());
            }else{
                params.setURL("http://192.168.1.38:8081/event/getNearestEvents");
            }

            if (eLocation==null){
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        ApplicationSettings.getInstance().getmGoogleApiClient());
                if (mLastLocation==null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                LocationServices.FusedLocationApi.requestLocationUpdates(ApplicationSettings.getInstance().getmGoogleApiClient(), new LocationRequest(), new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        int i=0;
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    });

                    return;
                }
                eLocation=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
            }
            params.setCurrentCoords(eLocation);
            SharedPreferences prefs=getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            params.setRadius(new Long(prefs.getLong("event_radius",5)));

            final ArrayList<Event> events=(ArrayList<Event>)new GetEventsOperation().execute(params).get();
            allEvents=events;
            //if (events.size()==0){return;}
            lvEvents=(ListView)findViewById(R.id.listViewEvents);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final LinearLayout llevents=(LinearLayout)findViewById(R.id.llEvents);
            if (llevents.getChildCount() > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llevents.removeAllViews();
                    }
                });
            }

            for (int i=0;i<events.size();i++){
                final Event eventItem=events.get(i);
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.lv_events_item ,null);
                final View convertView=layout;
                ((TextView) convertView.findViewById(R.id.tvEventsTitle)).setText(eventItem.getMessage());
                ((TextView) convertView.findViewById(R.id.tvEventsDate)).setText(df.format(eventItem.getCreateDate()));
                ((TextView) convertView.findViewById(R.id.tvEventsStreet)).setText(eventItem.getStreetName());
                if (eventItem.getAudioId()==null||eventItem.getAudioId().equals(new Long(0))){
                    convertView.findViewById(R.id.ivHasAudio).setVisibility(View.GONE);
                    ViewGroup.LayoutParams qwe=convertView.findViewById(R.id.ivHasAudio).getLayoutParams();
                    qwe.width=0;
                    convertView.findViewById(R.id.ivHasAudio).setLayoutParams(qwe);
                }else{

                }
                if (eventItem.getPhotoIds()==null||eventItem.getPhotoIds().size()==0){
                    convertView.findViewById(R.id.ivHasPhoto).setVisibility(View.GONE);
                    ViewGroup.LayoutParams qwe=convertView.findViewById(R.id.ivHasPhoto).getLayoutParams();
                    qwe.width=0;
                    convertView.findViewById(R.id.ivHasPhoto).setLayoutParams(qwe);
                }else{

                }
                if (eventItem.getVideoId()==null||eventItem.getVideoId().equals(new Long(0))){
                    convertView.findViewById(R.id.ivHasVideo).setVisibility(View.GONE);
                    ViewGroup.LayoutParams qwe=convertView.findViewById(R.id.ivHasVideo).getLayoutParams();
                    qwe.width=0;
                    convertView.findViewById(R.id.ivHasVideo).setLayoutParams(qwe);
                }else{

                }

                convertView.setOnClickListener(this);
                convertView.setTag(eventItem);


                ImageButton ibComment=(ImageButton) convertView.findViewById(R.id.ibComment);
                ibComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent startIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        startIntent.putExtra("eventId", eventItem.getId());
                        startIntent.putExtra("openComment",true);
                        startActivity(startIntent);
                    }
                });

                ImageButton ibShare=(ImageButton)convertView.findViewById(R.id.ibShare);
                ibShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent startIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        startIntent.putExtra("eventId", eventItem.getId());
                        startIntent.putExtra("openShare",true);
                        startActivity(startIntent);
                    }
                });

                ImageButton ibMap=(ImageButton)convertView.findViewById(R.id.ibMap);
                ibMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent startIntent= new Intent(getApplicationContext(), MainActivity.class);
                        SimpleGeoCoords geoCoords=new SimpleGeoCoords(eventItem.getLongitude(),eventItem.getLatitude(),eventItem.getAltitude());
                        startIntent.putExtra("eventCoords",geoCoords);
                        startActivity(startIntent);
                    }
                });

                if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.NONE){
                    ibShare.setImageResource(R.mipmap.icon_share_grey);
                    ibShare.setEnabled(false);
                    //ibShare.setVisibility(View.INVISIBLE);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llevents.addView(convertView);
                    }
                });

            }

            newEventAsyncTask =new AsyncTask<List<Event>, Void, Void>() {
                private boolean isplayed=false;
                MediaPlayer mPlayer;
                @Override
                protected Void doInBackground(List<Event>... lists) {

                    List<Event> events=lists[0];
                    List<Long> oldId=ApplicationSettings.getInstance().getOldEventsId();
                    for (int i=events.size()-1;i>=0;i--){
                        if (!oldId.contains(events.get(i).getId())){
                                oldId.add(events.get(i).getId());
                                ApplicationSettings.getInstance().setOldEventsId(oldId);
                                try{
                                    File temp = new File(ApplicationSettings.getInstance().getOldEventsIdFilePath());
                                    String listString = "";

                                    for (Long s : oldId)
                                    {
                                        listString += s.toString() + ",";
                                    }
                                    //write it
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
                                    bw.write(listString);
                                    bw.close();
                                }catch(Exception e){
                                    e.printStackTrace();
                                }

                                if (events.get(i).getAudioId()==null||events.get(i).getAudioId().equals(new Long(0))){
                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                    r.play();
                                }else{
                                    File file=null;
                                    try {
                                        String DownloadUrl="http://192.168.1.38:8081/utils/getFile/"+events.get(i).getAudioId().toString();
                                        String fileName= "newEventAudio.3gp";
                                        String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                                        File dir = new File (root + "/gl");
                                        if(dir.exists()==false) {
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
                                        String audioPath=file.toString();
                                        mPlayer = new MediaPlayer();

                                        mPlayer.setDataSource(audioPath);
                                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                isplayed=false;
                                            }
                                        });
                                        mPlayer.prepare();
                                        isplayed=true;
                                        mPlayer.start();

                                        while (isplayed && !isCancelled()){
                                            if (isCancelled()) break;
                                        }
                                        isplayed=false;
                                        if (isCancelled()){
                                            mPlayer.stop();
                                            mPlayer.release();
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
                    isplayed=false;
                    super.onCancelled();
                }

                @Override
                protected void onCancelled(Void aVoid) {
                    isplayed=false;
                    try{
                        mPlayer.stop();
                        mPlayer.release();
                        mPlayer=null;
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    super.onCancelled(aVoid);
                }
            }.execute(events);

            int i=0;

        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try{
            Event event=(Event)parent.getItemAtPosition(position);
            Intent startIntent = new Intent(this, EventDetailsActivity.class);
            startIntent.putExtra("eventId", event.getId());
            startActivity(startIntent);
        }catch(Exception e){

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (newEventAsyncTask!=null && !newEventAsyncTask.isCancelled())
        {
            newEventAsyncTask.cancel(true);
            newEventAsyncTask=null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
//        if (ApplicationSettings.getInstance().getAuthorizationType()!=AuthorizationType.NONE){
            MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
            editMenuItem.setOnMenuItemClickListener(this);
//        }else {
//            MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
//            editMenuItem.setVisible(false);
//        }

        MenuItem eventListMenuItem=menu.findItem(R.id.action_event_list);
        eventListMenuItem.setOnMenuItemClickListener(this);
        eventListMenuItem.setVisible(false);
        MenuItem mapItem=menu.findItem(R.id.action_map);
        mapItem.setOnMenuItemClickListener(this);
        MenuItem logoutItem=menu.findItem(R.id.action_logout);
        logoutItem.setOnMenuItemClickListener(this);
        MenuItem settingItem=menu.findItem(R.id.action_settings);
        settingItem.setOnMenuItemClickListener(this);

        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        try{
            if (newEventAsyncTask!=null && !newEventAsyncTask.isCancelled())
            {
//                newEventAsyncTask.cancel(true);
                newEventAsyncTask.cancel(true);
                newEventAsyncTask=null;
            }


            Intent startIntent;
            if (menuItem.getItemId()==R.id.action_new_event){
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        ApplicationSettings.getInstance().getmGoogleApiClient());
                SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
                startIntent= new Intent(this, NewEventActivity.class);
                startIntent.putExtra("location", coords);
                startActivityForResult(startIntent,1);
            }else if(menuItem.getItemId()==R.id.action_event_list){
                startIntent= new Intent(this, EventListActivity.class);
//                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            } else if (menuItem.getItemId()==R.id.action_map){
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        ApplicationSettings.getInstance().getmGoogleApiClient());
                SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
                startIntent= new Intent(this, MainActivity.class);
                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            } else if (menuItem.getItemId()==R.id.action_logout) {
                startIntent=new Intent(this,AuthorizationActivity.class);
                startActivity(startIntent);
            } else if (menuItem.getItemId()==R.id.action_settings){
                startIntent=new Intent(this,SettingsActivity.class);
                startActivity(startIntent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            initChannels();
        }catch(Exception e){
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
        if(ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.VK){
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    // Пользователь успешно авторизовался
                    keyToken=res.accessToken;
                }
                @Override
                public void onError(VKError error) {
                    // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                    keyToken=error.errorMessage+error.errorReason;
                }
            })){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
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
            Long newEventId=intent.getLongExtra("eventId",0);
            addEventToList(newEventId);
        }
    };
    private void addEventToList(final Long eventId){

        boolean alreadyLoaded=false;
        for (int i=0;i<allEvents.size();i++){
            if (eventId.equals(allEvents.get(i).getId())){
                alreadyLoaded=true;
            }
        }
        if (alreadyLoaded){
            return;
        }

                try{
                    GetEventParams params=new GetEventParams();
                    params.setURL("http://192.168.1.38:8081/event/getEvent");
                    params.setEventId(eventId);
                    if (ApplicationSettings.getInstance().getChannelId()!=null &&
                            !ApplicationSettings.getInstance().getChannelId().equals(new Long(0)))
                    params.setChannelId(ApplicationSettings.getInstance().getChannelId());

                    List<Event> events=new GetEventsOperation().execute(params).get();
                    if (events.size()==0) return;
                    final Event newEvent=events.get(0);
                    //newEvent.setMessage("!!!!!!!!!!");

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.lv_events_item ,null);
                    final View convertView=layout;
                    ((TextView) convertView.findViewById(R.id.tvEventsTitle)).setText(newEvent.getMessage());
                    ((TextView) convertView.findViewById(R.id.tvEventsDate)).setText(df.format(newEvent.getCreateDate()));
                    ((TextView) convertView.findViewById(R.id.tvEventsStreet)).setText(newEvent.getStreetName());
                    if (newEvent.getAudioId()==null||newEvent.getAudioId().equals(new Long(0))){
                        convertView.findViewById(R.id.ivHasAudio).setVisibility(View.GONE);
                    }
                    if (newEvent.getPhotoIds()==null||newEvent.getPhotoIds().size()==0){
                        convertView.findViewById(R.id.ivHasPhoto).setVisibility(View.GONE);
                    }
                    if (newEvent.getVideoId()==null||newEvent.getVideoId().equals(new Long(0))){
                        convertView.findViewById(R.id.ivHasVideo).setVisibility(View.GONE);
                    }
                    convertView.setOnClickListener(this);
                    convertView.setTag(newEvent);

                    ImageButton ibComment=(ImageButton) convertView.findViewById(R.id.ibComment);
                    ibComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent startIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                            startIntent.putExtra("eventId", newEvent.getId());
                            startIntent.putExtra("openComment",true);
                            startActivity(startIntent);
                        }
                    });

                    ImageButton ibShare=(ImageButton)convertView.findViewById(R.id.ibShare);
                    ibShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent startIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                            startIntent.putExtra("eventId", newEvent.getId());
                            startIntent.putExtra("openShare",true);
                            startActivity(startIntent);
                        }
                    });

                    ImageButton ibMap=(ImageButton)convertView.findViewById(R.id.ibMap);
                    ibMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent startIntent= new Intent(getApplicationContext(), MainActivity.class);
                            SimpleGeoCoords geoCoords=new SimpleGeoCoords(newEvent.getLongitude(),newEvent.getLatitude(),newEvent.getAltitude());
                            startIntent.putExtra("eventCoords",geoCoords);
                            startActivity(startIntent);
                        }
                    });

                    if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.NONE){
                        ibShare.setImageResource(R.mipmap.icon_share_grey);
                        ibShare.setEnabled(false);
                        //ibShare.setVisibility(View.INVISIBLE);
                    }

                    LinearLayout llevents=(LinearLayout)findViewById(R.id.llEvents);
                    llevents.addView(convertView,0);

                    newEventAsyncTask =new AsyncTask<List<Event>, Void, Void>() {
                        private boolean isplayed=false;
                        MediaPlayer mPlayer;
                        @Override
                        protected Void doInBackground(List<Event>... lists) {

                            List<Event> events=lists[0];
                            List<Long> oldId=ApplicationSettings.getInstance().getOldEventsId();
                            for (int i=events.size()-1;i>=0;i--){
                                if (!oldId.contains(events.get(i).getId())){
                                    oldId.add(events.get(i).getId());
                                    ApplicationSettings.getInstance().setOldEventsId(oldId);
                                    try{
                                        File temp = new File(ApplicationSettings.getInstance().getOldEventsIdFilePath());
                                        String listString = "";

                                        for (Long s : oldId)
                                        {
                                            listString += s.toString() + ",";
                                        }
                                        //write it
                                        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
                                        bw.write(listString);
                                        bw.close();
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                    if (events.get(i).getAudioId()==null||events.get(i).getAudioId().equals(new Long(0))){
                                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                        r.play();
                                    }else{
                                        File file=null;
                                        try {
                                            String DownloadUrl="http://192.168.1.38:8081/utils/getFile/"+events.get(i).getAudioId().toString();
                                            String fileName= "newEventAudio.3gp";
                                            String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                                            File dir = new File (root + "/gl");
                                            if(dir.exists()==false) {
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
                                            String audioPath=file.toString();
                                            mPlayer = new MediaPlayer();

                                            mPlayer.setDataSource(audioPath);
                                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                    isplayed=false;
                                                }
                                            });
                                            mPlayer.prepare();
                                            isplayed=true;
                                            mPlayer.start();

                                            while (isplayed && !isCancelled()){
                                                if (isCancelled()) break;
                                            }
                                            isplayed=false;
                                            if (isCancelled()){
                                                mPlayer.stop();
                                                mPlayer.release();
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
                            isplayed=false;
                            super.onCancelled();
                        }

                        @Override
                        protected void onCancelled(Void aVoid) {
                            isplayed=false;
                            try{
                                mPlayer.stop();
                                mPlayer.release();
                                mPlayer=null;
                            }catch(Exception e){
                                e.printStackTrace();
                            }

                            super.onCancelled(aVoid);
                        }
                    }.execute(events);

                }catch(Exception e){
                    e.printStackTrace();
                }

    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    //deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}

