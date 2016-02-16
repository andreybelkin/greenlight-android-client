package com.globalgrupp.greenlight.greenlightclient.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by п on 29.12.2015.
 */
public class CommentsAdapter extends ArrayAdapter<Comment> {
    DateFormat df = new SimpleDateFormat("HH:mm");
    LayoutInflater inflater ;
    MediaPlayer mPlayer;
    boolean mStartPlaying = true;

    private class ViewHolder {
        TextView tvTitle;
        TextView tvDate;
//        LinearLayout listPhoto;

    }

    public  CommentsAdapter(Context context, ArrayList<Comment> commentItems){
        super(context, R.layout.lv_comments_item,commentItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

            final Comment commentsItem = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                if (inflater==null){
                    inflater = LayoutInflater.from(getContext());
                }
                convertView = inflater.inflate(R.layout.lv_comments_item, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvCommentMessage);
                viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvCommentDate);
//                viewHolder.listPhoto=(LinearLayout) convertView.findViewById(R.id.llImages);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvTitle.setText(commentsItem.getMessage());
            viewHolder.tvDate.setText(df.format(commentsItem.getCreateDate()));

        try{
            if (commentsItem.getAudioId()!=null &&!commentsItem.getAudioId().equals(new Long(0))){
//            TableRow trAudioRow=(TableRow)convertView.findViewById(R.id.tableRow);
//            trAudioRow.getLayoutParams()
                final ProgressBar progressBar=(ProgressBar)convertView.findViewById(R.id.pbAudio);
                final ImageButton btnPlayAudioComment=(ImageButton)convertView.findViewById(R.id.btnPlayAudio);
                final String audioFilePath= new FileDownloadTask().execute("http://192.168.1.38:8080/utils/getFile/"+commentsItem.getAudioId().toString(),"3gp").get();
                btnPlayAudioComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            onPlay(mStartPlaying,audioFilePath,progressBar);
                            if (mStartPlaying) {
                                progressBar.setMax(mPlayer.getDuration());
                                btnPlayAudioComment.setImageResource(R.drawable.icon_audio_play);//todo stopImage
                            } else {
                                btnPlayAudioComment.setImageResource(R.drawable.icon_audio_play);
                            }
                            mStartPlaying = !mStartPlaying;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }else{
                TableRow trAudioRow=(TableRow)convertView.findViewById(R.id.tableRow);
                ViewGroup.LayoutParams layoutParams= trAudioRow.getLayoutParams();
                layoutParams.height=0;
                trAudioRow.setLayoutParams(layoutParams);
            }
            if (commentsItem.getPhotoIds()!=null && commentsItem.getPhotoIds().size()>0){

                List<Long> photoIds=commentsItem.getPhotoIds();
                for (int i=0;i<photoIds.size();i++){
                    try{
                        final String photoFilePath=new FileDownloadTask().execute("http://192.168.1.38:8080/utils/getFile/"+photoIds.get(i),"jpg").get();

//                    ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
//                    phLayoutParams.height =150;
//                    findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
//                    findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
                        LinearLayout llImages=(LinearLayout)convertView.findViewById(R.id.llImages);
                        //viewHolder.listPhoto=
                        ImageView ivNew=new ImageView(getContext());
                        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(150,150);
                        ivNew.setLayoutParams(layoutParams);
                        //ivNew.setBackgroundColor(Color.parseColor("#D8D8DA"));
                        ivNew.setPadding(5,5,5,5);
                        llImages.addView(ivNew);
//                        viewHolder.listPhoto.addView(ivNew);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath.replace("file:", ""), bmOptions);
                        Bitmap bmPhoto= Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                        ivNew.setImageBitmap(bmPhoto);
                        ivNew.setClickable(true);
                        final String path=photoFilePath;
//                        viewHolder.listPhoto=llImages;
                        ivNew.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://"+path), "image/*");
                                getContext().startActivity(intent);
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }else{
                TableRow trVideoRow=(TableRow) convertView.findViewById(R.id.tableRow2);
                ViewGroup.LayoutParams layoutParams=trVideoRow.getLayoutParams();
                layoutParams.height=0;
                trVideoRow.setLayoutParams(layoutParams);
            }
            if (commentsItem.getVideoId()!=null && !commentsItem.getVideoId().equals(new Long(0))){
                try{
                    final String videoFilePath=new FileDownloadTask().execute("http://192.168.1.38:8080/utils/getFile/"+commentsItem.getVideoId().toString(),"3gp").get();

//            ViewGroup.LayoutParams phLayoutParams = findViewById(R.id.trImageRow).getLayoutParams();
//            phLayoutParams.height = 150;
//            findViewById(R.id.trImageRow).setLayoutParams(phLayoutParams);
//            findViewById(R.id.trImageRow).setVisibility(View.VISIBLE);
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    if (thumbnail!=null){
                        Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 150, 150, true);
                        ImageView ivVideoPreview=(ImageView) convertView.findViewById(R.id.ivForVideo);
                        ivVideoPreview.setPadding(5,5,5,5);
                        //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
                        ivVideoPreview.setImageBitmap(bmPhoto);

                        ImageButton btnPlayVideo=(ImageButton)convertView.findViewById(R.id.btnVideoPlay);
                        btnPlayVideo.setImageResource(R.drawable.icon_play_white);
                        final String path=videoFilePath;
                        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://"+path), "video/*");
                                getContext().startActivity(intent);
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }else{
                TableRow trVideoRow=(TableRow) convertView.findViewById(R.id.tableRow2);
                ViewGroup.LayoutParams layoutParams=trVideoRow.getLayoutParams();
                layoutParams.height=0;
                trVideoRow.setLayoutParams(layoutParams);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        convertView.requestLayout();

        return convertView;
    }

    private void onPlay(boolean start,String audioFilePath,ProgressBar progressBar) {
        if (start) {
            startPlaying(audioFilePath,progressBar);
        } else {
            stopPlaying();
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        private ProgressBar progressBar;

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public void setProgressBar(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            try{
                while (!stop.get()) {
                    progressBar.setProgress(mPlayer.getCurrentPosition());
                    Thread.sleep(200);
                }
            }catch (Exception e){
                Log.e("",e.getMessage());
            }

        }
    }
    private MediaObserver observer = null;
    private void startPlaying(String audioFilePath,final ProgressBar progressBar) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(audioFilePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try{
                        observer.stop();
                        progressBar.setProgress(mp.getCurrentPosition());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            observer = new MediaObserver();
            observer.setProgressBar(progressBar);
            mPlayer.prepare();
            mPlayer.start();

            new Thread(observer).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
}
