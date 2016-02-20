package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.location.Address;
import android.location.Geocoder;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.*;
import com.globalgrupp.greenlight.greenlightclient.utils.GCMRegistrationHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
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

    ArrayList<String> photoPathList=new ArrayList<String>();


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        try{

            setContentView(R.layout.createevent);
//            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            if (getIntent().hasExtra("location")){
                eLocation=(SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
            }

            Geocoder gc=new Geocoder(this, Locale.getDefault());
            List<Address> addres= gc.getFromLocation(eLocation.getLatitude(),eLocation.getLongtitude(),1);//по идее хватит и одного адреса
            eAddres=addres.get(0);

            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setTitle("");
            TextView tvGl=(TextView)findViewById(R.id.tvGl);
            tvGl.setText("Новое событие");
            tvGl.setVisibility(View.VISIBLE);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//            getSupportActionBar().setIcon(R.drawable.ic_launcher);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), EventListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //onBackPressed();
                }
            });
            progress=(ProgressBar)findViewById(R.id.pbAudio);
            progress.getProgressDrawable().setColorFilter(Color.parseColor("#41B147"), PorterDuff.Mode.MULTIPLY);
            mFileName=null;
            mCurrentPhotoPath=null;
            mCurrentVideoPath=null;
            setEvents();
//            findViewById(R.id.trAudioRow).setVisibility(View.INVISIBLE);

            ViewGroup.LayoutParams phLayoutParams= findViewById(R.id.trImageRow).getLayoutParams();
            phLayoutParams.height=0;
            findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
            ViewGroup.LayoutParams auLayoutParams= findViewById(R.id.trAudioRow).getLayoutParams();
            auLayoutParams.height=0;
            findViewById(R.id.trAudioRow).setLayoutParams(auLayoutParams);

            TextView tvStreetName=(TextView)findViewById(R.id.streetName);
            tvStreetName.setText(eAddres.getThoroughfare());
            findViewById(R.id.ivDropDown).setVisibility(View.INVISIBLE);

        }
        catch (Exception e){
            e.printStackTrace();
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    boolean mStartRecording = true;
    boolean mStartPlaying = true;
    public void sendEvent(){
        try{
            Button btnSend=(Button)findViewById(R.id.btnCreateEvent);
            btnSend.setEnabled(false);
            String serverURL = "http://46.146.171.6:8080/event/createEvent";
            EditText et=(EditText) findViewById(R.id.etEventText);
            String street=eAddres.getThoroughfare();
            //CreateEventParams params=new CreateEventParams(serverURL,eLocation.getLongtitude(),eLocation.getLatitude(),et.getText().toString());
            Event params=new Event();
            params.setLongitude(eLocation.getLongtitude());
            params.setLatitude(eLocation.getLatitude());
            params.setMessage(et.getText().toString());

//            Long audioId=new Long(0);
//            if (mFileName!=null){
//                CreateEventParams cep=new CreateEventParams();
//                cep.setURL(mFileName);
//                audioId=new UploadFileOperation().execute(cep).get();
//            }
            if (mFileName!=null){
                params.setAudioId(new Long(-1));
            }
            params.setAudioPath(mFileName);

            List<Long> photoIds=new ArrayList<Long>();
            if (photoPathList.size()>0){
                for (int i=0;i<photoPathList.size();i++){
                    photoIds.add(new Long(-1));
                }
            }
            params.setPhotoIds(photoIds);
            params.setPhotoPathList(photoPathList);

//            Long videoId=new Long(0);
//            if (mCurrentVideoPath!=null){
//                CreateEventParams cep=new CreateEventParams();
//                cep.setURL(mCurrentVideoPath);
//                videoId=new UploadFileOperation().execute(cep).get();
//            }
            if (mCurrentVideoPath!=null){
                params.setVideoId(new Long(-1));
            }
            params.setVideoPath(mCurrentVideoPath);

            String registrationId =GCMRegistrationHelper.getRegistrationId(getApplicationContext());


//            params.setAudioId(audioId);
            params.setCreateDate(new Date());
//            params.setVideoId(videoId);
            params.setStreetName(street);
            params.setSenderAppId(registrationId);
//            params.setPhotoIds(photoIds);

            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.FACEBOOK){
                String userName = prefs.getString("FacebookUserName", "");
                params.setUserName(userName);
                params.setSocialType(new Long(AuthorizationType.FACEBOOK.getValue()));

            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.VK){
                String userName = prefs.getString("VKUserName", "");
                params.setUserName(userName);
                params.setSocialType(new Long(AuthorizationType.VK.getValue()));

            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.TWITTER){
                String userName = prefs.getString("TwitterUserName", "");
                params.setUserName(userName);
                params.setSocialType(new Long(AuthorizationType.TWITTER.getValue()));

            } else if (ApplicationSettings.getInstance().getAuthorizationType()==AuthorizationType.GREENLIGHT){
                String userName = prefs.getString("GreenLightToken", "");
                params.setUserName(userName);
                params.setSocialType(new Long(AuthorizationType.GREENLIGHT.getValue()));

            }
            params.setUniqueGUID(UUID.randomUUID().toString());
            String queueEventsString = prefs.getString("queueEvents","[]");
            Gson gson=new Gson();
            Type listType = new TypeToken<ArrayList<Event>>() {
            }.getType();
            ArrayList<Event> queueEvents=gson.fromJson(queueEventsString,listType);
            queueEvents.add(params);
            SharedPreferences.Editor editor=prefs.edit();
            editor.putString("queueEvents",gson.toJson(queueEvents));
            editor.commit();

            finish();
//            Boolean res= new CreateEventOperation().execute(params).get();
//            if (res){
//                finish();
//            } else{
////                setProgressBarIndeterminateVisibility(false);
//                Toast toast = Toast.makeText(getApplicationContext(),
//                        "Проблемы с соединением.\n Повторите попытку позже.", Toast.LENGTH_LONG);
//                toast.show();
//                btnSend.setEnabled(true);
//            }
        }catch (Exception e){
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Проблемы с соединением.\n Повторите попытку позже.", Toast.LENGTH_LONG);
            toast.show();
            Button btnSend=(Button)findViewById(R.id.btnCreateEvent);
            btnSend.setEnabled(true);
        }
    }

    public void setEvents(){
        Button createEventButton= (Button) findViewById(R.id.btnCreateEvent);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEvent();
            }
        });
        Button createEventButtonTop=(Button)findViewById(R.id.btnCreateEventTop);
        createEventButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEvent();
            }
        });
        btnAudio=(ImageButton)findViewById(R.id.btnAudio);
        btnAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onRecord(true);
                    mStartRecording = !mStartRecording;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    onRecord(false);
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
                        progress.setMax(mPlayer.getDuration());
                        //btnPlayAudio.setImageResource(R.drawable.icon_audio_stop);//todo stopImage
                    } else {
                        btnPlayAudio.setImageResource(R.drawable.icon_audio_play);
                        progress.setProgress(0);
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
            ViewGroup.LayoutParams layoutParams= findViewById(R.id.trAudioRow).getLayoutParams();
            layoutParams.height=ViewGroup.LayoutParams.WRAP_CONTENT;
            findViewById(R.id.trAudioRow).setLayoutParams(layoutParams);
            //findViewById(R.id.trAudioRow).setVisibility(View.VISIBLE);
        }
    }




    public void findStartEndStreets(){
        SimpleGeoCoords previousCooords= ApplicationSettings.getInstance().getPreviousCoord();
        if (previousCooords.equals(eLocation)){
            //nothing типа давно не двигался, нет направления

        } else {
            //todo есть направление

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
                    try{
                        progress.setProgress(mPlayer.getCurrentPosition());
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

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
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
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        try{
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
        }catch (Exception e){
            e.printStackTrace();
        }

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
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 5491520L);

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
        ViewGroup.LayoutParams phLayoutParams;
        LinearLayout llImages=(LinearLayout)findViewById(R.id.llImages);;
        try{
            phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
            phLayoutParams.height = 150;
            findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
            findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        Bitmap bmPhoto;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize=4;
            File photoFile=new File(mCurrentPhotoPath);
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
            String filePath=mCurrentPhotoPath.replace("JPEG","qwer");
            Bitmap bmp= Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.6), (int)(bitmap.getHeight()*0.6), true);
            File file = new File(filePath);
            FileOutputStream fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            bmPhoto= Bitmap.createScaledBitmap(bitmap, 150, 150, true);
            mCurrentPhotoPath=filePath;
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        try{
            ImageView ivNew=new ImageView(getApplicationContext());
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(150,150);
            ivNew.setLayoutParams(layoutParams);
            //ivNew.setBackgroundColor(Color.parseColor("#D8D8DA"));
            ivNew.setPadding(5,5,5,5);
            llImages.addView(ivNew);
            ivNew.setImageBitmap(bmPhoto);
            ivNew.setClickable(true);

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
        }
           catch (Exception e){
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
                e.printStackTrace();
            }
        }else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            try{
                setVideo();
            }catch ( Exception e){
                e.printStackTrace();
            }
        } else if (resultCode==RESULT_CANCELED ){
            try{
                setVideo();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void setVideo(){
        try{
            ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
            phLayoutParams.height = 150;
            findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
            findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 150, 150, true);
            ImageView ivVideoPreview=(ImageView) findViewById(R.id.ivForVideo);
            ivVideoPreview.setPadding(5,5,0,0);
            //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
            ivVideoPreview.setImageBitmap(bmPhoto);
            ImageButton btnPlayVideo=(ImageButton)findViewById(R.id.btnVideoPlay);
            btnPlayVideo.setImageResource(R.drawable.video_wh);
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
}
