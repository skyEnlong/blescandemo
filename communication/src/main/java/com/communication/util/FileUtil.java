package com.communication.util;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by workEnlong on 2015/12/4.
 */

public class FileUtil {

    /* 获取SD卡目录 */
    private static final String SD_FILE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    private static final String ACCESSORY_PATH = SD_FILE_PATH + File.separator
            + "codoonsports" + File.separator + "accessory";



    public static String getDataFilePath(Context context) {
        File path;

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            path = new File(ACCESSORY_PATH + File.separator + "band_data");
        } else {
            String appPath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath();
            path = new File(appPath + File.separator + "accessory"
                    + File.separator + "band_data");
        }


        if (!path.exists()) {
            path.mkdirs();
        }

        return path.getAbsolutePath();
    }

    public static String getEphemerisPath(Context context) {
        File path;

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            path = new File(ACCESSORY_PATH + File.separator + "Ephemeris");
        } else {
            String appPath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath();
            path = new File(appPath + File.separator + "accessory"
                    + File.separator + "Ephemeris");
        }


        if (!path.exists()) {
            path.mkdirs();
        }

        return path.getAbsolutePath();
    }


    public static void saveAsFile(String desPath, String fileName, InputStream in) {
        File desDir = new File(desPath);
        if ( !desDir.exists()) {
            desDir.mkdirs();
        }

        File file = new File(desPath + File.separator + fileName);
        FileOutputStream out = null;
        try {

            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length = -1;
            while (-1 != (length = in.read(buffer))) {
                out.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (null != out) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    public static void saveAsFile( String fileName, byte[] data) {

        File file = new File(fileName);
        try {

            FileOutputStream e = new FileOutputStream( file);
            e.write(data);
            e.flush();
            e.close();
        } catch (FileNotFoundException var8) {
            var8.printStackTrace();
        } catch (IOException var9) {
            var9.printStackTrace();
        }
    }

    public static void deleteFile(String fileName){
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
        }
    }

    public static void renameFile(String path, String file_name){
        File f= new File(file_name);
        File path_copy = new File(path+ "_copy");
        if(!path_copy.exists()){
            path_copy.mkdirs();
        }

        File f_c = new File(path_copy.getPath() + File.separator + f.getName());
        if(f_c.exists()) f_c.delete();
        f.renameTo(f_c);
    }

    public static void saveAsFile(String desPath, String fileName, byte[] data) {
        File desDir = new File(desPath);
        if (desDir == null || !desDir.exists()) {
            desDir.mkdir();
        }

        File file = new File(desPath + File.separator + fileName);
        try {

            FileOutputStream e = new FileOutputStream(file);
            e.write(data);
            e.flush();
            e.close();
        } catch (FileNotFoundException var8) {
            var8.printStackTrace();
        } catch (IOException var9) {
            var9.printStackTrace();
        }
    }


    public static byte[] readFile(
            String fileName) throws IOException {
        File file = new File(fileName);
        if(!file.exists()) return null;

        int size = (int) file.length();
        byte byteArr[] = new byte[size];

        FileInputStream inputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                inputStream);
        bufferedInputStream.read(byteArr, 0, byteArr.length);
        bufferedInputStream.close();

        return byteArr;
    }

}
