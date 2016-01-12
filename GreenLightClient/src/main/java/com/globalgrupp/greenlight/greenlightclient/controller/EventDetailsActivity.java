package com.globalgrupp.greenlight.greenlightclient.controller;

import android.media.MediaPlayer;
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

import java.io.*;
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

    private MediaPlayer mPlayer = null;
    private Button btnPlayAudio;
    boolean mStartPlaying = true;

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
                params.setURL("http://192.168.100.14:8080/event/getEvent");
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
                if (currentEvent.getAudioId()!=null&&!currentEvent.getAudioId().equals(new Long(0)) ){
//                    "http://192.168.100.14:8080/utils/downloadFile?id=
                    audioFilePath=new FileDownloadTask().execute("http://192.168.100.14:8080/utils/getFiles/"+currentEvent.getAudioId().toString()).get();
                    btnPlayAudio=(Button)findViewById(R.id.btnPlayAudio);
                    btnPlayAudio.setVisibility(View.VISIBLE);
                    btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onPlay(mStartPlaying);
                            if (mStartPlaying) {
                                btnPlayAudio.setText("Стоп");
                            } else {
                                btnPlayAudio.setText("Воспроизвести аудио");
                            }
                            mStartPlaying = !mStartPlaying;
                        }
                    });
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        etComment=(EditText)findViewById(R.id.etComment);
        btnSendComment=(Button) findViewById(R.id.btnSendComment);
        btnSendComment.setOnClickListener(this);
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(audioFilePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnPlayAudio.performClick();
                }
            });
            mPlayer.prepare();
            mPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
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
            File file=null;
            try {
                String DownloadUrl=params[0];
                String fileName="audiotest2.3gp";
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                File dir = new File (root + "/gl");
                if(dir.exists()==false) {
                    dir.mkdirs();
                }
                URL url = new URL(DownloadUrl); //you can write here any link
                file = new File(dir, fileName);

                long startTime = System.currentTimeMillis();
                Log.d("DownloadManager", "download begining");
                Log.d("DownloadManager", "download url:" + url);
                Log.d("DownloadManager", "downloaded file name:" + fileName);

                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();

                byte[] dataFile = buffer.toByteArray();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(dataFile);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                Log.d("DownloadManager", "Error: " + e);
            }
            return file.toString();
        }
    }
}
