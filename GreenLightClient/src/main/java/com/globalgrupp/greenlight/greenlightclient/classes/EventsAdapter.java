package com.globalgrupp.greenlight.greenlightclient.classes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.controller.EventDetailsActivity;
import com.globalgrupp.greenlight.greenlightclient.controller.MainActivity;

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
        ImageView ivAudio;
        ImageView ivPhoto;
        ImageView ivVideo;
        TextView tvAuthor;
        ImageView ivSocNet;
        Long eventId;
    }

    public  EventsAdapter(Context context, ArrayList<Event> commentsItems){
        super(context, R.layout.lv_drawer_item,commentsItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Event commentsItem=getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            if (inflater==null) inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.lv_events_item, parent, false);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvEventsTitle);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvEventsDate);
            viewHolder.tvStreetName=(TextView) convertView.findViewById(R.id.tvEventsStreet);
            viewHolder.tvAuthor=(TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.ivSocNet=(ImageView) convertView.findViewById(R.id.ivCreateIcon);
            viewHolder.ivAudio=(ImageView) convertView.findViewById(R.id.ivHasAudio);
            viewHolder.ivPhoto=(ImageView) convertView.findViewById(R.id.ivHasPhoto);
            viewHolder.ivVideo=(ImageView) convertView.findViewById(R.id.ivHasVideo);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.eventId=commentsItem.getId();
//        ((TextView) convertView.findViewById(R.id.tvEventsTitle)).setText(commentsItem.getMessage());
//        ((TextView) convertView.findViewById(R.id.tvEventsDate)).setText(df.format(commentsItem.getCreateDate()));
//        ((TextView) convertView.findViewById(R.id.tvEventsStreet)).setText(commentsItem.getStreetName());
        viewHolder.tvTitle.setText(commentsItem.getMessage());
        viewHolder.tvDate.setText(df.format(commentsItem.getCreateDate()));
        viewHolder.tvStreetName.setText(commentsItem.getStreetName());
        if (commentsItem.getUserName()!=null && !commentsItem.getUserName().isEmpty()){
//            ((TextView)convertView.findViewById(R.id.tvUserName)).setText(commentsItem.getUserName());
            viewHolder.tvAuthor.setText(commentsItem.getUserName());
        } else{
            viewHolder.tvAuthor.setText("");
        }

        if (commentsItem.getSocialType()!=null){
            if (commentsItem.getSocialType().equals(new Long(1))){
                viewHolder.ivSocNet.setImageResource(R.drawable.icon_vk_event);
            } else if (commentsItem.getSocialType().equals(new Long(2))){
                viewHolder.ivSocNet.setImageResource(R.drawable.icon_facebook_event);
            } else if (commentsItem.getSocialType().equals(new Long(3))){
                viewHolder.ivSocNet.setImageResource(R.drawable.icon_twitter_event);
            } else if (commentsItem.getSocialType().equals(new Long(4))){
                viewHolder.ivSocNet.setImageResource(R.drawable.icon_greenlight_event);
            }
        }

        if (commentsItem.getAudioId() == null || commentsItem.getAudioId().equals(new Long(0))) {
            viewHolder.ivAudio.setVisibility(View.GONE);
//            ViewGroup.LayoutParams qwe = convertView.findViewById(R.id.ivHasAudio).getLayoutParams();
//            qwe.width = 0;
//            convertView.findViewById(R.id.ivHasAudio).setLayoutParams(qwe);
        } else {
            viewHolder.ivAudio.setVisibility(View.VISIBLE);
        }
        if (commentsItem.getPhotoIds() == null || commentsItem.getPhotoIds().size() == 0) {
            viewHolder.ivPhoto.setVisibility(View.GONE);
//            ViewGroup.LayoutParams qwe = convertView.findViewById(R.id.ivHasPhoto).getLayoutParams();
//            qwe.width = 0;
//            convertView.findViewById(R.id.ivHasPhoto).setLayoutParams(qwe);
        } else {
            viewHolder.ivPhoto.setVisibility(View.VISIBLE);
        }
        if (commentsItem.getVideoId() == null || commentsItem.getVideoId().equals(new Long(0))) {
            viewHolder.ivVideo.setVisibility(View.GONE);
//            ViewGroup.LayoutParams qwe = convertView.findViewById(R.id.ivHasVideo).getLayoutParams();
//            qwe.width = 0;
//            convertView.findViewById(R.id.ivHasVideo).setLayoutParams(qwe);
        } else {
            viewHolder.ivVideo.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder holder=(ViewHolder)view.getTag();
                Intent startIntent = new Intent(getContext(), EventDetailsActivity.class);
                startIntent.putExtra("eventId", holder.eventId);
                startIntent.putExtra("eventObject",commentsItem);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(startIntent);

            }
        });


        ImageButton ibComment = (ImageButton) convertView.findViewById(R.id.ibComment);
        ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getContext(), EventDetailsActivity.class);
                startIntent.putExtra("eventId", commentsItem.getId());
                startIntent.putExtra("openComment", true);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(startIntent);
            }
        });

        ImageButton ibShare = (ImageButton) convertView.findViewById(R.id.ibShare);
        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getContext(), EventDetailsActivity.class);
                startIntent.putExtra("eventId", commentsItem.getId());
                startIntent.putExtra("openShare", true);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(startIntent);
            }
        });

        ImageButton ibMap = (ImageButton) convertView.findViewById(R.id.ibMap);
        ibMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(getContext(), MainActivity.class);
                SimpleGeoCoords geoCoords = new SimpleGeoCoords(commentsItem.getLongitude(), commentsItem.getLatitude(), commentsItem.getAltitude());
                startIntent.putExtra("eventCoords", geoCoords);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(startIntent);
            }
        });

        if (ApplicationSettings.getInstance().getAuthorizationType() == AuthorizationType.NONE) {
            ibShare.setImageResource(R.mipmap.icon_share_grey);
            ibShare.setEnabled(false);
            //ibShare.setVisibility(View.INVISIBLE);
        }
        convertView.setTag(viewHolder);
        return convertView;
    }
}
