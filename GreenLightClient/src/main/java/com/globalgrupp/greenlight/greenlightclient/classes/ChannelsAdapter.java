package com.globalgrupp.greenlight.greenlightclient.classes;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.globalgrupp.greenlight.greenlightclient.R;

import java.util.ArrayList;

/**
 * Created by Lenovo on 24.02.2016.
 */
public class ChannelsAdapter extends ArrayAdapter<Channel> {

    public ChannelsAdapter (Context context, ArrayList<Channel> channelsItems){
        super(context, R.layout.lv_drawer_item,channelsItems);
    }
}
