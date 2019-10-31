package com.wyb.srp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片加载类
 * */
public class ImageLoader {
    //图片缓存
    LruCache<String, Bitmap> mImageCache;
    //线程池，线程数量为Cpu数量
    ExecutorService mExecutorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    //构造方法
    public ImageLoader(){
        initImageCache();
    }
    //初始化
    private void initImageCache()
    {
        //计算可使用的最大内存
        final int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        //取1/4的可用内存作为缓存
        final int cacheSize = maxMemory/4;

        mImageCache = new LruCache<String ,Bitmap>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight()/1024;
            }
        };
    }
    //显示图片
    public void displayImage(final  String url,final ImageView imageView)
    {
        imageView.setTag(url);
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = downloadImage(url);
                if(bitmap == null)
                {
                    return ;
                }
                if(imageView.getTag().equals(url))
                {
                    imageView.setImageBitmap(bitmap);
                }
                mImageCache.put(url,bitmap);
            }
        });
    }
    //下载图片
    public Bitmap downloadImage(String imageUrl)
    {
        Bitmap bitmap = null;
        try
        {
            URL url = new URL(imageUrl);
            final HttpURLConnection conn
                    = (HttpURLConnection) url.openConnection();
            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            conn.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bitmap;
    }
}
