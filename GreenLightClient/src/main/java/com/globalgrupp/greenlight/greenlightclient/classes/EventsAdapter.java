package com.globalgrupp.greenlight.greenlightclient.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.globalgrupp.greenlight.greenlightclient.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by п on 31.12.2015.
 */
public class EventsAdapter  extends ArrayAdapter<Event> {
    DateFormat df = new SimpleDateFormat("HH:mm");
    LayoutInflater inflater;
    private static class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvStreetName;
    }

    public  EventsAdapter(Context context, ArrayList<Event> eventItems){
        super(context, R.layout.lv_drawer_item,eventItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event commentsItem = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (inflater==null) inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.lv_events_item, parent, false);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvEventsTitle);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvEventsDate);
            viewHolder.tvStreetName=(TextView) convertView.findViewById(R.id.tvEventsStreet);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(commentsItem.getMessage());
        viewHolder.tvDate.setText(df.format(commentsItem.getCreateDate()));
        viewHolder.tvStreetName.setText(commentsItem.getStreetName());
        return convertView;
    }
}
