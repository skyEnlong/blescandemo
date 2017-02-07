package com.communication.util;

import android.content.Context;
import android.text.TextUtils;

import com.communication.data.CLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by enlong on 2017/1/9.
 */

public class UserCollection {

    private String path;
    private String user_id = "shoes";
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public UserCollection(Context mContext) {
        path = FileUtil.getEphemerisPath(mContext);
        dateFormat = new SimpleDateFormat("yyyyMMdd");
        timeFormat = new SimpleDateFormat("HH:mm:ss");
    }

    public synchronized void recordAction(String action) {

        if (TextUtils.isEmpty(action)) return;

        Date d = new Date(System.currentTimeMillis());
        String file_name = dateFormat.format(d) + ".txt";

        recordAction(action, true, file_name);
    }

    public synchronized void recordAction(String action, boolean isAlwaysPrint, String file_name) {
        if (!isAlwaysPrint && !CLog.isDebug) return;


        File file = new File(path + File.separator + user_id + "_" + file_name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Date d = new Date(System.currentTimeMillis());

        String content = android.os.Process.myPid() + "  " + timeFormat.format(d) + " " + action;

        try {
            FileWriter fw = new FileWriter(file, true);
//            content = Base64.encode(content.getBytes("utf-8"), 0, content.length());

            PrintWriter pw = new PrintWriter(fw);
            pw.println(content);
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
