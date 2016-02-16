package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.facebook.login.LoginManager;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vk.sdk.VKSdk;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    protected LocationManager locationManager;

    private HashMap<String, Long> mMarkers = new HashMap<String, Long>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        setContentView( R.layout.activity_maps);
        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        setActionBarEvents(mActionBarToolbar);
        getSupportActionBar().setTitle("");
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        setUpMapIfNeeded();
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);

        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        try{

            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    ApplicationSettings.getInstance().getmGoogleApiClient());
            if (mLastLocation != null) {
                LatLng myLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (getIntent().hasExtra("eventCoords")){
                    SimpleGeoCoords eventCoords=(SimpleGeoCoords) getIntent().getExtras().getSerializable("eventCoords");
                    myLoc=new LatLng(eventCoords.getLatitude(),eventCoords.getLongtitude());
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLoc, 15);
                mMap.animateCamera(cameraUpdate);

                GetEventParams params=new GetEventParams();
                //params.setURL("http://192.168.1.33:8080/event/getNearestEvents");
                Long channelId= ApplicationSettings.getInstance().getChannelId();
                if (channelId!=null && !channelId.equals(new Long(0))){
                    params.setURL("http://192.168.1.33:8080/event/getEventsByChannel/"+channelId.toString());
                }else{
                    params.setURL("http://192.168.1.33:8080/event/getNearestEvents");
                }
                SharedPreferences prefs=getApplicationContext().getSharedPreferences(
                        EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                params.setRadius(new Long(prefs.getLong("event_radius",5)));
                SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
                params.setCurrentCoords(coords);
                List<Event> events=new GetEventsOperation().execute(params).get();
                for(int i=0;i<events.size();i++){
                    myLoc = new LatLng(events.get(i).getLatitude(), events.get(i).getLongitude());
                    Marker marker=mMap.addMarker(new MarkerOptions()
                            .title("Событие")
                            .snippet(events.get(i).getMessage())
                            .position(myLoc));
                    mMarkers.put(marker.getId(),events.get(i).getId());
                }
            }
            findViewById(R.id.ivDropDown).setVisibility(View.INVISIBLE);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setActionBarEvents(Toolbar mActionBarToolbar) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
        MenuItem eventListMenuItem=menu.findItem(R.id.action_event_list);
        eventListMenuItem.setOnMenuItemClickListener(this);
        MenuItem mapMenuItem=menu.findItem(R.id.action_map);
        mapMenuItem.setVisible(false);
        MenuItem settingMenuItem=menu.findItem(R.id.action_settings);
        settingMenuItem.setOnMenuItemClickListener(this);
        MenuItem logoutMenuItem=menu.findItem(R.id.action_logout);
        logoutMenuItem.setOnMenuItemClickListener(this);


        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    protected void onStop() {
        super.onStop();
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setOnInfoWindowClickListener(this);
            }
        }
    }
    public Location getLocation() {
        Location location=null;
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters

    private static final long MIN_TIME_BW_UPDATES = 100; // in Milliseconds

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    ApplicationSettings.getInstance().getmGoogleApiClient());
            SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
            Intent startIntent;
            if (item.getItemId()==R.id.action_new_event){
                startIntent= new Intent(this, NewEventActivity.class);
                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            }else if(item.getItemId()==R.id.action_event_list){
                startIntent= new Intent(this, EventListActivity.class);
                startIntent.putExtra("location", coords);
                startActivity(startIntent);
            } else if (item.getItemId()==R.id.action_logout){
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
            } else if( item.getItemId()==R.id.action_settings){
                startIntent = new Intent(this, SettingsActivity.class);
                startActivity(startIntent);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Long id = mMarkers.get(marker.getId());
        Intent startIntent = new Intent(this, EventDetailsActivity.class);
        startIntent.putExtra("eventId", id);
        startActivity(startIntent);
    }
}
