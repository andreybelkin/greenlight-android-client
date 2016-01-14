package com.globalgrupp.greenlight.greenlightclient.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.util.Arrays;

/**
 * Created by Lenovo on 14.01.2016.
 */
public class AuthorizationActivity extends Activity implements View.OnClickListener {

    Toolbar mActionBarToolbar;

    private LoginButton loginBtn;


    private String keytoken;



    CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try{
            setContentView( R.layout.authorize_list);
            FacebookSdk.sdkInitialize(getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    keytoken=loginResult.getAccessToken().getToken();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException e) {
                    e.printStackTrace();
                }
            });
//            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
//            setSupportActionBar(mActionBarToolbar);
//            getSupportActionBar().setTitle("");
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//            getSupportActionBar().setIcon(R.drawable.ic_launcher);

            Button vkButton=(Button) findViewById(R.id.btnVKLogin);
            vkButton.setOnClickListener(this);
            Button btnFBLogin=(Button) findViewById(R.id.btnFBLogin);
            btnFBLogin.setOnClickListener(this);

//            loginBtn = (LoginButton) findViewById(R.id.fb_login_button);
//            loginBtn.setReadPermissions(Arrays.asList("email"));
//            CallbackManager callbackManager= CallbackManager.Factory.create();
//            loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                @Override
//                public void onSuccess(LoginResult loginResult) {
//                    keytoken=loginResult.getAccessToken().getToken();
//                    //loginResult.getAccessToken().getUserId();
//                }
//
//                @Override
//                public void onCancel() {
//
//                }
//
//                @Override
//                public void onError(FacebookException e) {
//
//                }
//            });


            Button twitterButton=(Button) findViewById(R.id.btnTwitterLogin);
            twitterButton.setOnClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    @Override
    public void onClick(View view) {
        try{
            if (view.getId()==R.id.btnVKLogin){
                VKSdk.login(this, "wall");
            } else if (view.getId()==R.id.btnFBLogin){
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
            } else if (view.getId()==R.id.btnTwitterLogin){

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                @Override
                public void onResult(VKAccessToken res) {
                    // Пользователь успешно авторизовался

                }
                @Override
                public void onError(VKError error) {
                    // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                    //todo alert?
                }
            }))
            {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
