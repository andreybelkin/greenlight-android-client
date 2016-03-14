package com.globalgrupp.greenlight.androidclient.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.model.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

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
public class GroupEditActivity extends ActionBarActivity implements MenuItem.OnMenuItemClickListener, View.OnClickListener {

    GroupUsersAdapter groupUsersAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);


        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        groupUsersAdapter=new GroupUsersAdapter(this,new ArrayList<SocialNetworkUser>());
        ((ListView)findViewById(R.id.lvGroupUser)).setAdapter(groupUsersAdapter);

        if (getIntent().hasExtra("groupId")){
            final Long groupId=(Long)getIntent().getExtras().getSerializable("groupId");
            new AsyncTask<Long, Void, List<Group>>() {
                @Override
                protected List<Group> doInBackground(Long... longs) {

                        /************ Make Post Call To Web Server ***********/
                        BufferedReader reader=null;
                        Log.i("doInBackground service ","doInBackground service ");
                        List<Group> result=new ArrayList<Group>();
                        try
                        {
                            String urlString="http://188.227.16.166:8080/group/getAllGroups/"+longs[0];
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

                        Group group=groups.get(0);
                        EditText etGroupName=(EditText)findViewById(R.id.etGroupName);
                        etGroupName.setText(group.getName());
                        if (group.getGroupType()==1){
                            ((RadioButton)findViewById(R.id.rbPublic)).setChecked(true);
                        } else if (group.getGroupType()==2){
                            ((RadioButton)findViewById(R.id.rbPrivate)).setChecked(true);
                        }

                        groupUsersAdapter=new GroupUsersAdapter(getApplicationContext(),group.getSocialNetworkUserSet());
                        ((ListView)findViewById(R.id.lvGroupUser)).setAdapter(groupUsersAdapter);

                    }
            }.execute(groupId);

        }

        Button btnAddUser=(Button)findViewById(R.id.btnAddUser);
        btnAddUser.setOnClickListener(this);

        Button btnSaveGroup=(Button)findViewById(R.id.btnSaveGroup);
        btnSaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etGroupName=(EditText)findViewById(R.id.etGroupName);
                Group newGroup=new Group();
                newGroup.setName(etGroupName.getText().toString());
                RadioGroup rgGroupType=(RadioGroup)findViewById(R.id.rgGroupType);
                int checkedId=rgGroupType.getCheckedRadioButtonId();

                if (checkedId==R.id.rbPublic){
                        newGroup.setGroupType(new Long(1));
                } else if (checkedId==R.id.rbPrivate){
                    newGroup.setGroupType(new Long(2));
                }
                //todo заполнение списка пользователей

                new AsyncTask<Group, Void, Void>() {
                    @Override
                    protected Void doInBackground(Group... groups) {
                        BufferedReader reader=null;
                        Log.i("doInBackground service ","doInBackground service ");
                        // Send data
                        List<Event> result=new ArrayList<Event>();
                        try
                        {
                            String urlString="http://188.227.16.166:8080/group/editGroup";

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
                            String str = gson.toJson(groups[0]);

                            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

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

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        finish();
                    }
                }.execute(newGroup);
            }
        });


    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem editMenuItem = menu.findItem(R.id.action_new_event);
        editMenuItem.setVisible(false);

        MenuItem eventListMenuItem = menu.findItem(R.id.action_event_list);
        eventListMenuItem.setVisible(false);

        MenuItem mapItem = menu.findItem(R.id.action_map);
        mapItem.setVisible(false);

        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        logoutItem.setVisible(false);

        MenuItem settingItem = menu.findItem(R.id.action_settings);
        settingItem.setVisible(false);


        MenuItem groupsItem=menu.findItem(R.id.action_groups);
        groupsItem.setVisible(false);

        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.btnAddUser){
            Intent searchUserIntent=new Intent(getApplicationContext(),SearchUserActivity.class);
            startActivity(searchUserIntent);
        }
    }
}
