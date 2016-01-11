package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by п on 31.12.2015.
 */
public class EventListActivity extends ActionBarActivity implements View.OnClickListener,AdapterView.OnItemClickListener {


    Toolbar mActionBarToolbar;
    ListView lvEvents;

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        SimpleGeoCoords eLocation=new SimpleGeoCoords(0,0,0);
        if (getIntent().hasExtra("location")) {

             eLocation = (SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
        }
            try{
                GetEventParams params=new GetEventParams();
                params.setURL("http://188.227.16.166:8080/event/getNearestEvents");
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
}
