package com.globalgrupp.greenlight.androidclient.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.controller.EventDetailsActivity;
import com.globalgrupp.greenlight.androidclient.controller.EventListActivity;
import com.globalgrupp.greenlight.androidclient.controller.MainActivity;
import com.globalgrupp.greenlight.androidclient.util.ApplicationSettings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.android.gms.internal.zzir.runOnUiThread;

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
        ImageButton ivDelete;
    }

    public  EventsAdapter(Context context, ArrayList<Event> commentsItems){
        super(context, R.layout.lv_drawer_item,commentsItems);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Event commentsItem=getItem(position);

        final ViewHolder viewHolder;
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
            viewHolder.ivDelete=(ImageButton) convertView.findViewById(R.id.ibDelete);
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

        final SharedPreferences prefs = getContext().getSharedPreferences(
                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString("regId", "");
//        if (true){//тут должна быть проверка, что может удалять
        if (!TextUtils.isEmpty(registrationId) && registrationId.equals(commentsItem.getUser().getPushAppId())){
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
                Long eventId=commentsItem.getId();
                @Override
                public void onClick(View view) {
                    new AsyncTask<Long, Void, Void>() {
                        @Override
                        protected Void doInBackground(Long... params) {
                            BufferedReader reader=null;
                            Log.i("doInBackground service ","doInBackground service ");
                            // Send data
                            List<Event> result=new ArrayList<Event>();
                            try
                            {
                                String urlString=ApplicationSettings.getServerURL() + "/event/delete/"+params[0].toString();
                                URL url = new URL(urlString);

                                HttpURLConnection conn =(HttpURLConnection) url.openConnection();
                                conn.setDoOutput(true);
                                conn.setDoInput(true);
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("User-Agent","Mozilla/5.0");
                                conn.setRequestProperty("Accept","*/*");
                                conn.setRequestProperty("Content-Type","application/json");
                                conn.setRequestProperty("charset", "utf-8");
                                conn.setConnectTimeout(5000);
                                conn.setReadTimeout(10000);

                                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                                String str = "";
                                byte[] data=str.getBytes("UTF-8");
                                wr.write(data);
                                wr.flush();
                                wr.close();
                                InputStream is; //todo conn.getResponseCode() for errors
                                try{
                                    is= conn.getInputStream();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            EventsAdapter.this.remove(getItem(position));
                                            EventsAdapter.this.notifyDataSetChanged();
                                        }
                                    });
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                    is=conn.getErrorStream();
                                }
                                reader = new BufferedReader(new InputStreamReader(is));
                                StringBuilder sb = new StringBuilder();
                                String line = null;

                                // Read Server Response
                                while((line = reader.readLine()) != null)
                                {
                                    // Append server response in string
                                    sb.append(line + "\n");
                                }
                            }
                            catch(Exception ex)
                            {
                                Log.d(ex.getMessage(),ex.getMessage());
                                ex.printStackTrace();
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
                            return null;
                        }
                    }.execute(eventId);
                }
            });
        } else{
            viewHolder.ivDelete.setVisibility(View.GONE);
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
        } else {
            viewHolder.ivPhoto.setVisibility(View.VISIBLE);
            viewHolder.trPhotoRow.setVisibility(View.VISIBLE);
            final LinearLayout llPhotos=(LinearLayout) viewHolder.trPhotoRow.findViewById(R.id.llPhotos);
            llPhotos.removeAllViews();

            for (int i=0;i<commentsItem.getPhotoIds().size();i++ ){
                try{
                    String mCurrentPhotoPath="";
                    if (commentsItem.getPhotoIds().get(i).equals(new Long(-1))){
                        mCurrentPhotoPath=commentsItem.getPhotoPathList().get(i);
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
                        llPhotos.addView(ivNew);
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
                    }else {
                        mCurrentPhotoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/gl/" + commentsItem.getUniqueGUID() + "_" + commentsItem.getPhotoIds().get(i).toString() + ".jpg";
                        final Long id=commentsItem.getPhotoIds().get(i);
                       // new FileDownloadTask().execute(commentsItem.getPhotoIds().get(i).toString(), commentsItem.getUniqueGUID().toString(), "jpg");
                        new AsyncTask<String, Void, String>() {
                            @Override
                            protected String doInBackground(String... params) {
                                File file = null;
                                try {
                                    String DownloadUrl = ApplicationSettings.getServerURL() + "/utils/getFile/" + params[0];
                                    String fileName = params[1] + "_" + params[0] + "." + params[2];
                                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                                    File dir = new File(root + "/gl");
                                    if (dir.exists() == false) {
                                        dir.mkdirs();
                                    }
                                    URL url = new URL(DownloadUrl);
                                    file = new File(dir, fileName);
                                    if (file.exists()) {
                                        return file.toString();//файл уже есть, можно не качать.
                                    }
                                    Log.d("DownloadManager", "download begining");
                                    Log.d("DownloadManager", "download url:" + url);
                                    Log.d("DownloadManager", "downloaded file name:" + fileName);

                                    URLConnection ucon = url.openConnection();
                                    ucon.setConnectTimeout(5000);
                                    ucon.setReadTimeout(20000);
                                    InputStream is = ucon.getInputStream();
                                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                    int nRead;
                                    byte[] data = new byte[16384];

                                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                                        buffer.write(data, 0, nRead);
                                    }
                                    buffer.flush();

                                    byte[] dataFile = buffer.toByteArray();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(dataFile);
                                    fos.flush();
                                    fos.close();
                                } catch (Exception e) {
                                    Log.d("DownloadManager", "Error: " + e);
                                }
                                return file.toString();
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                super.onPostExecute(s);
                                File photoFile=new File(s);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                bmOptions.inSampleSize=4;
                                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
                                Bitmap bmPhoto= Bitmap.createScaledBitmap(bitmap, 100, 100, true);

                                ImageView ivNew=new ImageView(getContext());
                                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(90,90);
                                ivNew.setLayoutParams(layoutParams);
                                //ivNew.setBackgroundColor(Color.parseColor("#D8D8DA"));
                                ivNew.setPadding(5,5,5,5);
                                ivNew.setId(id.intValue());
                                if (llPhotos.findViewById(id.intValue())==null){
                                    llPhotos.addView(ivNew);
                                }

                                ivNew.setImageBitmap( getRoundedCornerBitmap(bmPhoto));
                                ivNew.setClickable(true);

                                final String path=s;
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
                            }
                        }.execute(commentsItem.getPhotoIds().get(i).toString(), commentsItem.getUniqueGUID().toString(), "jpg");
                    }

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
                ViewGroup.LayoutParams layoutParams=viewHolder.trPhotoRow.findViewById(R.id.ivForVideo).getLayoutParams();
                layoutParams.height=90;
                layoutParams.width=90;
                viewHolder.trPhotoRow.findViewById(R.id.ivForVideo).setLayoutParams(layoutParams);
                if (commentsItem.getVideoId().equals(new Long(-1))){
                    mCurrentVideoPath=commentsItem.getVideoPath();



                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(mCurrentVideoPath,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 90, 90, true);
                    ImageView ivVideoPreview=(ImageView) viewHolder.trPhotoRow.findViewById(R.id.ivForVideo);
                    ivVideoPreview.setPadding(5,5,5,5);
                    //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
                    ivVideoPreview.setImageBitmap(getRoundedCornerBitmap(bmPhoto));
                    ImageButton btnPlayVideo=(ImageButton)viewHolder.trPhotoRow.findViewById(R.id.btnVideoPlay);
                    btnPlayVideo.setImageResource(R.drawable.video_wh);
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
                }else{
                    mCurrentVideoPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/gl/"+commentsItem.getUniqueGUID()+"_"+commentsItem.getVideoId().toString()+".3gp";
                    //new FileDownloadTask().execute(commentsItem.getVideoId().toString(),commentsItem.getUniqueGUID().toString(),"3gp");
                    final ImageView ivVideoPreview=(ImageView) viewHolder.trPhotoRow.findViewById(R.id.ivForVideo);
                    final ImageButton btnPlayVideo=(ImageButton)viewHolder.trPhotoRow.findViewById(R.id.btnVideoPlay);
                    new AsyncTask<String, Void, String>() {
                        @Override
                        protected String doInBackground(String... params) {
                            File file = null;
                            try {
                                String DownloadUrl = ApplicationSettings.getServerURL() + "/utils/getFile/" + params[0];
                                String fileName = params[1] + "_" + params[0] + "." + params[2];
                                String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                                File dir = new File(root + "/gl");
                                if (dir.exists() == false) {
                                    dir.mkdirs();
                                }
                                URL url = new URL(DownloadUrl);
                                file = new File(dir, fileName);
                                if (file.exists()) {
                                    return file.toString();//файл уже есть, можно не качать.
                                }
                                Log.d("DownloadManager", "download begining");
                                Log.d("DownloadManager", "download url:" + url);
                                Log.d("DownloadManager", "downloaded file name:" + fileName);

                                URLConnection ucon = url.openConnection();
                                ucon.setConnectTimeout(5000);
                                ucon.setReadTimeout(20000);
                                InputStream is = ucon.getInputStream();
                                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                int nRead;
                                byte[] data = new byte[16384];

                                while ((nRead = is.read(data, 0, data.length)) != -1) {
                                    buffer.write(data, 0, nRead);
                                }
                                buffer.flush();

                                byte[] dataFile = buffer.toByteArray();
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(dataFile);
                                fos.flush();
                                fos.close();
                            } catch (Exception e) {
                                Log.d("DownloadManager", "Error: " + e);
                            }
                            return file.toString();
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);

                            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(s,
                                    MediaStore.Images.Thumbnails.MINI_KIND);
                            Bitmap bmPhoto= Bitmap.createScaledBitmap(thumbnail, 90, 90, true);

                            ivVideoPreview.setPadding(5,5,5,5);
                            //ivVideoPreview.setBackgroundColor(Color.parseColor("#D8D8DA"));
                            ivVideoPreview.setImageBitmap(getRoundedCornerBitmap(bmPhoto));

                            btnPlayVideo.setImageResource(R.drawable.video_wh);
                            final String path=s;
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
                        }
                    }.execute(commentsItem.getVideoId().toString(),commentsItem.getUniqueGUID().toString(),"3gp");
                }



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

        if (ApplicationSettings.getAuthorizationType() == AuthorizationType.NONE) {
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
