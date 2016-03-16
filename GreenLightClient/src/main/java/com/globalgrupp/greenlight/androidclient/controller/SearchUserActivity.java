package com.globalgrupp.greenlight.androidclient.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.model.*;
import com.globalgrupp.greenlight.androidclient.util.ApplicationSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
public class SearchUserActivity extends ActionBarActivity implements MenuItem.OnMenuItemClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        Toolbar mActionBarToolbar;
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("Пользователи");
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

        ImageButton ibSearch=(ImageButton)findViewById(R.id.ibSearch);
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshUsersList();
            }
        });
    }

    void refreshUsersList(){
        UsersFilter usersFilter=new UsersFilter();
        usersFilter.setName(((EditText)findViewById(R.id.etUserName)).getText().toString());
        usersFilter.setFbUser(((CheckBox)findViewById(R.id.cbFb)).isChecked());
        usersFilter.setVkUser(((CheckBox)findViewById(R.id.cbVK)).isChecked());
        usersFilter.setTwUser(((CheckBox)findViewById(R.id.cbTwitter)).isChecked());
        new AsyncTask<UsersFilter, Void, List<SocialNetworkUser>>() {
            protected List<SocialNetworkUser> doInBackground(UsersFilter... filters) {

                /************ Make Post Call To Web Server ***********/
                BufferedReader reader=null;
                Log.i("doInBackground service ","doInBackground service ");
                List<SocialNetworkUser> result=new ArrayList<SocialNetworkUser>();
                try
                {
                    String urlString= ApplicationSettings.getServerURL() + "/group/getUsers";


                    URL url = new URL(urlString);
                    HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("User-Agent","Mozilla/5.0");
                    conn.setRequestProperty("Accept","*/*");
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(20000);

                    Gson gson=new Gson();

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    String str = gson.toJson(filters[0]);
                    byte[] data=str.getBytes("UTF-8");
                    wr.write(data);
                    wr.flush();
                    wr.close();

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

                    Type listType = new TypeToken<ArrayList<SocialNetworkUser>>() {
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
            protected void onPostExecute(List<SocialNetworkUser> groups) {
                super.onPostExecute(groups);
                GroupUsersAdapter groupsAdapter=new GroupUsersAdapter(getApplicationContext(),groups);
                ListView lvGroups=(ListView)findViewById(R.id.lvUsers);
                lvGroups.setAdapter(groupsAdapter);
            }
        }.execute(usersFilter);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }
}
