package zhexian.app.smartcall.lib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.HashMap;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.async.LoadImageTask;
import zhexian.app.smartcall.async.LoadImageTask.ILoadImageCallBack;
import zhexian.app.smartcall.async.SaveImageTask;
import zhexian.app.smartcall.async.ThreadPoolManager;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.tools.Utils;

public class ZImage implements ILoadImageCallBack {

    /**
     * 内存最大单张200KB
     */
    private static final int MAX_CACHED_IMAGE_SIZE = 200 * 1024;


    /**
     * 内存缓存最大容量20M
     */
    private static final int CACHED_MEMORY_SIZE = 20 * 1024 * 1024;
    private static ZImage mZImage;
    LruCache<String, Bitmap> mMemoryCache;
    private HashMap<String, ImageView> imageRequestList;

    private BaseApplication mApp;
    private Bitmap placeHolderBitmap;

    public ZImage(Activity activity) {
        mApp = (BaseApplication) activity.getApplication();
        placeHolderBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_default);
        imageRequestList = new HashMap<>();

        mMemoryCache = new LruCache<String, Bitmap>(CACHED_MEMORY_SIZE) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public static void Init(Activity activity) {
        if (mZImage == null)

            synchronized (ZImage.class) {
                if (mZImage == null)
                    mZImage = new ZImage(activity);
            }
    }

    public static ZImage getInstance() {
        if (mZImage == null) {
            Log.e("error", "ZIMAGE 需要被初始化，参考Init");
        }
        return mZImage;
    }

    public void loadEmpty(ImageView imageView) {
        imageView.setImageBitmap(placeHolderBitmap);
    }

    public void load(String url, ImageView imageView, int width, int height, boolean isCache, boolean canQueryHttp) {
        if (url.isEmpty()) {
            loadEmpty(imageView);
            return;
        }
        Bitmap bitmap = getFromMemoryCache(url);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        loadEmpty(imageView);
        String cachedUrl = ZString.getFileCachedDir(url, mApp.getFilePath());

        if (ZIO.isExist(cachedUrl)) {
            bitmap = Utils.getScaledBitMap(cachedUrl, width, height);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);

                if (isCache)
                    putToMemoryCache(url, bitmap);

                return;
            }
        }

        if (!canQueryHttp)
            return;

        if (imageRequestList.containsKey(url)) {
            ImageView oldImageView = imageRequestList.remove(url);
            oldImageView.setTag("");
            imageView.setTag(url);
            imageRequestList.put(url, imageView);
            return;
        }

        imageView.setTag(url);
        imageRequestList.put(url, imageView);
        ThreadPoolManager.getInstance().addTask(new LoadImageTask(mApp, url, width, height, isCache, this));
    }

    public Bitmap getBitMap(String url) {
        if (url.isEmpty()) {
            return placeHolderBitmap;
        }

        Bitmap bitmap = getFromMemoryCache(url);

        if (bitmap != null) {
            return bitmap;
        }

        String cachedUrl = ZString.getFileCachedDir(url, mApp.getFilePath());

        if (ZIO.isExist(cachedUrl)) {
            bitmap = Utils.getBitMap(cachedUrl);

            if (bitmap != null) {
                return bitmap;
            }
        }
        return placeHolderBitmap;
    }

    public void saveToLocal(String url, int width, int height) {
        ThreadPoolManager.getInstance().addTask(new SaveImageTask(mApp, url, width, height));
    }

    Bitmap getFromMemoryCache(String url) {
        return mMemoryCache.get(url);
    }

    void putToMemoryCache(String url, Bitmap bitmap) {
        if (bitmap != null && bitmap.getByteCount() < MAX_CACHED_IMAGE_SIZE)
            mMemoryCache.put(url, bitmap);
    }

    @Override
    public void onDone(String url, Bitmap bitmap, boolean isCache) {
        ImageView imageView = imageRequestList.remove(url);
        String originalUrl = (String) imageView.getTag();

        if (originalUrl != null && originalUrl.equals(url))
            imageView.setImageBitmap(bitmap);

        if (isCache && getFromMemoryCache(url) == null)
            putToMemoryCache(url, bitmap);

    }
}
