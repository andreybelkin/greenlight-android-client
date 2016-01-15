package com.globalgrupp.greenlight.greenlightclient.classes;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

/**
 * Created by Lenovo on 15.01.2016.
 */
public class FileDownloadTask extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... params) {
        File file=null;
        try {
            String DownloadUrl=params[0];
            String fileName= UUID.randomUUID().toString()+"."+params[1];
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();

            File dir = new File (root + "/gl");
            if(dir.exists()==false) {
                dir.mkdirs();
            }
            URL url = new URL(DownloadUrl); //you can write here any link
            file = new File(dir, fileName);

            long startTime = System.currentTimeMillis();
            Log.d("DownloadManager", "download begining");
            Log.d("DownloadManager", "download url:" + url);
            Log.d("DownloadManager", "downloaded file name:" + fileName);

            URLConnection ucon = url.openConnection();
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
}
