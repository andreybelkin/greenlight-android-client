package com.globalgrupp.greenlight.greenlightclient.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.internal.ShareFeedContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.ApplicationSettings;
import com.globalgrupp.greenlight.greenlightclient.classes.AuthorizationType;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Places;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.facebook.messenger.*;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by Lenovo on 14.01.2016.
 */
public class AuthorizationActivity extends ActionBarActivity implements View.OnClickListener {

    Toolbar mActionBarToolbar;

    private LoginButton loginBtn;


    private String keytoken;

    private AuthorizationType authorizationType;

    private Twitter twitter;
    RequestToken requestToken;

    private String TWITTER_CONSUMER_KEY="fWJW731tJv7Yk2ID2vBmIYLFR";
    private String TWITTER_CONSUMER_SECRET="VsUjFxLpzSwWycOscn4Tti9BRyGaIvWJxTEQGI48SmDRHmuDFz";
    private static final String CALLBACK_URL = "oauth://t4jsample";

    private int ll_buttons_height;


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
                    ApplicationSettings.getInstance().setAuthorizationType(authorizationType);
                    Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCancel() { }

                @Override
                public void onError(FacebookException e) {
                    e.printStackTrace();
                }
            });
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_launcher);

            Button vkButton=(Button) findViewById(R.id.btnVKLogin);
            vkButton.setOnClickListener(this);
            Button btnFBLogin=(Button) findViewById(R.id.btnFBLogin);
            btnFBLogin.setOnClickListener(this);
            Button twitterButton=(Button) findViewById(R.id.btnTwitterLogin);
            twitterButton.setOnClickListener(this);
            Button btnNotAuthorized=(Button) findViewById(R.id.btnNotAuthorized);
            btnNotAuthorized.setOnClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        try{
            if (view.getId()==R.id.btnVKLogin){
                authorizationType=AuthorizationType.VK;
                VKSdk.login(this, "wall");
            } else if (view.getId()==R.id.btnFBLogin){
                authorizationType=AuthorizationType.FACEBOOK;
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
            } else if (view.getId()==R.id.btnTwitterLogin){
                authorizationType=AuthorizationType.TWITTER;
                twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
                requestToken=
                        new AsyncTask<Void, Void, RequestToken>() {
                    @Override
                    protected RequestToken doInBackground(Void... voids) {
                        try {
                            return twitter.getOAuthRequestToken(CALLBACK_URL);
                        } catch (TwitterException e) {
                            e.printStackTrace();
                            return null;
                        }

                    }
                }.execute().get();

                LinearLayout ll=(LinearLayout)findViewById(R.id.layout_buttons);
                ll_buttons_height=ll.getHeight();
                ll.setLayoutParams(new LinearLayout.LayoutParams(ll.getWidth(),0));
                WebView mWebView = (WebView)findViewById(R.id.webView);
                mWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public boolean shouldOverrideUrlLoading (WebView view, String url) {
                        if (url.startsWith(CALLBACK_URL)) {
                            Uri uri = Uri.parse(url);
                            completeVerify(uri);
                            return true;
                        }
                        return false;
                    }
                });
                mWebView.loadUrl(requestToken.getAuthenticationURL());
                mWebView.getSettings().setAppCacheEnabled(false);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.clearCache(true);
                mWebView.clearFormData();
                getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
                final Activity activity = this;
                mWebView.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView w, int p) {
                        activity.setProgress(p * 100);
                    }
                });
            } else if (view.getId()==R.id.btnNotAuthorized){
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void completeVerify(Uri uri) {
        if (uri != null) {
            String verifier = uri.getQueryParameter("oauth_verifier");
            try {
                AccessToken mAccessToken =
                new AsyncTask<String, Void, AccessToken>() {
                    @Override
                    protected AccessToken doInBackground(String... params) {
                        try{
                            return twitter.getOAuthAccessToken(requestToken, params[0]);
                        }catch (Exception e){
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.execute(verifier).get();
                twitter.setOAuthAccessToken(mAccessToken);
                ApplicationSettings.getInstance().setTwitterAccessToken(mAccessToken);
                // Add code here to save the OAuth AccessToken and AccessTokenSecret into  SharedPreferences
            } catch (Exception e) {
                e.printStackTrace();
            }
            onActivityResult(0, Activity.RESULT_OK, getIntent());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            if (authorizationType==AuthorizationType.VK){
                if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                    @Override
                    public void onResult(VKAccessToken res) {
                        // Пользователь успешно авторизовался
                        keytoken=res.accessToken;
                        ApplicationSettings.getInstance().setAuthorizationType(authorizationType);
                        Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                        startActivity(intent);
                    }
                    @Override
                    public void onError(VKError error) {
                        // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                        //todo alert?
                    }
                }))   {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
            if (authorizationType==AuthorizationType.FACEBOOK){
                super.onActivityResult(requestCode, resultCode, data);
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
            if (authorizationType==AuthorizationType.TWITTER){
                super.onActivityResult(requestCode, resultCode, data);

                ApplicationSettings.getInstance().setAuthorizationType(authorizationType);
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
                WebView webView=(WebView)findViewById(R.id.webView);
                ViewGroup.LayoutParams lp = webView.getLayoutParams();
                lp.width=100;
                lp.height=0;
                webView.setLayoutParams(lp);
//                LinearLayout ll=(LinearLayout)findViewById(R.id.layout_buttons);
//                ll.setLayoutParams(new LinearLayout.LayoutParams(ll.getWidth(),ll_buttons_height));
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
