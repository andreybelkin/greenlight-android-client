package com.globalgrupp.greenlight.greenlightclient.classes;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lenovo on 28.01.2016.
 */
public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;

    private Activity app = null;

    public TopExceptionHandler(Activity app) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.app = app;
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++)
        {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

// If the exception was thrown in a background thread inside
// AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++)
            {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

        try {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File (root + "/gl/logs");
            if(dir.exists()==false) {
                dir.mkdirs();
            }
//            FileOutputStream trace = app.openFileOutput(
//                    "stack.trace", Context.MODE_PRIVATE);
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            Date dt=new Date();
            File file = new File(dir, "greenlight"+df.format(dt));
            FileOutputStream trace = new FileOutputStream(file);
            trace.write(report.getBytes());
            trace.close();
        } catch(IOException ioe) {
// ...
        }

        defaultUEH.uncaughtException(t, e);
    }
}
