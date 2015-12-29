package com.globalgrupp.greenlight.greenlightclient.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.Event;
import com.globalgrupp.greenlight.greenlightclient.classes.GetEventParams;
import com.globalgrupp.greenlight.greenlightclient.classes.GetEventsOperation;

import java.util.List;

/**
 * Created by п on 28.12.2015.
 */
public class EventDetailsActivity extends ActionBarActivity {

    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(mActionBarToolbar);

        if (getIntent().hasExtra("eventId")){

            try{
            GetEventParams params=new GetEventParams();
            params.setURL("http://192.168.1.38:8080/event/getEvent");
            Long id=(Long)getIntent().getExtras().getSerializable("eventId");
            params.setEventId(id );

                List<Event> events=new GetEventsOperation().execute(params).get();
                Event currentEvent=events.get(0);
                TextView eventMessageTV=(TextView)findViewById(R.id.eventMessage);
                eventMessageTV.setText(currentEvent.getMessage());
                //todo comments
            }catch (Exception e) {

                e.printStackTrace();
            }


        }


    }
}
