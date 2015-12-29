package com.globalgrupp.greenlight.greenlightclient.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by п on 28.12.2015.
 */
public class EventDetailsActivity extends ActionBarActivity {

    private Toolbar mActionBarToolbar;
    private ListView lvComments;

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
                lvComments=(ListView)findViewById(R.id.listViewComments);
                ArrayList<Comment> list = new ArrayList<Comment>(currentEvent.getComments());

                CommentsAdapter commentsAdapter=new CommentsAdapter(this,list);
                lvComments.setAdapter(commentsAdapter);
                View listItem = commentsAdapter.getView(0, null, lvComments);
                listItem.measure(0, 0);
                float totalHeight = 0;
                for (int i = 0; i < commentsAdapter.getCount(); i++) {
                    totalHeight += listItem.getMeasuredHeight();
                }
                ViewGroup.LayoutParams layoutParams = lvComments.getLayoutParams();
                layoutParams.height = (int) (totalHeight + (lvComments.getDividerHeight() * (lvComments.getCount() - 1)));
                lvComments.setLayoutParams(layoutParams);
                lvComments.requestLayout();

            }catch (Exception e) {

                e.printStackTrace();
            }


        }


    }
}
