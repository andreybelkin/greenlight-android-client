package com.globalgrupp.greenlight.greenlightclient.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.UserCredentials;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by п on 11.02.2016.
 */
public class RegistrationActivity extends ActionBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        Button btnRegister=(Button)findViewById(R.id.btnSend);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String etPwd=((EditText)findViewById(R.id.etPwd)).getText().toString();
                String etConfirmPwd=((EditText)findViewById(R.id.etConfirmPwd)).getText().toString();
                String etLogin=((EditText)findViewById(R.id.etLogin)).getText().toString();
                if (etPwd.equals(etConfirmPwd)){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пароль не ссовпадает с потдверждением", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                UserCredentials userCredentials=new UserCredentials();
                userCredentials.setLogin(etLogin);
                userCredentials.setPassword(etPwd);
                userCredentials.setNewUser(true);
                try{
                    Boolean result=new AsyncTask<UserCredentials, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(UserCredentials... params) {

                            BufferedReader reader=null;
                            Log.i("doInBackground service ","doInBackground service ");
                            // Send data
                            try
                            {
                                // Defined URL  where to send data
                                JSONObject msg=new JSONObject();
                                msg.put("login",params[0].getLogin());
                                msg.put("password",params[0].getPassword());
                                msg.put("newUser",params[0].isNewUser());

                                URL url = new URL("http://192.168.1.33:8080/utils/authorize");

                                // Send POST data request

                                HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("User-Agent","Mozilla/5.0");
                                conn.setRequestProperty("Accept","*/*");
                                conn.setRequestProperty("Content-Type","application/json");
                                conn.setRequestProperty("charset", "utf-8");
                                conn.setConnectTimeout(20000);
                                conn.setReadTimeout(20000);
                                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                                String str = msg.toString();
                                byte[] data=str.getBytes("UTF-8");
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

                                while((line = reader.readLine()) != null)
                                {
                                    sb.append(line + "\n");
                                }

                                final String content = sb.toString();
                                if (content==null  || content.isEmpty()){
                                    return true;
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(),
                                                    content, Toast.LENGTH_LONG);
                                            toast.show();
                                        }
                                    });

                                    return false;
                                }
                            }
                            catch(Exception ex)
                            {
                                Log.d(ex.getMessage(),ex.getMessage());
                                ex.printStackTrace();
                                return false;
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
                        }
                    }.execute(userCredentials).get();
                    if (result){
                        Intent intent=new Intent(getApplicationContext(),AuthorizationActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }
}
