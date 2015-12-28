package com.globalgrupp.greenlight.greenlightclient.controller;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.CreateEventOperation;
import com.globalgrupp.greenlight.greenlightclient.classes.CreateEventParams;
import com.globalgrupp.greenlight.greenlightclient.classes.SimpleGeoCoords;

import java.util.List;
import java.util.Locale;

/**
 * Created by п on 28.12.2015.
 */
public class NewEventActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{

    private Toolbar mActionBarToolbar;

    private SimpleGeoCoords eLocation;
    private Address eAddres;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.createevent);
            //getIntent().hasExtra
            //getIntent().getSerializableExtra
            if (getIntent().hasExtra("location")){
                eLocation=(SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
            }

            Geocoder gc=new Geocoder(this, Locale.getDefault());
            List<Address> addres= gc.getFromLocation(eLocation.getLatitude(),eLocation.getLongtitude(),1);//по идее хватит и одного адреса
            eAddres=addres.get(0);

            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            setSupportActionBar(mActionBarToolbar);
            setEvents();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setEvents(){
        final Button createEventButton= (Button) findViewById(R.id.btnCreateEvent);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String serverURL = "http://192.168.1.38:8080/event/createEvent";//todo config
                    // Use AsyncTask execute Method To Prevent ANR Problem
                    EditText et=(EditText) findViewById(R.id.etEventText);
                    CreateEventParams params=new CreateEventParams(serverURL,eLocation.getLongtitude(),eLocation.getLatitude(),et.getText().toString());
                    new CreateEventOperation().execute(params);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }
}
