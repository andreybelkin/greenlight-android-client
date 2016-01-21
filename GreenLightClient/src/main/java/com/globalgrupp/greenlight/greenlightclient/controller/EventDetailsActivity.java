package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
    ProgressBar progress;

    private String videoFilePath;
    private Button btnPlayVideo;

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
        TableRow tr=(TableRow) findViewById(R.id.trImageRow);
        ViewGroup.LayoutParams llParams= tr.getLayoutParams();
        llParams.height=0;
        tr.setLayoutParams(llParams);

        TableRow trAudiorow=(TableRow) findViewById(R.id.trAudioRow);
        ViewGroup.LayoutParams llAudioparams=trAudiorow.getLayoutParams();
        llAudioparams.height=0;
        trAudiorow.setLayoutParams(llAudioparams);
        if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.NONE){
            ImageButton ibShare=(ImageButton)findViewById(R.id.ibShare);
            ibShare.setVisibility(View.INVISIBLE);
        }

        if (getIntent().hasExtra("eventId")){

            try{
                refreshFields();
                //todo асинхронная загрузка
                if (currentEvent.getAudioId()!=null&&!currentEvent.getAudioId().equals(new Long(0)) ){
//                    "http://192.168.1.33:8080/utils/downloadFile?id=

                    audioFilePath=new FileDownloadTask().execute("http://192.168.1.33:8080/utils/getFile/"+currentEvent.getAudioId().toString(),"3gp").get();
                    trAudiorow=(TableRow) findViewById(R.id.trAudioRow);
                    llAudioparams=trAudiorow.getLayoutParams();
                    llAudioparams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
                    trAudiorow.setLayoutParams(llAudioparams);
                    btnPlayAudio=(ImageButton)findViewById(R.id.btnPlayAudio);
                    progress=(ProgressBar)findViewById(R.id.pbAudio);
                    btnPlayAudio.setVisibility(View.VISIBLE);
                    btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onPlay(mStartPlaying);
                            if (mStartPlaying) {
                                progress.setMax(mPlayer.getDuration());
                                //todo set pause image
                            } else {
                                //todo set play image
                            }
                            mStartPlaying = !mStartPlaying;
                        }
                    });
                }

                if (currentEvent.getPhotoIds()!=null&&currentEvent.getPhotoIds().size()>0){
                    List<Long> photoIds=currentEvent.getPhotoIds();
                    for (int i=0;i<photoIds.size();i++){
                        final String photoFilePath=new FileDownloadTask().execute("http://192.168.1.33:8080/utils/getFile/"+photoIds.get(i),"jpg").get();

                        ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
                        phLayoutParams.height =150;
                        findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
                        findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
                        LinearLayout llImages=(LinearLayout)findViewById(R.id.llImages);
                        ImageView ivNew=new ImageView(getApplicationContext());
                        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(150,150);
                        ivNew.setLayoutParams(layoutParams);
                        //ivNew.setBackgroundColor(Color.parseColor("#D8D8DA"));
                        ivNew.setPadding(5,5,5,5);
                        llImages.addView(ivNew);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath.replace("file:", ""), bmOptions);
                        Bitmap bmPhoto= Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                        ivNew.setImageBitmap(bmPhoto);
                        ivNew.setClickable(true);
                        final String path=photoFilePath;
                        ivNew.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://"+path), "image/*");
                                startActivity(intent);
                            }
                        });
                    }

                }else {

                }
                if (currentEvent.getVideoId()!=null&&!currentEvent.getVideoId().equals(new Long(0))){
                    videoFilePath=new FileDownloadTask().execute("http://192.168.1.33:8080/utils/getFile/"+currentEvent.getVideoId().toString(),"3gp").get();

                    ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
                    phLayoutParams.height = 150;
                    findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
                    findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 150, 150, true);
                    ImageView ivVideoPreview=(ImageView) findViewById(R.id.ivForVideo);
                    ivVideoPreview.setPadding(0,5,0,0);
                    //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
                    ivVideoPreview.setImageBitmap(bmPhoto);
                    ImageButton btnPlayVideo=(ImageButton)findViewById(R.id.btnVideoPlay);
                    btnPlayVideo.setImageResource(R.drawable.icon_play_white);
                    final String path=videoFilePath;
                    btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://"+path), "video/*");
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

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            try{
                while (!stop.get()) {
                    progress.setProgress(mPlayer.getCurrentPosition());
                    Thread.sleep(200);
                }
            }catch (Exception e){
                Log.e("",e.getMessage());
            }

        }
    }
    private MediaObserver observer = null;
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(audioFilePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try{
                        observer.stop();
                        progress.setProgress(mp.getCurrentPosition());
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    //btnPlayAudio.performClick();
                }
            });
            observer = new MediaObserver();
            mPlayer.prepare();
            mPlayer.start();

            new Thread(observer).start();
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

                new AlertDialog.Builder(this)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Поделиться")
                        .setMessage("Вы уверены что хотите поделиться событием в вконтакте?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                                    Toast toast = Toast.makeText(getApplicationContext(),
                                                            "Событие опубликовано в вконтакте", Toast.LENGTH_SHORT);
                                                    toast.show();
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
                            }

                        })
                        .setNegativeButton("Нет", null)
                        .show();


            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.FACEBOOK){
                new AlertDialog.Builder(this)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Поделиться")
                        .setMessage("Вы уверены что хотите поделиться событием в Facebook'е?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try{
                                    Bitmap bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher);

                                    SharePhoto photo=new SharePhoto.Builder().setBitmap(bm).build();
                                    SharePhotoContent photoContent=new SharePhotoContent.Builder().addPhoto(photo).build();
                                    ShareApi shareApi=new ShareApi(photoContent);
                                    shareApi.setMessage(currentEvent.getMessage());
                                    shareApi.share( new FacebookCallback<Sharer.Result>() {
                                        @Override
                                        public void onSuccess(Sharer.Result result) {
                                            Toast toast = Toast.makeText(getApplicationContext(),
                                                    "Событие опубликовано в Facebook'е", Toast.LENGTH_SHORT);
                                            toast.show();
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
                            }

                        })
                        .setNegativeButton("Нет", null)
                        .show();


            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.TWITTER){
                new AlertDialog.Builder(this)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Поделиться")
                        .setMessage("Вы уверены что хотите поделиться событием в Twitter'е?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Событие опубликовано в Twitter'е", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }

                        })
                        .setNegativeButton("Нет", null)
                        .show();

            }

        }

    }

}
