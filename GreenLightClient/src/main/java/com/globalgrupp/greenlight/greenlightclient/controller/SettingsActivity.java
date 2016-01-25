package com.globalgrupp.greenlight.greenlightclient.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import com.globalgrupp.greenlight.greenlightclient.R;

/**
 * Created by Lenovo on 25.01.2016.
 */
public class SettingsActivity extends ActionBarActivity {

    Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView( R.layout.settings_layout);

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
            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            Long radius = prefs.getLong("event_radius", 10);
            EditText etRadius=(EditText)findViewById(R.id.etRadius);
            etRadius.setText(radius.toString());
            findViewById(R.id.ivDropDown).setVisibility(View.INVISIBLE);
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
            EditText etRadius=(EditText)findViewById(R.id.etRadius);
            Long radius=new Long(etRadius.getText().toString());
            editor.putLong("event_radius",radius);
            editor.commit();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
