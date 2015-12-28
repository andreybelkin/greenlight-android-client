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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity extends ActionBarActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,AdapterView.OnItemClickListener, MenuItem.OnMenuItemClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    protected LocationManager locationManager;

    protected GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_maps);
        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        setActionBarEvents(mActionBarToolbar);
        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


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
                final CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myLoc)      // Sets the center of the map to Mountain View
                        .zoom(13).build();              // Sets the zoom

                GetEventsOperation eventOperation=new GetEventsOperation();
                GetEventParams params=new GetEventParams();
                params.setUrl("http://192.168.1.38:8080/event/getNearestEvents");
                SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
                params.setCurrentCoords(coords);
                eventOperation.execute(params);
                List<Event> events=eventOperation.get();
                for(int i=0;i<events.size();i++){
                    myLoc = new LatLng(events.get(i).getLatitude(), events.get(i).getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .title("Событие")
                            .snippet(events.get(i).getMessage())
                            .position(myLoc));
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
        try{
            Intent startIntent = new Intent(this, NewEventActivity.class);
            startIntent.putExtra("", "");
            startActivity(startIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            SimpleGeoCoords coords=new SimpleGeoCoords(mLastLocation.getLongitude(),mLastLocation.getLatitude(),mLastLocation.getAltitude());
            Intent startIntent = new Intent(this, NewEventActivity.class);
            startIntent.putExtra("location", coords);
            //startIntent.putExtra("addres",addres.get(0));
            startActivity(startIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
