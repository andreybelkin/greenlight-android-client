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

/**
 * Created by Lenovo on 15.01.2016.
 */
public class FileDownloadTask extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... params) {
        File file=null;
        try {
            String DownloadUrl="http://192.168.1.33:8080/utils/getFile/"+params[0];
            String fileName= params[1]+"_"+params[0]+"."+params[2];
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();

            File dir = new File(root + "/gl");
            if(dir.exists()==false) {
                dir.mkdirs();
            }
            URL url = new URL(DownloadUrl);
            file = new File(dir, fileName);
            if (file.exists()){
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
}
