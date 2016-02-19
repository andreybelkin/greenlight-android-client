package com.globalgrupp.greenlight.greenlightclient.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.globalgrupp.greenlight.greenlightclient.R;
import com.globalgrupp.greenlight.greenlightclient.controller.EventDetailsActivity;
import com.globalgrupp.greenlight.greenlightclient.controller.MainActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
        TableRow trAudioRow;
        ProgressBar audioProgress;
        TableRow trPhotoRow;
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
            viewHolder.trAudioRow=(TableRow)convertView.findViewById(R.id.trAudioRow);
            viewHolder.audioProgress=(ProgressBar)convertView.findViewById(R.id.pbAudio);
            viewHolder.trPhotoRow=(TableRow) convertView.findViewById(R.id.trImageRow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.eventId=commentsItem.getId();
        viewHolder.tvTitle.setText(commentsItem.getMessage());
        viewHolder.tvDate.setText(df.format(commentsItem.getCreateDate()));
        viewHolder.tvStreetName.setText(commentsItem.getStreetName());
        if (commentsItem.getUserName()!=null && !commentsItem.getUserName().isEmpty()){
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
            viewHolder.trAudioRow.setVisibility(View.GONE);
//            ViewGroup.LayoutParams qwe = convertView.findViewById(R.id.ivHasAudio).getLayoutParams();
//            qwe.width = 0;
//            convertView.findViewById(R.id.ivHasAudio).setLayoutParams(qwe);
        } else {
            viewHolder.trAudioRow.setVisibility(View.VISIBLE);
            viewHolder.ivAudio.setVisibility(View.VISIBLE);
            ImageButton ibPlayAudio= (ImageButton)viewHolder.trAudioRow.findViewById(R.id.btnPlayAudio);
            if (!commentsItem.getAudioId().equals(new Long(-1))){
                new FileDownloadTask().execute(commentsItem.getAudioId().toString(),commentsItem.getUniqueGUID(),"3gp");
            }
            final ProgressBar audioProgres=viewHolder.audioProgress;
            audioProgres.getProgressDrawable().setColorFilter(Color.parseColor("#41B147"), PorterDuff.Mode.MULTIPLY);
            ibPlayAudio.setOnClickListener(new View.OnClickListener() {
                class MediaObserver implements Runnable {
                    private AtomicBoolean stop = new AtomicBoolean(false);

                    public void stop() {
                        stop.set(true);
                    }

                    @Override
                    public void run() {
                        try{
                            while (!stop.get()) {
                                try{
                                    audioProgres.setProgress(mPlayer.getCurrentPosition());
                                    Thread.sleep(200);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }catch (Exception e){
                            Log.e("",e.getMessage());
                        }
                    }
                }
                MediaPlayer mPlayer;
                MediaObserver observer;
                @Override
                public void onClick(View v) {
                    try{
                        mPlayer = new MediaPlayer();
                        String audioPath=commentsItem.getAudioPath();
                        if (commentsItem.getAudioPath()==null||commentsItem.getAudioPath().isEmpty()){
                            audioPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/gl/"+commentsItem.getUniqueGUID()+"_"+commentsItem.getAudioId().toString()+".3gp";
                        }
                        mPlayer.setDataSource(audioPath);
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                observer.stop();
                                mPlayer=null;

                            }
                        });
                        mPlayer.prepare();
                        observer=new MediaObserver();
                        audioProgres.setMax(mPlayer.getDuration());
                        mPlayer.start();
                        new Thread(observer).start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });


        }
        viewHolder.trPhotoRow.setVisibility(View.GONE);
        if (commentsItem.getPhotoIds() == null || commentsItem.getPhotoIds().size() == 0) {
            viewHolder.ivPhoto.setVisibility(View.GONE);
//            ViewGroup.LayoutParams qwe = convertView.findViewById(R.id.ivHasPhoto).getLayoutParams();
//            qwe.width = 0;
//            convertView.findViewById(R.id.ivHasPhoto).setLayoutParams(qwe);
        } else {
            viewHolder.ivPhoto.setVisibility(View.VISIBLE);
            viewHolder.trPhotoRow.setVisibility(View.VISIBLE);
            LinearLayout llImages=(LinearLayout) viewHolder.trPhotoRow.findViewById(R.id.llImages);
            for (int z=1;z<llImages.getChildCount();z++){
                llImages.removeViewAt(z);
            }

            for (int i=0;i<commentsItem.getPhotoIds().size();i++ ){
                try{
                    String mCurrentPhotoPath="";
                    if (commentsItem.getPhotoIds().get(i).equals(new Long(-1))){
                        mCurrentPhotoPath=commentsItem.getPhotoPathList().get(i);
                    }else{
                        mCurrentPhotoPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/gl/"+commentsItem.getUniqueGUID()+"_"+commentsItem.getPhotoIds().get(i).toString()+".jpg";
                        new FileDownloadTask().execute(commentsItem.getPhotoIds().get(i).toString(),commentsItem.getUniqueGUID().toString(),"jpg");
                    }
                    File photoFile=new File(mCurrentPhotoPath);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inSampleSize=4;
                    Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
                    Bitmap bmPhoto= Bitmap.createScaledBitmap(bitmap, 100, 100, true);

                    ImageView ivNew=new ImageView(getContext());
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(90,90);
                    ivNew.setLayoutParams(layoutParams);
                    //ivNew.setBackgroundColor(Color.parseColor("#D8D8DA"));
                    ivNew.setPadding(5,5,5,5);
                    llImages.addView(ivNew);
                    ivNew.setImageBitmap( getRoundedCornerBitmap(bmPhoto));
                    ivNew.setClickable(true);

                    final String path=mCurrentPhotoPath;
                    ivNew.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://"+path), "image/*");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        if (commentsItem.getVideoId() == null || commentsItem.getVideoId().equals(new Long(0))) {
            viewHolder.ivVideo.setVisibility(View.GONE);
//            ViewGroup.LayoutParams qwe = convertView.findViewById(R.id.ivHasVideo).getLayoutParams();
//            qwe.width = 0;
//            convertView.findViewById(R.id.ivHasVideo).setLayoutParams(qwe);
            viewHolder.trPhotoRow.findViewById(R.id.rlVideo).setVisibility(View.GONE);
        } else {
            try{

                viewHolder.ivVideo.setVisibility(View.VISIBLE);
                viewHolder.trPhotoRow.setVisibility(View.VISIBLE);
                viewHolder.trPhotoRow.findViewById(R.id.rlVideo).setVisibility(View.VISIBLE);
                String mCurrentVideoPath="";
                if (commentsItem.getVideoId().equals(new Long(-1))){
                    mCurrentVideoPath=commentsItem.getVideoPath();
                }else{
                    mCurrentVideoPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/gl/"+commentsItem.getUniqueGUID()+"_"+commentsItem.getVideoId().toString()+".jpg";
                    new FileDownloadTask().execute(commentsItem.getVideoId().toString(),commentsItem.getUniqueGUID().toString(),"jpg");
                }


                ViewGroup.LayoutParams layoutParams=viewHolder.trPhotoRow.findViewById(R.id.ivForVideo).getLayoutParams();
                layoutParams.height=90;
                layoutParams.width=90;
                viewHolder.trPhotoRow.findViewById(R.id.ivForVideo).setLayoutParams(layoutParams);


                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath,
                        MediaStore.Images.Thumbnails.MINI_KIND);
                Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 90, 90, true);
                ImageView ivVideoPreview=(ImageView) viewHolder.trPhotoRow.findViewById(R.id.ivForVideo);
                ivVideoPreview.setPadding(5,5,5,5);
                //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
                ivVideoPreview.setImageBitmap(getRoundedCornerBitmap(bmPhoto));
                ImageButton btnPlayVideo=(ImageButton)viewHolder.trPhotoRow.findViewById(R.id.btnVideoPlay);
                btnPlayVideo.setImageResource(R.drawable.icon_audio_play);
                final String path=mCurrentVideoPath;
                btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://"+path), "video/*");
                        getContext().startActivity(intent);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }



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

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
