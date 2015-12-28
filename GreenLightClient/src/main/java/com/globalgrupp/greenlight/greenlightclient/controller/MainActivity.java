package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Intent;
import android.location.Criteria;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
        Location mLastLocation=null;
        try{
             mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            mMap.setMyLocationEnabled(true);
            //mLastLocation=mMap.getMyLocation();


            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);

            if(location!=null){
                // Getting latitude of the current location
                double latitude = location.getLatitude();

                // Getting longitude of the current location
                double longitude = location.getLongitude();

                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(latitude, longitude);

                LatLng myPosition = new LatLng(latitude, longitude);

                mMap.addMarker(new MarkerOptions().position(myPosition).title("Start"));
            }
        }catch(SecurityException e){
            int k=0;
        }
        int i=0;

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
                mMap.addMarker(new MarkerOptions()
                        .title("Me")
                        .snippet("My location")
                        .position(myLoc));
            }
        }catch(SecurityException e){}



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
            Intent startIntent = new Intent(this, NewEventActivity.class);
            startIntent.putExtra("", "");
            startActivity(startIntent);
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }


//
//    protected LocationManager locationManager;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.createevent);
//        Log.i("start App ","start App");
//        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//        final Button createEventButton= (Button) findViewById(R.id.btnCreateEvent);
//        createEventButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String locationProvider = LocationManager.NETWORK_PROVIDER;
//                try{
//                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//                    LocationListener locationListener = new LocationListener() {
//                        public void onLocationChanged(Location location) {
//                            // Called when a new location is found by the network location provider.
//                            //makeUseOfNewLocation(location);
//                        }
//
//                        public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//                        public void onProviderEnabled(String provider) {}
//
//                        public void onProviderDisabled(String provider) {}
//                    };
//                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
//                    if (lastKnownLocation==null){
//                        lastKnownLocation=new Location(locationProvider);
//                        lastKnownLocation.setAltitude(-1);
//                        lastKnownLocation.setLatitude(-1);
//                    }
//
//                    String serverURL = "http://10.0.2.2:8080/event/createEvent";//todo config
//
//                    // Use AsyncTask execute Method To Prevent ANR Problem
//                    EditText et=(EditText) findViewById(R.id.etEventText);
//                    CreateEventParams params=new CreateEventParams(serverURL,lastKnownLocation.getLongitude(),lastKnownLocation.getLatitude(),et.getText().toString());
//                    new CreateEventOperation().execute(params);
//                }catch (Exception e){
//                    Log.i("location error",e.getMessage()+e.getStackTrace()[0].toString());
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
}
