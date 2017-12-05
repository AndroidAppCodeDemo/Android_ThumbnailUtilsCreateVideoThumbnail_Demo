package com.example.scalephoto;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity {


    TextView mTextView01 = null;
    TextView mTextView02 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        mTextView01 = (TextView) findViewById(R.id.TextView01);
        mTextView02 = (TextView) findViewById(R.id.TextView02);
        //
        findViewById(R.id.button01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取最大关键帧
                Bitmap bmp = ThumbnailUtils.createVideoThumbnail("/sdcard/0001.mp4", MediaStore.Images.Thumbnails.MINI_KIND);
                mTextView01.setBackground(new BitmapDrawable(bmp));

            }
        });

        findViewById(R.id.button02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取第一个关键帧
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource("/sdcard/0001.mp4");
                //
                Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                mTextView02.setBackground(new BitmapDrawable(bmp));

            }
        });


        //-----------------------拷贝视频-------------------------
        Task.call(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return AssetsCopyUtils.copyFile(MainActivity.this, "0001.mp4", "/sdcard/0001.mp4");
            }
        }, Task.BACKGROUND_EXECUTOR).continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.getResult() == true) {
                    Toast.makeText(MainActivity.this, "拷贝视频到Sdcard成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "拷贝视频到Sdcard失败", Toast.LENGTH_SHORT).show();
                }

                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }
}