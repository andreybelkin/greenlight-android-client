package com.globallgrupp.greenlight;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.globallgrupp.greenlight.classes.CreateEventOperation;
import com.globallgrupp.greenlight.classes.CreateEventParams;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters

    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds

    protected LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createevent);
        Log.i("start App ","start App");
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        final Button createEventButton= (Button) findViewById(R.id.btnCreateEvent);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationProvider = LocationManager.NETWORK_PROVIDER;
                try{
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    LocationListener locationListener = new LocationListener() {
                        public void onLocationChanged(Location location) {
                            // Called when a new location is found by the network location provider.
                            //makeUseOfNewLocation(location);
                        }

                        public void onStatusChanged(String provider, int status, Bundle extras) {}

                        public void onProviderEnabled(String provider) {}

                        public void onProviderDisabled(String provider) {}
                    };
                    Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                    if (lastKnownLocation==null){
                        lastKnownLocation=new Location(locationProvider);
                        lastKnownLocation.setAltitude(-1);
                        lastKnownLocation.setLatitude(-1);
                    }

                String serverURL = "http://10.0.2.2:8080/event/createEvent";//todo config

                // Use AsyncTask execute Method To Prevent ANR Problem
                    EditText et=(EditText) findViewById(R.id.etEventText);
                CreateEventParams params=new CreateEventParams(serverURL,lastKnownLocation.getLongitude(),lastKnownLocation.getLatitude(),et.getText().toString());
                new CreateEventOperation().execute(params);
                }catch (Exception e){
                    Log.i("location error",e.getMessage()+e.getStackTrace()[0].toString());
                    e.printStackTrace();
                }
            }
        });
    }




// Register the listener with the Location Manager to receive location updates

}
