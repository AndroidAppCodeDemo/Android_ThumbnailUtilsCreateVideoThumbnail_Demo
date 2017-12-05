package com.example.scalephoto;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by xueliangxia on 2017/3/28.
 */

public class AssetsCopyUtils {

    /**
     * Assets下文件复制到sdcard
     */
    public static boolean copyFile(Context context, String sourceFileName, String descFilePath) {
        if (context == null || TextUtils.isEmpty(sourceFileName) || TextUtils.isEmpty(descFilePath)) {
            return false;
        }
        // 如果文件存在，则删除文件
        File file = new File(descFilePath);
        if (file.exists()) {
            file.delete();
        }
        // 拷贝
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(sourceFileName);
            //String newFileName = Environment.getExternalStorageDirectory() + descFileName;
            out = new FileOutputStream(descFilePath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
