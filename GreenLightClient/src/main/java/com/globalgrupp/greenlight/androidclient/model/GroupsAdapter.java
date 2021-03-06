package com.globalgrupp.greenlight.androidclient.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.globalgrupp.greenlight.androidclient.R;

import java.util.List;

/**
 * Created by Lenovo on 24.02.2016.
 */
public class GroupsAdapter extends ArrayAdapter<Group> {

    LayoutInflater inflater;

    private static class ViewHolder {

        TextView tvGroupName;

    }

    public GroupsAdapter(Context context, List<Group> groupsItems){
        super(context, R.layout.lv_drawer_item,groupsItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Group groupsItem = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (inflater==null){
                inflater = LayoutInflater.from(getContext());
            }
            convertView = inflater.inflate(R.layout.lv_group_item, parent, false);
            viewHolder.tvGroupName=(TextView)convertView.findViewById(R.id.tvGroupName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvGroupName.setText(groupsItem.getName());

        convertView.setTag(viewHolder);
        return convertView;


    }
}
