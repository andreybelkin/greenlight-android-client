package com.globalgrupp.greenlight.greenlightclient.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by п on 28.12.2015.
 */
public class EventDetailsActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mActionBarToolbar;
    private ListView lvComments;
    private Button btnSendComment;
    private Event currentEvent;
    private EditText etComment;
    private String audioFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getIntent().hasExtra("eventId")){

            try{
                GetEventParams params=new GetEventParams();
                params.setURL("http://46.146.122.16:8081/event/getEvent");
                Long id=(Long)getIntent().getExtras().getSerializable("eventId");
                params.setEventId(id );

                List<Event> events=new GetEventsOperation().execute(params).get();
                currentEvent=events.get(0);
                TextView eventMessageTV=(TextView)findViewById(R.id.eventMessage);
                eventMessageTV.setText(currentEvent.getMessage());
                lvComments=(ListView)findViewById(R.id.listViewComments);
                ArrayList<Comment> list = new ArrayList<Comment>(currentEvent.getComments());

                if (list.size()>0){
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
                }
                if (currentEvent.getAudioId()!=null){
                    audioFilePath=new FileDownloadTask().execute("http://46.146.122.16:8081/utils/downloadFile?id="+currentEvent.getAudioId().toString()).get();
                }

            }catch (Exception e) {

                e.printStackTrace();
            }
        }
        etComment=(EditText)findViewById(R.id.etComment);
        btnSendComment=(Button) findViewById(R.id.btnSendComment);
        btnSendComment.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        Comment sendData=new Comment();
        sendData.setEvent(currentEvent);
        sendData.setMessage(etComment.getText().toString());
        new SaveCommentOperation().execute(sendData);
    }
    private class FileDownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                String DownloadUrl=params[0];
                String fileName="audiotest.3gp";
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                File dir = new File (root + "/xmls");
                if(dir.exists()==false) {
                    dir.mkdirs();
                }

                URL url = new URL(DownloadUrl); //you can write here any link
                File file = new File(dir, fileName);

                long startTime = System.currentTimeMillis();
                Log.d("DownloadManager", "download begining");
                Log.d("DownloadManager", "download url:" + url);
                Log.d("DownloadManager", "downloaded file name:" + fileName);

           /* Open a connection to that URL. */
                URLConnection ucon = url.openConnection();

           /*
            * Define InputStreams to read from the URLConnection.
            */
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

           /*
            * Read bytes to the Buffer until there is nothing more to read(-1).
            */

                byte[] data = new byte[(int) file.length()];
                try {
                    is.read(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }


           /* Convert the Bytes read to a String. */
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.flush();
                fos.close();
                Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

            } catch (Exception e) {
                Log.d("DownloadManager", "Error: " + e);
            }
            return getFilesDir().getAbsolutePath();
        }
    }
}
