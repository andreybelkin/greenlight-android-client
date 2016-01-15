package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.CreateEventOperation;
import com.globalgrupp.greenlight.greenlightclient.classes.CreateEventParams;
import com.globalgrupp.greenlight.greenlightclient.classes.SimpleGeoCoords;
import com.globalgrupp.greenlight.greenlightclient.classes.UploadFileOperation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by п on 28.12.2015.
 */
public class NewEventActivity extends ActionBarActivity implements AdapterView.OnItemClickListener{

    private Toolbar mActionBarToolbar;

    private SimpleGeoCoords eLocation;
    private Address eAddres;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private static String mFileName = null;
    private static final String LOG_TAG = "AudioRecordTest";

    ImageButton btnAudio;
    ImageButton btnPlayAudio;
    ProgressBar progress;
    ImageButton btnPhoto;
    ImageButton btnVideo;
    String mCurrentVideoPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.createevent);
            if (getIntent().hasExtra("location")){
                eLocation=(SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
            }

            Geocoder gc=new Geocoder(this, Locale.getDefault());
            List<Address> addres= gc.getFromLocation(eLocation.getLatitude(),eLocation.getLongtitude(),1);//по идее хватит и одного адреса
            eAddres=addres.get(0);

            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            setSupportActionBar(mActionBarToolbar);
            //getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_launcher);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            mFileName=null;
            mCurrentPhotoPath=null;
            mCurrentVideoPath=null;
            setEvents();
            findViewById(R.id.trAudioRow).setVisibility(View.INVISIBLE);
            TableRow trVideoRow=(TableRow)findViewById(R.id.trVideoRow);
            trVideoRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,0));

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

    boolean mStartRecording = true;
    boolean mStartPlaying = true;
    public void setEvents(){
        final Button createEventButton= (Button) findViewById(R.id.btnCreateEvent);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Long audioId=new Long(0);
                    if (mFileName!=null){
                        CreateEventParams cep=new CreateEventParams();
                        cep.setURL(mFileName);
                        audioId=new UploadFileOperation().execute(cep).get();
                    }
                    Long photoId=new Long(0);
                    if (mCurrentPhotoPath!=null){
                        CreateEventParams cep=new CreateEventParams();
                        cep.setURL(mCurrentPhotoPath);
                        photoId=new UploadFileOperation().execute(cep).get();
                    }
                    Long videoId=new Long(0);
                    if (mCurrentVideoPath!=null){
                        CreateEventParams cep=new CreateEventParams();
                        cep.setURL(mCurrentVideoPath);
                        videoId=new UploadFileOperation().execute(cep).get();
                    }

                    String serverURL = "http://192.168.100.14:8080/event/createEvent";//todo config
                    EditText et=(EditText) findViewById(R.id.etEventText);

                    String street=eAddres.getThoroughfare();
                    CreateEventParams params=new CreateEventParams(serverURL,eLocation.getLongtitude(),eLocation.getLatitude(),et.getText().toString());
                    params.setAudioId(audioId);
                    params.setPhotoId(photoId);
                    params.setVideoId(videoId);
                    params.setStreetName(street);
                    Boolean res= new CreateEventOperation().execute(params).get();
                    if (res){
                        finish();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        btnAudio=(ImageButton)findViewById(R.id.btnAudio);
        btnAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onRecord(mStartRecording);
                    mStartRecording = !mStartRecording;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    onRecord(mStartRecording);
                    mStartRecording = !mStartRecording;
                }

                return false;
            }
        });
        btnPlayAudio=(ImageButton)findViewById(R.id.btnPlayAudio);
        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    onPlay(mStartPlaying);
                    if (mStartPlaying) {
                        btnPlayAudio.setImageResource(R.drawable.icon_audio_play);//todo stopImage
                    } else {
                        btnPlayAudio.setImageResource(R.drawable.icon_audio_play);

                    }
                    mStartPlaying = !mStartPlaying;
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

        btnPhoto=(ImageButton)findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        btnVideo=(ImageButton)findViewById(R.id.btnVideo);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
            findViewById(R.id.trAudioRow).setVisibility(View.VISIBLE);
        }
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
            mPlayer.setDataSource(mFileName);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                   // observer.stop();
                    //progress.setProgress(mp.getCurrentPosition());
                    //btnPlayAudio.performClick();
                }
            });
            //observer = new MediaObserver();
            mPlayer.prepare();
            mPlayer.start();

            //new Thread(observer).start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.stop();

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
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //mCurrentPhotoPath=Uri.fromFile(photoFile).toString();
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
    private void setPic() {
        // Get the dimensions of the View
        ImageView mImageView=(ImageView)findViewById(R.id.ivPhoto);
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath.replace("file:",""), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.replace("file:",""), bmOptions);
        mImageView.setImageBitmap(bitmap);
        findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
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

    private void setVideo(){
        TableRow trVideoRow=(TableRow)findViewById(R.id.trVideoRow);
        trVideoRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,trVideoRow.getWidth()));
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        final VideoView video = (VideoView) findViewById(R.id.videoView);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(thumbnail);
        //video.setBackgroundDrawable(bitmapDrawable);
        video.setVideoPath(mCurrentVideoPath);
        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                video.start();
                return false;
            }
        });
        //video.start();
    }
}
