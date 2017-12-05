android获取视频关键帧，具体可查看http://blog.csdn.net/xiaxl/article/details/67637030

## ThumbnailUtils.createVideoThumbnail 获取到的帧不是视频第一个关键帧


最近项目中遇到一个获取视频首帧图片的问题。
网上一般给出的答案是用ThumbnailUtils.createVideoThumbnail(String filePath, int kind) 获取视频首帧。
我也是这么做的，但后来遇到一个ThumbnailUtils.createVideoThumbnail获取的视频帧并非视频首帧的bug。
经过对ThumbnailUtils.createVideoThumbnail方法的了解，得出以下结论。
## 结论：
**ThumbnailUtils.createVideoThumbnail(String filePath, int kind) 与 MediaMetadataRetriever.getFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) 获取到的为视频的最大关键帧；**
**MediaMetadataRetriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) 获取的为视频第一个关键帧。**

## 举例说明：
**视频第一个关键帧**
![Alt text](http://img.blog.csdn.net/20170328145720028?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGlheGw=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
**视频最大关键帧**
![Alt text](http://img.blog.csdn.net/20170328145750929?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGlheGw=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
**获取“最大关键帧”和“第一个关键帧”**
```java
// 获取最大关键帧
Bitmap bmp = ThumbnailUtils.createVideoThumbnail("/sdcard/0001.mp4", MediaStore.Images.Thumbnails.MINI_KIND);
mTextView01.setBackground(new BitmapDrawable(bmp));

// 获取第一个关键帧
MediaMetadataRetriever retriever = new MediaMetadataRetriever();
retriever.setDataSource("/sdcard/0001.mp4");
Bitmap bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
mTextView02.setBackground(new BitmapDrawable(bmp));
```
**案例运行效果图**
![Alt text](http://img.blog.csdn.net/20170328145811685?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGlheGw=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

## 了解ThumbnailUtils.createVideoThumbnail(String filePath, int kind)方法实现

**android.media.ThumbnailUtils**
```java
public static Bitmap createVideoThumbnail(String filePath, int kind) {
    Bitmap bitmap = null;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    try {
        retriever.setDataSource(filePath);
        bitmap = retriever.getFrameAtTime(-1);
    } catch (IllegalArgumentException ex) {
        // Assume this is a corrupt video file
    } catch (RuntimeException ex) {
        // Assume this is a corrupt video file.
    } finally {
        try {
            retriever.release();
        } catch (RuntimeException ex) {
            // Ignore failures while cleaning up.
        }
    }
	...
    return bitmap;
}
```
从以上代码可以看出createVideoThumbnail调用的是MediaMetadataRetriever.getFrameAtTime(-1)获取视频关键帧

**android.media.MediaMetadataRetriever**
```java
public Bitmap getFrameAtTime(long timeUs) {
    return getFrameAtTime(timeUs, OPTION_CLOSEST_SYNC);
}
```
+ OPTION_CLOSEST   在给定的时间，检索最近一个帧，这个帧不一定是关键帧。
+ OPTION_CLOSEST_SYNC   在给定的时间，检索最近一个关键帧。
+ OPTION_NEXT_SYNC   在给定时间之后，检索一个关键帧。
+ OPTION_PREVIOUS_SYNC   在给定时间之前，检索一个关键帧。

**android.media.MediaMetadataRetriever**
```java
public Bitmap getFrameAtTime(long timeUs, int option) {
    if (option < OPTION_PREVIOUS_SYNC ||
        option > OPTION_CLOSEST) {
        throw new IllegalArgumentException("Unsupported option: " + option);
    }
    return _getFrameAtTime(timeUs, option);
}
```
_getFrameAtTime为Native方法


```cpp
int64_t thumbNailTime;  
if (frameTimeUs < 0) { ／／如果传入的时间为负数  
    if (!trackMeta->findInt64(kKeyThumbnailTime, &thumbNailTime)|| thumbNailTime < 0) { ／／查看kKeyThumbnailTime是否存在  
        thumbNailTime = 0; ／／如不存在取第0帧  
    }  
    options.setSeekTo(thumbNailTime, mode);  
} else {  
    thumbNailTime = -1;  
    options.setSeekTo(frameTimeUs, mode);  
}  
```
frameTimeUs为我们传入的时间参数；
thumbNailTime为最大关键帧的时间参数；

参考：
http://blog.csdn.net/cloudwu007/article/details/18959567
http://www.codeweblog.com/camera%E5%BD%95%E5%88%B6%E8%A7%86%E9%A2%91%E7%9A%84%E7%BC%A9%E7%95%A5%E5%9B%BE%E8%8E%B7%E5%8F%96%E5%8E%9F%E7%90%86%E5%BF%83%E5%BE%97%E5%88%86%E4%BA%AB/

