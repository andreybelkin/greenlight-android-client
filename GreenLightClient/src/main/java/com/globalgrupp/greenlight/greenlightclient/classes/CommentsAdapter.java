package com.globalgrupp.greenlight.greenlightclient.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.globalgrupp.greenlight.greenlightclient.R;

import java.util.ArrayList;

/**
 * Created by п on 29.12.2015.
 */
public class CommentsAdapter extends ArrayAdapter<Comment> {
    private static class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
    }

    public  CommentsAdapter(Context context, ArrayList<Comment> commentItems){
        super(context, R.layout.lv_drawer_item,commentItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment commentsItem = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.lv_comments_item, parent, false);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvNewsTitle);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvNewsDate);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvTitle.setText(commentsItem.getMessage());
        //viewHolder.tvDate.setText(commentsItem.getCreateDate().toString());

        return convertView;
    }
}
