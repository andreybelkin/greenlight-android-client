package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.globalgrupp.greenlight.greenlightclient.Application;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by п on 31.12.2015.
 */
public class EventListActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, View.OnClickListener,AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener, GoogleApiClient.OnConnectionFailedListener {


    Toolbar mActionBarToolbar;
    ListView lvEvents;
    String keyToken;
    MediaPlayer mPlayer;
    private boolean isplayed=false;

    protected GoogleApiClient mGoogleApiClient;

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.event_list);
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            //mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_launcher);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();

           //refreshEventList();
        }catch(Exception e){
            e.printStackTrace();
        }


    }
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        refreshEventList();
    }

    public void refreshEventList(){

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
        SimpleGeoCoords eLocation=new SimpleGeoCoords(0,0,0);
        if (getIntent().hasExtra("location")) {
            eLocation = (SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
        }
        try{
            GetEventParams params=new GetEventParams();
            params.setURL("http://192.168.100.14:8080/event/getNearestEvents");
            params.setCurrentCoords(eLocation);
            List<Event> events=new GetEventsOperation().execute(params).get();
            lvEvents=(ListView)findViewById(R.id.listViewEvents);
            EventsAdapter commentsAdapter=new EventsAdapter(this,(ArrayList)events);
            lvEvents.setAdapter(commentsAdapter);
            View listItem = commentsAdapter.getView(0, null, lvEvents);
            listItem.measure(0, 0);
            float totalHeight = 0;
            for (int i = 0; i < commentsAdapter.getCount(); i++) {
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams layoutParams = lvEvents.getLayoutParams();
            layoutParams.height = (int) (totalHeight + (lvEvents.getDividerHeight() * (lvEvents.getCount() - 1)));
            lvEvents.setLayoutParams(layoutParams);
            lvEvents.requestLayout();
            lvEvents.setOnItemClickListener(this);


            new AsyncTask<List<Event>, Void, Void>() {
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
                                        String DownloadUrl="http://192.168.100.14:8080/utils/getFile/"+events.get(i).getAudioId().toString();
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
                                        MediaPlayer mPlayer = new MediaPlayer();

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

                                        while (isplayed){

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                        }
                    }
                    return null;
                }
            }.execute(events);


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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
        editMenuItem.setOnMenuItemClickListener(this);
        MenuItem eventListMenuItem=menu.findItem(R.id.action_event_list);
        eventListMenuItem.setOnMenuItemClickListener(this);
        eventListMenuItem.setVisible(false);
        MenuItem mapItem=menu.findItem(R.id.action_map);
        mapItem.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
            Intent startIntent;
            if (menuItem.getItemId()==R.id.action_new_event){
                startIntent= new Intent(this, NewEventActivity.class);
                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            }else if(menuItem.getItemId()==R.id.action_event_list){
                startIntent= new Intent(this, EventListActivity.class);
                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            } else if (menuItem.getItemId()==R.id.action_map){
                startIntent= new Intent(this, MainActivity.class);
                startIntent.putExtra("location", coords);
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
        }))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
