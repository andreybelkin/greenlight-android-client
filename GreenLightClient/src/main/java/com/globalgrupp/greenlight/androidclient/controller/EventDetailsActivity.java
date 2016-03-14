package com.globalgrupp.greenlight.androidclient.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.model.*;
import com.globalgrupp.greenlight.androidclient.util.GCMRegistrationHelper;
import com.vk.sdk.api.*;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by п on 28.12.2015.
 */
public class EventDetailsActivity extends ActionBarActivity implements View.OnClickListener {

    private Toolbar mActionBarToolbar;
    private ListView lvComments;
    private Event currentEvent;


    private String audioFilePath;
    private String commentAudioFilePath;

    private MediaPlayer mPlayer = null;
    private ImageButton btnPlayAudio;
    boolean mStartPlaying = true;

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
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        setContentView(R.layout.event_details);
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);

        setSupportActionBar(mActionBarToolbar);
        ListView lv=(ListView)findViewById(R.id.listViewComments);
        lv.setFocusable(false);
        findViewById(R.id.ivDropDown).setVisibility(View.INVISIBLE);

        ((TextView)findViewById(R.id.eventMessage)).setMovementMethod(new ScrollingMovementMethod());
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EventListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //onBackPressed();
            }
        });
        mActionBarToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (view.getId()==android.R.id.home){
                    ScrollView scrollView=(ScrollView)findViewById(R.id.scrollView);
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                //}
            }
        });
        mActionBarToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==android.R.id.home){
                    ScrollView scrollView=(ScrollView)findViewById(R.id.scrollView);
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                }
                return false;
            }
        });

        ImageButton ibIconUp=(ImageButton) findViewById(R.id.ibIconUp);
        ibIconUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScrollView scrollView=(ScrollView)findViewById(R.id.scrollView);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
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
        if (ApplicationSettings.getAuthorizationType() == AuthorizationType.NONE){
            ImageButton ibShare=(ImageButton)findViewById(R.id.ibShare);
            ibShare.setImageResource(R.mipmap.icon_share_grey);
            ibShare.setEnabled(false);

            ImageButton bComment=(ImageButton)findViewById(R.id.ibComment);
            //ibShare.setVisibility(View.INVISIBLE);
            bComment.setVisibility(View.GONE);
        }

        ImageButton ibMap=(ImageButton) findViewById(R.id.ibMap);
        ibMap.setOnClickListener(this);
        ImageButton ibComment=(ImageButton)findViewById(R.id.ibComment);
        ibComment.setOnClickListener(this);
        ImageButton ibShare=(ImageButton) findViewById(R.id.ibShare);
        ibShare.setOnClickListener(this);
        ScrollView scrollView=(ScrollView)findViewById(R.id.scrollView);
        scrollView.fullScroll(ScrollView.FOCUS_UP);

        if (getIntent().hasExtra("eventId")){
//
            try{
                refreshFields();
                //todo асинхронная загрузка
//                Thread thread=new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                        try{
                            if (currentEvent.getAudioId()!=null&&!currentEvent.getAudioId().equals(new Long(0)) ){
                                if (currentEvent.getAudioId().equals(new Long(-1))){
                                        audioFilePath=currentEvent.getAudioPath();
                                }else{
                                    audioFilePath=new FileDownloadTask().execute(currentEvent.getAudioId().toString(),currentEvent.getUniqueGUID(),"3gp").get();
                                }

                                llAudioparams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
                                trAudiorow.setLayoutParams(llAudioparams);
                                final ImageButton btnPlayAudio=(ImageButton)findViewById(R.id.btnPlayAudio);
                                final ProgressBar progress=(ProgressBar)findViewById(R.id.pbAudio);
                                btnPlayAudio.setVisibility(View.VISIBLE);
                                btnPlayAudio.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (lastPressedPlay!=null && !lastPressedPlay.equals(this)){
                                            mStartPlaying=true;
                                        }else if (lastPressedPlay==null) {
                                            mStartPlaying=true;
                                        } else if (lastPressedPlay.equals(this)){
                                        } else{
                                            mStartPlaying=false;
                                        }
                                        lastPressedPlay=this;
                                        onPlay(mStartPlaying,audioFilePath,progress);
                                        if (mStartPlaying) {
                                            progress.setMax(mPlayer.getDuration());
                                            //btnPlayAudio.setImageResource(R.drawable.icon_audio_stop);
                                        } else {
                                            btnPlayAudio.setImageResource(R.drawable.icon_audio_play);
                                            progress.setProgress(0);
                                        }
                                        mStartPlaying = !mStartPlaying;
                                    }
                                });
                            }

                            if (currentEvent.getPhotoIds()!=null&&currentEvent.getPhotoIds().size()>0){
                                List<Long> photoIds=currentEvent.getPhotoIds();
                                for (int i=0;i<photoIds.size();i++){
                                    String photoFilePath="";
                                    if (photoIds.get(i).equals(new Long(-1))){
                                        photoFilePath=currentEvent.getPhotoPathList().get(i);
                                    }else{
                                        photoFilePath=new FileDownloadTask().execute(photoIds.get(i).toString(),currentEvent.getUniqueGUID(),"jpg").get();
                                    }


                                    ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
                                    phLayoutParams.height =150;
                                    findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
                                    findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
                                    LinearLayout llImages=(LinearLayout)findViewById(R.id.llImages);
                                    ImageView ivNew=new ImageView(getApplicationContext());
                                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(150,150);
                                    ivNew.setLayoutParams(layoutParams);
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
                                if (currentEvent.getVideoId().equals(new Long(-1))){
                                    videoFilePath=currentEvent.getVideoPath();
                                }else{
                                    videoFilePath=new FileDownloadTask().execute(currentEvent.getVideoId().toString(),currentEvent.getUniqueGUID(),"3gp").get();
                                }

                                ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
                                phLayoutParams.height = 150;
                                findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
                                findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
                                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath,
                                        MediaStore.Images.Thumbnails.MINI_KIND);
                                Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 150, 150, true);
                                ImageView ivVideoPreview=(ImageView) findViewById(R.id.ivForVideo);
                                ivVideoPreview.setPadding(0,5,0,0);
                                ivVideoPreview.setImageBitmap(bmPhoto);
                                ImageButton btnPlayVideo=(ImageButton)findViewById(R.id.btnVideoPlay);
                                btnPlayVideo.setImageResource(R.drawable.video_wh);
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
                        }catch (Exception e){
                            e.printStackTrace();
                        }

