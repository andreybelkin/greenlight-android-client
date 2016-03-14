package com.globalgrupp.greenlight.androidclient.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import com.facebook.login.LoginManager;
import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.vk.sdk.VKSdk;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 25.02.2016.
 */
public class GroupListActivity extends ActionBarActivity implements MenuItem.OnMenuItemClickListener{

    GroupsAdapter groupsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);


        Toolbar mActionBarToolbar;
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Список групп");
        ImageButton ibIconUp = (ImageButton) findViewById(R.id.ibIconUp);
        ibIconUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ((ListView)findViewById(R.id.listViewGroups)).setSelectionAfterHeaderView();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        refreshGroupList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGroupList();
    }

    void refreshGroupList(){
        new AsyncTask<Void, Void, List<Group>>() {
            @Override
            protected List<Group> doInBackground(Void... voids) {

                /************ Make Post Call To Web Server ***********/
                BufferedReader reader=null;
                Log.i("doInBackground service ","doInBackground service ");
                List<Group> result=new ArrayList<Group>();
                try
                {
                    String urlString="http://188.227.16.166:8080/group/getAllGroups";
                    JSONObject msg=new JSONObject();
                    URL url = new URL(urlString);
                    HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setConnectTimeout(5000);
//                    conn.setReadTimeout(20000);

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

                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Group>>() {
                    }.getType();
                    result=gson.fromJson(sb.toString(),listType);
                }
                catch(Exception ex)
                {
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
                return result;
            }

            @Override
            protected void onPostExecute(List<Group> groups) {
                super.onPostExecute(groups);
                groupsAdapter=new GroupsAdapter(getApplicationContext(),groups);
                ListView lvGroups=(ListView)findViewById(R.id.listViewGroups);
                lvGroups.setAdapter(groupsAdapter);
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        //super.onBackPressed();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        try {
            Intent startIntent;
            if (menuItem.getItemId() == R.id.action_new_event) {
                startIntent=new Intent(this,GroupEditActivity.class);
                startActivity(startIntent);
            } else if (menuItem.getItemId() == R.id.action_logout) {
                final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                        EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("FacebookToken","");
                editor.putString("VKToken","");
                editor.putString("TwitterToken","");
                editor.putString("GreenLightToken","");
                editor.commit();
                if (ApplicationSettings.getAuthorizationType() == AuthorizationType.FACEBOOK){
                    LoginManager.getInstance().logOut();
                } else if (ApplicationSettings.getAuthorizationType() == AuthorizationType.VK){
                    VKSdk.logout();
                } else if (ApplicationSettings.getAuthorizationType() == AuthorizationType.TWITTER){

                }
                ApplicationSettings.setAuthorizationType(AuthorizationType.NONE);
                startIntent = new Intent(this, AuthorizationActivity.class);
                startActivity(startIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
        editMenuItem.setOnMenuItemClickListener(this);

        MenuItem eventListMenuItem = menu.findItem(R.id.action_event_list);
        eventListMenuItem.setVisible(false);

        MenuItem mapItem = menu.findItem(R.id.action_map);
        mapItem.setVisible(false);

        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        logoutItem.setOnMenuItemClickListener(this);

        MenuItem settingItem = menu.findItem(R.id.action_settings);
        settingItem.setVisible(false);


        MenuItem groupsItem=menu.findItem(R.id.action_groups);
        groupsItem.setOnMenuItemClickListener(this);
        return true;
    }

}
