package com.globalgrupp.greenlight.greenlightclient.controller;

import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.CreateEventOperation;
import com.globalgrupp.greenlight.greenlightclient.classes.CreateEventParams;
import com.globalgrupp.greenlight.greenlightclient.classes.SimpleGeoCoords;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

    Button btnAudio;
    Button btnPlayAudio;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.createevent);
            //getIntent().hasExtra
            //getIntent().getSerializableExtra
            if (getIntent().hasExtra("location")){
                eLocation=(SimpleGeoCoords) getIntent().getExtras().getSerializable("location");
            }

            Geocoder gc=new Geocoder(this, Locale.getDefault());
            List<Address> addres= gc.getFromLocation(eLocation.getLatitude(),eLocation.getLongtitude(),1);//по идее хватит и одного адреса
            eAddres=addres.get(0);

            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            setSupportActionBar(mActionBarToolbar);
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/audiorecordtest.3gp";
            setEvents();
            btnPlayAudio.setVisibility(View.INVISIBLE);
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
                    String serverURL = "http://192.168.1.38:8080/event/createEvent";//todo config
                    // Use AsyncTask execute Method To Prevent ANR Problem
                    EditText et=(EditText) findViewById(R.id.etEventText);
                    CreateEventParams params=new CreateEventParams(serverURL,eLocation.getLongtitude(),eLocation.getLatitude(),et.getText().toString());
                    new CreateEventOperation().execute(params);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        btnAudio=(Button)findViewById(R.id.btnAudio);
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    onRecord(mStartRecording);
                    if (mStartRecording) {
                        btnAudio.setText("Стоп");
                    } else {

                        btnAudio.setText("Аудио");
                    }
                    mStartRecording = !mStartRecording;
                }catch (Exception e){
                    Log.e("Audio error",e.getMessage());
                }
            }
        });
        btnPlayAudio=(Button)findViewById(R.id.btnPlayAudio);
        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    btnPlayAudio.setText("Стоп");
                } else {
                    btnPlayAudio.setText("Воспроизвести");
                }
                mStartPlaying = !mStartPlaying;
            }
        });

    }
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
            btnPlayAudio.setVisibility(View.VISIBLE);
        }
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
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
    private void startRecording() {
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
}