//                    }
//                });
//                thread.start();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getIntent().hasExtra("openComment")){
            initCommentDialog();
        }
        if (getIntent().hasExtra("openShare")){
            share();
        }

    }

    protected Rect getLocationOnScreen(EditText mEditText) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        mEditText.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mEditText.getWidth();
        mRect.bottom = location[1] + mEditText.getHeight();

        return mRect;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        boolean handleReturn = super.dispatchTouchEvent(ev);

        View view = getCurrentFocus();

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if(view instanceof EditText){
            //View innerView = getCurrentFocus();
            EditText innerView=(EditText)findViewById(R.id.etEventText);
            if (ev.getAction() == MotionEvent.ACTION_UP &&
                    !getLocationOnScreen(innerView).contains(x, y)) {

                InputMethodManager input = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                input.hideSoftInputFromWindow(getWindow().getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }

        return handleReturn;
    }
    Object lastPressedPlay;

    private void refreshFields(){
        try{
            GetEventParams params=new GetEventParams();
            params.setURL("http://188.227.16.166:8080/event/getEvent");
            Long id=(Long)getIntent().getExtras().getSerializable("eventId");
            params.setEventId(id );
            if (id!=null){
                List<Event> events=new GetEventsOperation().execute(params).get();
                currentEvent=events.get(0);
            }else{
                currentEvent=(Event)getIntent().getExtras().getSerializable("eventObject");
                ImageButton commentButton=(ImageButton)findViewById(R.id.ibComment);
                commentButton.setVisibility(View.INVISIBLE);
            }

            TextView eventMessageTV=(TextView)findViewById(R.id.eventMessage);
            eventMessageTV.setText(currentEvent.getMessage());
            TextView eventCreateDate=(TextView)findViewById(R.id.tvEventCreateDate);
            eventCreateDate.setText(df.format(currentEvent.getCreateDate()));
            TextView eventStreetName=(TextView)findViewById(R.id.tvStreetName);
            eventStreetName.setText(currentEvent.getStreetName());
            lvComments=(ListView)findViewById(R.id.listViewComments);
            final ArrayList<Comment> list = new ArrayList<Comment>(currentEvent.getComments());

            if (list.size()>0){
                LinearLayout llComments=(LinearLayout)findViewById(R.id.llComments);
                final LayoutInflater inflater;
                inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (llComments.getChildCount() > 0){
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
                            llComments.removeAllViews();
//                        }
//                    });
                }

//                Thread thread=new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                        try{


                            for (int z=0;z<list.size();z++){
                                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.lv_comments_item ,null);

                                final View convertView=layout;
                                Comment commentsItem=list.get(z);
                                TextView tvCommentMessage=(TextView) convertView.findViewById(R.id.tvCommentMessage);
                                TextView tvDateView = (TextView) convertView.findViewById(R.id.tvCommentDate);
                                tvCommentMessage.setText(commentsItem.getMessage());
                                DateFormat df = new SimpleDateFormat("HH:mm");
                                tvDateView.setText(df.format(commentsItem.getCreateDate()));
                                try{
                                    if (commentsItem.getAudioId()!=null &&!commentsItem.getAudioId().equals(new Long(0))){
                                        final ProgressBar progressBar=(ProgressBar)convertView.findViewById(R.id.pbAudio);
                                        final ImageButton btnPlayAudioComment=(ImageButton)convertView.findViewById(R.id.btnPlayAudio);
                                        final String audioFilePath= new FileDownloadTask().execute(commentsItem.getAudioId().toString(),"ci","3gp").get();
                                        btnPlayAudioComment.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {

                                                try{
                                                    if (lastPressedPlay!=null && !lastPressedPlay.equals(this)){
                                                        mStartPlaying=true;
                                                    }else if (lastPressedPlay==null) {
                                                        mStartPlaying=true;
                                                    } else if (lastPressedPlay.equals(this)){
                                                    } else{
                                                        mStartPlaying=false;
                                                    }
                                                    lastPressedPlay=this;

                                                    onPlay(mStartPlaying,audioFilePath,progressBar);
                                                    if (mStartPlaying) {
                                                        progressBar.setMax(mPlayer.getDuration());
                                                        //btnPlayAudioComment.setImageResource(R.drawable.icon_audio_stop);
                                                    } else {
                                                        btnPlayAudioComment.setImageResource(R.drawable.icon_audio_play);
                                                        progressBar.setProgress(0);
                                                    }
                                                    mStartPlaying = !mStartPlaying;
                                                }catch(Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }else{
                                        TableRow trAudioRow=(TableRow)convertView.findViewById(R.id.tableRow);
                                        ViewGroup.LayoutParams layoutParams= trAudioRow.getLayoutParams();
                                        layoutParams.height=0;
                                        trAudioRow.setLayoutParams(layoutParams);
                                    }
                                    if (commentsItem.getPhotoIds()!=null && commentsItem.getPhotoIds().size()>0){

                                        List<Long> photoIds=commentsItem.getPhotoIds();
                                        for (int i=0;i<photoIds.size();i++){
                                            try{
                                                final String photoFilePath=new FileDownloadTask().execute(photoIds.get(i).toString(),"ci","jpg").get();
                                                LinearLayout llImages=(LinearLayout)convertView.findViewById(R.id.llImages);
                                                ImageView ivNew=new ImageView(getApplicationContext());
                                                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(150,150);
                                                ivNew.setLayoutParams(layoutParams);
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
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }
                                    }else{
                                        TableRow trVideoRow=(TableRow) convertView.findViewById(R.id.tableRow2);
                                        ViewGroup.LayoutParams layoutParams=trVideoRow.getLayoutParams();
                                        layoutParams.height=0;
                                        trVideoRow.setLayoutParams(layoutParams);
                                    }
                                    if (commentsItem.getVideoId()!=null && !commentsItem.getVideoId().equals(new Long(0))){
                                        try{
                                            final String videoFilePath=new FileDownloadTask().execute(commentsItem.getVideoId().toString(),"ci","3gp").get();
                                            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath,
                                                    MediaStore.Images.Thumbnails.MINI_KIND);
                                            if (thumbnail!=null){
                                                Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 150, 150, true);
                                                ImageView ivVideoPreview=(ImageView) convertView.findViewById(R.id.ivForVideo);
                                                ivVideoPreview.setPadding(5,5,5,5);
                                                ivVideoPreview.setImageBitmap(bmPhoto);
                                                ImageButton btnPlayVideo=(ImageButton)convertView.findViewById(R.id.btnVideoPlay);
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
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }else{
                                        TableRow trVideoRow=(TableRow) convertView.findViewById(R.id.tableRow2);
                                        ViewGroup.LayoutParams layoutParams=trVideoRow.getLayoutParams();
                                        layoutParams.height=0;
                                        trVideoRow.setLayoutParams(layoutParams);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                llComments.addView(convertView);


                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
//                    }
//                });
//                thread.start();

                //llComments.requestLayout();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private boolean mStartRecording=true;
    private String mFileName="";
    private MediaRecorder mRecorder;
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
            ViewGroup.LayoutParams layoutParams= commentView.findViewById(R.id.trAudioRow).getLayoutParams();
            layoutParams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            commentView.findViewById(R.id.trAudioRow).setLayoutParams(layoutParams);
        }
    }
    private void startRecording() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("recording audio", "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    private View commentView;
    private void initCommentDialog(){
        alertDialog=new AlertDialog.Builder(EventDetailsActivity.this);

        commentView=(LinearLayout) getLayoutInflater()
                .inflate(R.layout.comment_dialog, null);

        ViewGroup.LayoutParams phLayoutParams= commentView.findViewById(R.id.trImageRow).getLayoutParams();
        phLayoutParams.height=0;
        commentView.findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
        ViewGroup.LayoutParams auLayoutParams= commentView.findViewById(R.id.trAudioRow).getLayoutParams();
        auLayoutParams.height=0;
        commentView.findViewById(R.id.trAudioRow).setLayoutParams(auLayoutParams);
        final ProgressBar progressCommentAudio=(ProgressBar)commentView.findViewById(R.id.pbAudio);
        ImageButton btnAudioComment=(ImageButton) commentView.findViewById(R.id.btnAudio);
        btnAudioComment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                try{
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mFileName=null;
                        onRecord(true);
//                        mStartRecording = !mStartRecording;
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        onRecord(false);
//                        mStartRecording = !mStartRecording;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }
        });

        final ImageButton btnPlayAudioComment=(ImageButton)commentView.findViewById(R.id.btnPlayAudio);
        btnPlayAudioComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (lastPressedPlay!=null && !lastPressedPlay.equals(this)){
                        mStartPlaying=true;
                    }else if (lastPressedPlay==null) {
                        mStartPlaying=true;
                    } else if (lastPressedPlay.equals(this)){
                    } else{
                        mStartPlaying=false;
                    }
                    lastPressedPlay=this;
                    onPlay(mStartPlaying,mFileName,progressCommentAudio);
                    if (mStartPlaying) {
                        progressCommentAudio.setMax(mPlayer.getDuration());
                        //btnPlayAudioComment.setImageResource(R.drawable.icon_audio_stop);//todo stopImage
                    } else {
                        btnPlayAudioComment.setImageResource(R.drawable.icon_audio_play);
                        progressCommentAudio.setProgress(0);
                    }
                    mStartPlaying = !mStartPlaying;
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        ImageButton btnPhotoComment=(ImageButton)commentView.findViewById(R.id.btnPhoto);
        btnPhotoComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        ImageButton btnVideoComment=(ImageButton)commentView.findViewById(R.id.btnVideo);
        btnVideoComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });

        alertDialog.setView(commentView);
        alertDialog.setCancelable(true);

        alertDialog.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                try{
                    Long audioId=new Long(0);
                    if (mFileName!=null &&!mFileName.isEmpty()){
                        CreateEventParams cep=new CreateEventParams();
                        cep.setURL(mFileName);
                        audioId=new UploadFileOperation().execute(cep).get();
                    }

                    List<Long> photoIds=new ArrayList<Long>();
                    if (photoPathList.size()>0){
                        for (int k=0;k<photoPathList.size();k++){
                            CreateEventParams cep=new CreateEventParams();
                            cep.setURL(photoPathList.get(k));
                            Long phId=new UploadFileOperation().execute(cep).get();
                            photoIds.add(phId);
                        }
                    }
                    Long videoId=new Long(0);
                    if (mCurrentVideoPath!=null &&!mCurrentVideoPath.isEmpty()){
                        CreateEventParams cep=new CreateEventParams();
                        cep.setURL(mCurrentVideoPath);
                        videoId=new UploadFileOperation().execute(cep).get();
                    }
                    String registrationId = GCMRegistrationHelper.getRegistrationId(getApplicationContext());
                    Comment sendData=new Comment();
                    sendData.setEvent(currentEvent);
                    EditText input=(EditText)commentView.findViewById(R.id.etEventText);
                    sendData.setMessage(input.getText().toString());
                    sendData.setPhotoIds(photoIds);
                    sendData.setAudioId(audioId);
                    sendData.setVideoId(videoId);
                    new SaveCommentOperation().execute(sendData).get();
                    refreshFields();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Проблемы с соединением.\n Повторите попытку позже.", Toast.LENGTH_LONG);
                    toast.show();
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


    private Long uploadFile(String filePath){
        String boundary =  "*****";
        BufferedReader reader=null;

        try
        {
            String urlString="http://188.227.16.166:8080/utils/uploadFile";
            URL url = new URL(urlString);
            HttpURLConnection conn =(HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("User-Agent","Mozilla/5.0");
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setConnectTimeout(5000);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            File file=new File(filePath);
            byte[] data = new byte[(int) file.length()];
            try {
                new FileInputStream(file).read(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            wr.write(data);
            wr.flush();
            wr.close();
            // Get the server response
            InputStream is; //todo conn.getResponseCode() for errors
            try{

                is= conn.getInputStream();
            }
            catch (Exception e){
                e.printStackTrace();
                is=conn.getErrorStream();
            }

            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line);
            }

            Long result=new Long(sb.toString());
            return result;
        }
        catch(Exception ex)
        {
            Log.d(ex.getMessage(),ex.getMessage());
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                reader.close();
            }

            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) { }
            if (photoFile != null) {

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createVideoFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private List<String> photoPathList=new ArrayList<String>();
    private void setPic() {
        try {
            ViewGroup.LayoutParams phLayoutParams =commentView.findViewById(R.id.trImageRow).getLayoutParams();
            phLayoutParams.height = 150;
            commentView.findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
            commentView.findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
            LinearLayout llImages=(LinearLayout)commentView.findViewById(R.id.llImages);
            ImageView ivNew=new ImageView(getApplicationContext());
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(150,150);
            ivNew.setLayoutParams(layoutParams);
            //ivNew.setBackgroundColor(Color.parseColor("#D8D8DA"));
            ivNew.setPadding(5,5,5,5);
            llImages.addView(ivNew);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.replace("file:", ""), bmOptions);

            String filePath=mCurrentPhotoPath.replace("JPEG","qwer");
            Bitmap bmp= Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.6), (int)(bitmap.getHeight()*0.6), true);
            File file = new File(filePath);
            FileOutputStream fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

            Bitmap bmPhoto= Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            ivNew.setImageBitmap(bmPhoto);
            ivNew.setClickable(true);
            mCurrentPhotoPath=filePath;
            final String path=mCurrentPhotoPath;
            ivNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://"+path), "image/*");
                    startActivity(intent);
                }
            });
            photoPathList.add(mCurrentPhotoPath);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();//"file:" +
        return image;
    }
    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "video_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentVideoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try{
                setPic();
            }catch(Exception e){
                Log.e("photoActivityResult",e.getMessage());
            }
        }else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            try{
                setVideo();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String mCurrentVideoPath="";

    private void setVideo(){
        try{
            ViewGroup.LayoutParams phLayoutParams = commentView.findViewById(R.id.trImageRow).getLayoutParams();
            phLayoutParams.height = 150;
            commentView.findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
            commentView.findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 150, 150, true);
            ImageView ivVideoPreview=(ImageView) commentView.findViewById(R.id.ivForVideo);
            ivVideoPreview.setPadding(5,5,0,0);
            //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
            ivVideoPreview.setImageBitmap(bmPhoto);
            ImageButton btnPlayVideo=(ImageButton)commentView.findViewById(R.id.btnVideoPlay);
            btnPlayVideo.setImageResource(R.drawable.icon_audio_play);
            final String path=mCurrentVideoPath;
            btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://"+path), "video/*");
                    startActivity(intent);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void onPlay(boolean start,String audioFilePath,ProgressBar progressBar) {
        if (start) {
            startPlaying(audioFilePath,progressBar);
        } else {
            stopPlaying();
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        private ProgressBar progressBar;

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public void setProgressBar(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            try{
                while (!stop.get()) {
                    try{
                        progressBar.setProgress(mPlayer.getCurrentPosition());
                        Thread.sleep(200);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }catch (Exception e){
                Log.e("",e.getMessage());
            }

        }
    }
    private MediaObserver observer = null;
    private void startPlaying(final String audioFilePath,final ProgressBar progressBar) {
        if (mPlayer!=null){
            try{
                observer.stop();
                observer.getProgressBar().setProgress(0);
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(audioFilePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try{
                        mStartPlaying = true;
                        observer.stop();
                        progressBar.setProgress(mp.getCurrentPosition());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            observer = new MediaObserver();
            observer.setProgressBar(progressBar);
            mPlayer.prepare();
            mPlayer.start();

            new Thread(observer).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        try{
            if (observer!=null) observer.stop();
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void share(){
        if (AuthorizationType.VK == ApplicationSettings.getAuthorizationType()){

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


        } else if (AuthorizationType.FACEBOOK == ApplicationSettings.getAuthorizationType()){
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


        } else if (AuthorizationType.TWITTER == ApplicationSettings.getAuthorizationType()){
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
                                                    Twitter twitter = new TwitterFactory(builder.build()).getInstance(
                                                            ApplicationSettings.getTwitterAccessToken());
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
            share();
        }
    }
}
