package com.globalgrupp.greenlight.greenlightclient;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.globalgrupp.greenlight.greenlightclient.controller.EventDetailsActivity;
import com.globalgrupp.greenlight.greenlightclient.controller.EventListActivity;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Created by Lenovo on 14.01.2016.
 */
public class Application extends android.app.Application {

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(@Nullable VKAccessToken oldToken, @Nullable VKAccessToken newToken) {
            if (newToken == null) {
                Toast.makeText(Application.this, "AccessToken invalidated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Application.this, EventListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //vkAccessTokenTracker.startTracking();
        VKSdk.initialize(Application.this);
        //VKSdk.login(Application.this, "friends");
    }
}
