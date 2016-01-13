package com.globalgrupp.greenlight.greenlightclient.controller;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by п on 28.12.2015.
 */
public class EventDetailsActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mActionBarToolbar;
    private ListView lvComments;
    private Event currentEvent;


    private String audioFilePath;
    private MediaPlayer mPlayer = null;
    private Button btnPlayAudio;
    boolean mStartPlaying = true;

    private String videoFilePath;
    private Button btnPlayVideo;


    private String photoFilePath;
    private Button btnShowPhoto;

    private TextView tvEventDate;
    DateFormat df = new SimpleDateFormat("HH:mm");

    AlertDialog.Builder alertDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_details);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //getSupportActionBar().setTitle("");

        if (getIntent().hasExtra("eventId")){

            try{
                refreshFields();
                //todo асинхронная загрузка
                if (currentEvent.getAudioId()!=null&&!currentEvent.getAudioId().equals(new Long(0)) ){
//                    "http://192.168.100.14:8080/utils/downloadFile?id=
                    audioFilePath=new FileDownloadTask().execute("http://192.168.100.14:8080/utils/getFile/"+currentEvent.getAudioId().toString(),"3gp").get();
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
                if (currentEvent.getPhotoId()!=null&&!currentEvent.getPhotoId().equals(new Long(0))){
                    photoFilePath=new FileDownloadTask().execute("http://192.168.100.14:8080/utils/getFile/"+currentEvent.getPhotoId().toString(),"jpg").get();
                    btnShowPhoto=(Button) findViewById(R.id.btnShowPhoto);
                    btnShowPhoto.setVisibility(View.VISIBLE);
                    btnShowPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://"+photoFilePath), "image/*");
                            startActivity(intent);
                        }
                    });
                }
                if (currentEvent.getVideoId()!=null&&!currentEvent.getPhotoId().equals(new Long(0))){
                    videoFilePath=new FileDownloadTask().execute("http://192.168.100.14:8080/utils/getFile/"+currentEvent.getVideoId().toString(),"3gp").get();
                    btnPlayVideo=(Button) findViewById(R.id.btnPlayVideo);
                    btnPlayVideo.setVisibility(View.VISIBLE);
                    btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoFilePath));
                            intent.setDataAndType(Uri.parse(videoFilePath), "video/*");
                            startActivity(intent);
                        }
                    });
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        ImageButton ibMap=(ImageButton) findViewById(R.id.ibMap);
        ibMap.setOnClickListener(this);
        ImageButton ibComment=(ImageButton)findViewById(R.id.ibComment);
        ibComment.setOnClickListener(this);

        //initCommentDialog();

    }

    private void refreshFields(){
        try{
            GetEventParams params=new GetEventParams();
            params.setURL("http://192.168.100.14:8080/event/getEvent");
            Long id=(Long)getIntent().getExtras().getSerializable("eventId");
            params.setEventId(id );

            List<Event> events=new GetEventsOperation().execute(params).get();
            currentEvent=events.get(0);
            TextView eventMessageTV=(TextView)findViewById(R.id.eventMessage);
            eventMessageTV.setText(currentEvent.getMessage());
            TextView eventCreateDate=(TextView)findViewById(R.id.tvEventCreateDate);
            eventCreateDate.setText(df.format(currentEvent.getCreateDate()));
            TextView eventStreetName=(TextView)findViewById(R.id.tvStreetName);
            eventStreetName.setText(currentEvent.getStreetName());
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void initCommentDialog(){
        alertDialog=new AlertDialog.Builder(EventDetailsActivity.this);
        final EditText input = new EditText(EventDetailsActivity.this);


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialog.setView(input);
        alertDialog.setCancelable(true);

        alertDialog.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Comment sendData=new Comment();
                sendData.setEvent(currentEvent);
                sendData.setMessage(input.getText().toString());
                try{
                    new SaveCommentOperation().execute(sendData).get();
                    refreshFields();
                }catch (Exception e){

                }

            }
        });
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    dialogInterface.cancel();
                }
                return false;
            }
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();

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
        if (v.getId()==R.id.ibMap){
            Intent startIntent= new Intent(this, MainActivity.class);
            startActivity(startIntent);
        } else if (v.getId()==R.id.ibComment){
            initCommentDialog();
        }

    }
    private class FileDownloadTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            File file=null;
            try {
                String DownloadUrl=params[0];
                String fileName= UUID.randomUUID().toString()+"."+params[1];
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
