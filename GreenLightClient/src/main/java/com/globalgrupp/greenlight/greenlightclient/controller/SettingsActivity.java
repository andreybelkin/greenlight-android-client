package com.globalgrupp.greenlight.greenlightclient.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.TopExceptionHandler;
import org.w3c.dom.Text;

/**
 * Created by Lenovo on 25.01.2016.
 */
public class SettingsActivity extends ActionBarActivity {

    Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        try{
            setContentView( R.layout.settings_layout);

            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            mActionBarToolbar.setNavigationIcon(R.drawable.icon_toolbal_arrow_white);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setTitle("");
            TextView tvGl=(TextView)findViewById(R.id.tvGl);
            tvGl.setText("Настройки");
            tvGl.setVisibility(View.VISIBLE);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//            getSupportActionBar().setIcon(R.drawable.ic_launcher);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            Long radius = prefs.getLong("event_radius", 5);
//            EditText etRadius=(EditText)findViewById(R.id.etRadius);
//            etRadius.setText(radius.toString());
            findViewById(R.id.ivDropDown).setVisibility(View.INVISIBLE);
            TextView tv=(TextView)findViewById(R.id.tvProgressText);
            tv.setText(radius.toString());
            SeekBar seekBar=(SeekBar)findViewById(R.id.sbRadius);
            seekBar.incrementProgressBy(1);
            seekBar.setMax(14);
            seekBar.setProgress(radius.intValue()-1);

            //seekBar.setProgressDrawable(new ColorDrawable(Color.parseColor("#3FA43A")));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    try{
                        TextView tvProgressText=  (TextView)findViewById(R.id.tvProgressText);
                        tvProgressText.setText(String.valueOf(seekBar.getProgress()+1));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try{
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            SeekBar seekBar=(SeekBar)findViewById(R.id.sbRadius);
            editor.putLong("event_radius",new Long(seekBar.getProgress()+1));
            editor.commit();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
