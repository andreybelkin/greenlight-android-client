package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.vk.sdk.api.*;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

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
    private ImageButton btnPlayAudio;
    boolean mStartPlaying = true;

    private String videoFilePath;
    private Button btnPlayVideo;


    private String photoFilePath;
    private Button btnShowPhoto;

    private TextView tvEventDate;
    DateFormat df = new SimpleDateFormat("HH:mm");

    AlertDialog.Builder alertDialog;

    private String TWITTER_CONSUMER_KEY="fWJW731tJv7Yk2ID2vBmIYLFR";
    private String TWITTER_CONSUMER_SECRET="VsUjFxLpzSwWycOscn4Tti9BRyGaIvWJxTEQGI48SmDRHmuDFz";

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
                Intent intent = new Intent(getApplicationContext(), EventListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //onBackPressed();
            }
        });
        //getSupportActionBar().setTitle("");

        if (getIntent().hasExtra("eventId")){

            try{
                refreshFields();
                //todo асинхронная загрузка
                if (currentEvent.getAudioId()!=null&&!currentEvent.getAudioId().equals(new Long(0)) ){
//                    "http://192.168.1.33:8080/utils/downloadFile?id=
                    audioFilePath=new FileDownloadTask().execute("http://192.168.1.33:8080/utils/getFile/"+currentEvent.getAudioId().toString(),"3gp").get();
                    btnPlayAudio=(ImageButton)findViewById(R.id.btnPlayAudio);
                    btnPlayAudio.setVisibility(View.VISIBLE);
                    btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onPlay(mStartPlaying);
                            if (mStartPlaying) {
                                //todo set pause image
                            } else {
                                //todo set play image
                            }
                            mStartPlaying = !mStartPlaying;
                        }
                    });
                }else {
                    TableRow tr=(TableRow) findViewById(R.id.trAudioRow);
                    tr.setLayoutParams(new TableRow.LayoutParams(tr.getWidth(),0));
                }
                if (currentEvent.getPhotoId()!=null&&!currentEvent.getPhotoId().equals(new Long(0))){
                    photoFilePath=new FileDownloadTask().execute("http://192.168.1.33:8080/utils/getFile/"+currentEvent.getPhotoId().toString(),"jpg").get();
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
                }else {
                    TableRow tr=(TableRow) findViewById(R.id.trPhotoRow);
                    tr.setLayoutParams(new TableRow.LayoutParams(tr.getWidth(),0));
                }
                if (currentEvent.getVideoId()!=null&&!currentEvent.getPhotoId().equals(new Long(0))){
                    videoFilePath=new FileDownloadTask().execute("http://192.168.1.33:8080/utils/getFile/"+currentEvent.getVideoId().toString(),"3gp").get();
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
                }else {
                    TableRow tr=(TableRow) findViewById(R.id.trVideoRow);
                    tr.setLayoutParams(new TableRow.LayoutParams(tr.getWidth(),0));
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        ImageButton ibMap=(ImageButton) findViewById(R.id.ibMap);
        ibMap.setOnClickListener(this);
        ImageButton ibComment=(ImageButton)findViewById(R.id.ibComment);
        ibComment.setOnClickListener(this);
        ImageButton ibShare=(ImageButton) findViewById(R.id.ibShare);
        ibShare.setOnClickListener(this);

        //initCommentDialog();

    }

    private void refreshFields(){
        try{
            GetEventParams params=new GetEventParams();
            params.setURL("http://192.168.1.33:8080/event/getEvent");
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
            SimpleGeoCoords geoCoords=new SimpleGeoCoords(currentEvent.getLongitude(),currentEvent.getLatitude(),currentEvent.getAltitude());
            startIntent.putExtra("eventCoords",geoCoords);
            startActivity(startIntent);
        } else if (v.getId()==R.id.ibComment){
            initCommentDialog();
        } else if (v.getId()==R.id.ibShare){
            if (AuthorizationType.VK==ApplicationSettings.getInstance().getAuthorizationType()){
                VKRequest request = VKApi.users().get();
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        try{
                            Long userId=response.json.getJSONArray("response").getJSONObject(0).getLong("id");
                            VKRequest postRequest = VKApi.wall().post(
                                    VKParameters.from(
                                            VKApiConst.OWNER_ID, userId.toString(),
                                            VKApiConst.MESSAGE, currentEvent.getMessage()
                                    )
                            );
                            postRequest.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);
                                    android.app.AlertDialog.Builder alertDialog=new android.app.AlertDialog.Builder(EventDetailsActivity.this);
                                    alertDialog.setMessage("Событие опубликовано в вконтакте");
                                    alertDialog.show();
                                }
                                @Override
                                public void onError(VKError error) {
                                    super.onError(error);
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(VKError error) {
                    }
                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                    }
                });
            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.FACEBOOK){
                try{
                    Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher);

                    SharePhoto photo=new SharePhoto.Builder().setBitmap(bm).build();
                    SharePhotoContent photoContent=new SharePhotoContent.Builder().addPhoto(photo).build();
                    ShareApi shareApi=new ShareApi(photoContent);
                    shareApi.setMessage(currentEvent.getMessage());
                    shareApi.share( new FacebookCallback<Sharer.Result>() {
                        @Override
                        public void onSuccess(Sharer.Result result) {
                            int i=0;
                            android.app.AlertDialog.Builder alertDialog=new android.app.AlertDialog.Builder(EventDetailsActivity.this);
                            alertDialog.setMessage("Событие опубликовано в Facebook'е");
                            alertDialog.show();
                        }

                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onError(FacebookException e) {
                            e.printStackTrace();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.TWITTER){
                try{
                    // Update status
                    twitter4j.Status response =
                    new AsyncTask<String, Void, Status>() {
                        @Override
                        protected twitter4j.Status doInBackground(String... strings) {
                            try{
                                ConfigurationBuilder builder = new ConfigurationBuilder();
                                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
                                Twitter twitter = new TwitterFactory(builder.build()).getInstance(ApplicationSettings.getInstance().getTwitterAccessToken());
                                return twitter.updateStatus(strings[0]);
                            }catch (Exception e){
                                e.printStackTrace();
                                return null;
                            }

                        }
                    }.execute(currentEvent.getMessage()).get();
                    if (response!=null){
                        android.app.AlertDialog.Builder alertDialog=new android.app.AlertDialog.Builder(EventDetailsActivity.this);
                        alertDialog.setMessage("Событие опубликовано в Twitter'е");
                        alertDialog.show();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            }

        }

    }

}
