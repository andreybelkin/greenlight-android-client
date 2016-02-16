package com.globalgrupp.greenlight.greenlightclient.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.classes.ApplicationSettings;
import com.globalgrupp.greenlight.greenlightclient.classes.AuthorizationType;
import com.globalgrupp.greenlight.greenlightclient.classes.TopExceptionHandler;
import com.globalgrupp.greenlight.greenlightclient.classes.UserCredentials;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONException;
import org.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by Lenovo on 14.01.2016.
 */
public class AuthorizationActivity extends ActionBarActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

    private boolean webViewExpanded=false;

    @Override
    public void onBackPressed() {
        if (webViewExpanded){
            finish();
            startActivity(getIntent());
        }else{
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        try{
            setContentView( R.layout.authorize_list);

            FacebookSdk.sdkInitialize(getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    keytoken=loginResult.getAccessToken().getToken();
                    final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                            EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("FacebookToken",loginResult.getAccessToken().getToken());
                    editor.commit();
                    try{
                        GraphRequest request=GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                try {
                                    String userName=jsonObject.getString("name");
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("FacebookUserName",userName);
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                        ApplicationSettings.getInstance().setAuthorizationType(authorizationType);
                        Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                        startActivity(intent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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


            ImageButton vkButton=(ImageButton) findViewById(R.id.btnVKLogin);
            vkButton.setOnClickListener(this);
            ImageButton btnFBLogin=(ImageButton) findViewById(R.id.btnFBLogin);
            btnFBLogin.setOnClickListener(this);
            ImageButton twitterButton=(ImageButton) findViewById(R.id.btnTwitterLogin);
            twitterButton.setOnClickListener(this);
            ImageButton btnNotAuthorized=(ImageButton) findViewById(R.id.btnNotAuthorized);
            btnNotAuthorized.setOnClickListener(this);
            findViewById(R.id.ivDropDown).setVisibility(View.INVISIBLE);

            ImageButton btnAuthorizeGl=(ImageButton) findViewById(R.id.btnAuthorizeGl);
            btnAuthorizeGl.setOnClickListener(this);

            Button btnGLreg=(Button)findViewById(R.id.btnGLreg);
            btnGLreg.setOnClickListener(this);


            TextView tvGl=(TextView)findViewById(R.id.tvGl);
            tvGl.setVisibility(View.VISIBLE);
            if (ApplicationSettings.getInstance().getmGoogleApiClient()==null){
                ApplicationSettings.getInstance().setmGoogleApiClient( new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build());
                ApplicationSettings.getInstance().getmGoogleApiClient().connect();
                ApplicationSettings.getInstance().startLocationTimer();
            }

            final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
            String registrationId = prefs.getString("FacebookToken", "");
            if (!registrationId.isEmpty()){
                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.FACEBOOK);
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            }
            registrationId = prefs.getString("VKToken", "");
            if (!registrationId.isEmpty()){
                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.VK);
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            }
            registrationId = prefs.getString("TwitterToken", "");
            if (!registrationId.isEmpty()){
                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.TWITTER);
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            }
            registrationId=prefs.getString("GreenLightToken","");
            if (!registrationId.isEmpty()){
                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.GREENLIGHT);
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        try{
            if (view.getId()==R.id.btnVKLogin){
                authorizationType=AuthorizationType.VK;
                VKSdk.login(this, "wall");
            } else if (view.getId()==R.id.btnGLreg||view.getId()==R.id.btnRegist){
                Intent intent=new Intent(getApplicationContext(),RegistrationActivity.class);
                startActivity(intent);
            }
            else if (view.getId()==R.id.btnFBLogin){
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

//                LinearLayout ll=(LinearLayout)findViewById(R.id.layout_buttons);
//                ll_buttons_height=ll.getHeight();
//                ll.setLayoutParams(new LinearLayout.LayoutParams(ll.getWidth(),0));

                ScrollView scrollView=(ScrollView)findViewById(R.id.scrollView3);
                ViewGroup.LayoutParams lpScroll=scrollView.getLayoutParams();
                lpScroll.height=0;
                scrollView.setLayoutParams(lpScroll);
                webViewExpanded=true;
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
                authorizationType=AuthorizationType.NONE;
                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.NONE);
                Intent intent= new Intent(getApplicationContext(), EventListActivity.class);
                startActivity(intent);
            } else if (view.getId()==R.id.btnAuthorizeGl){
                final AlertDialog.Builder alertDialog=new AlertDialog.Builder(AuthorizationActivity.this);
                final LinearLayout commentView=(LinearLayout) getLayoutInflater()
                        .inflate(R.layout.login_pass_dialog, null);
                alertDialog.setView(commentView);
                alertDialog.setCancelable(true);

                Button btnRegistr=(Button)commentView.findViewById(R.id.btnRegist);
                btnRegistr.setOnClickListener(this);


                Button btnLogin=(Button)commentView.findViewById(R.id.btnLogin);
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText etLogin=(EditText)commentView.findViewById(R.id.etLogin);
                        EditText etPassword=(EditText)commentView.findViewById(R.id.etPass);
                        CheckBox cbNewUser=(CheckBox)commentView.findViewById(R.id.cbNewUser);

                        UserCredentials userCredentials=new UserCredentials();
                        userCredentials.setLogin(etLogin.getText().toString());
                        userCredentials.setPassword(etPassword.getText().toString());
                        userCredentials.setNewUser(cbNewUser.isChecked());
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

                                        URL url = new URL("http://192.168.1.38:8080/utils/authorize");

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
                                final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                                        EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                //todo token
                                editor.putString("GreenLightToken",userCredentials.getLogin());
                                editor.commit();
                                ApplicationSettings.getInstance().setAuthorizationType(AuthorizationType.GREENLIGHT);
                                Intent intent=new Intent(getApplicationContext(),EventListActivity.class);
                                startActivity(intent);
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

                alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                                if (i == KeyEvent.KEYCODE_BACK) {
                                    dialogInterface.cancel();
                                }
                                return false;
                            }
                        });
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();

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
                String userName=
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        try{
                            return twitter.getAccountSettings().getScreenName();
                        }catch (Exception e){
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.execute().get();
                ApplicationSettings.getInstance().setTwitterAccessToken(mAccessToken);
                final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                        EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("TwitterToken",mAccessToken.getToken());
                editor.putString("TwitterUserName",userName);
                editor.commit();
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
                        final SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("VKToken",res.accessToken);
                        editor.commit();
                        VKRequest request = VKApi.users().get();
                        request.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);
                                try {
                                    String firstName= response.json.getJSONArray("response").getJSONObject(0).getString("first_name");
                                    String lastName=response.json.getJSONArray("response").getJSONObject(0).getString("last_name");
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("VKUserName",firstName+" "+lastName);
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
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
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();

//        twitter.setOAuthAccessToken(null);
//        twitter.shutdown();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    ApplicationSettings.getInstance().getmGoogleApiClient());
            int i=0;
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        try{
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    ApplicationSettings.getInstance().getmGoogleApiClient());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        int i=0;
    }
}
