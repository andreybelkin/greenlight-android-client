package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Intent;
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
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.Event;
import com.globalgrupp.greenlight.greenlightclient.classes.GetEventParams;
import com.globalgrupp.greenlight.greenlightclient.classes.GetEventsOperation;
import com.globalgrupp.greenlight.greenlightclient.classes.SimpleGeoCoords;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    protected LocationManager locationManager;

    private HashMap<String, Long> mMarkers = new HashMap<String, Long>();

    protected GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_maps);
        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        setActionBarEvents(mActionBarToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        setUpMapIfNeeded();
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
        MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
        editMenuItem.setOnMenuItemClickListener(this);
        MenuItem eventListMenuItem=menu.findItem(R.id.action_event_list);
        eventListMenuItem.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }


    protected void onStop() {
        mGoogleApiClient.disconnect();
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
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng myLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLoc, 15);
                mMap.animateCamera(cameraUpdate);

                GetEventParams params=new GetEventParams();
                params.setURL("http://192.168.100.14:8080/event/getNearestEvents");
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
                    mGoogleApiClient);
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
            }

            //startIntent.putExtra("addres",addres.get(0));

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
