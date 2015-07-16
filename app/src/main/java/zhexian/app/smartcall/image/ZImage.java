package zhexian.app.smartcall.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.lib.ZIO;
import zhexian.app.smartcall.lib.ZString;
import zhexian.app.smartcall.tools.Utils;

public class ZImage {

    private static ZImage mZImage;
    private LruCache<String, Bitmap> mMemoryCache;
    private BaseApplication baseApp;
    private Bitmap placeHolderBitmap;
    private ZImage(Activity activity) {
        baseApp = (BaseApplication) activity.getApplication();
        placeHolderBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_default);

        mMemoryCache = new LruCache<String, Bitmap>(baseApp.getImageCachePoolSize()) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public static void Init(Activity activity) {
        if (mZImage == null)
            mZImage = new ZImage(activity);
    }

    public static ZImage getInstance() {
        if (mZImage == null) {
            Log.e("error", "ZIMAGE 需要被初始化，参考Init");
        }
        return mZImage;
    }

    private void loadEmpty(ImageView imageView) {
        imageView.setImageBitmap(placeHolderBitmap);
    }

    public void load(String url, ImageView imageView) {
        load(url, imageView, baseApp.getAvatarWidth(), baseApp.getAvatarWidth(), CacheType.DiskMemory, baseApp.isNetworkWifi());
    }

    public void load(String url, ImageView imageView, int width, int height, CacheType cacheType, boolean canQueryHttp) {
        if (url.isEmpty()) {
            loadEmpty(imageView);
            return;
        }
        Bitmap bitmap = getFromMemoryCache(url);
        imageView.setTag(url);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        loadEmpty(imageView);
        String cachedUrl = ZString.getFileCachedDir(url, baseApp.getFilePath());

        if (ZIO.isExist(cachedUrl)) {
            bitmap = Utils.getScaledBitMap(cachedUrl, width, height);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);

                if (cacheType == CacheType.DiskMemory)
                    putToMemoryCache(url, bitmap);

                return;
            }
        }

        if (!canQueryHttp)
            return;

        ImageTaskManager.getInstance().addTask(new LoadImageTask(baseApp, imageView, url, width, height, cacheType), ImageTaskManager.WorkType.LIFO);
    }

    public Bitmap getBitMap(String url) {
        if (url.isEmpty()) {
            return placeHolderBitmap;
        }

        Bitmap bitmap = getFromMemoryCache(url);

        if (bitmap != null) {
            return bitmap;
        }

        String cachedUrl = ZString.getFileCachedDir(url, baseApp.getFilePath());

        if (ZIO.isExist(cachedUrl)) {
            bitmap = Utils.getBitMap(cachedUrl);

            if (bitmap != null) {
                return bitmap;
            }
        }
        return placeHolderBitmap;
    }

    public void deleteFromLocal(String httpUrl) {
        mMemoryCache.remove(httpUrl);
        String cachedUrl = ZString.getFileCachedDir(httpUrl, baseApp.getFilePath());
        ZIO.deleteFile(cachedUrl);
        ContactSQLHelper.getInstance().deleteFilePath(httpUrl);
    }

    public void saveToLocal(Bitmap bitmap, String httpUrl, String cachedUrl) {
        ZIO.saveBitmapToCache(bitmap, cachedUrl);
        ContactSQLHelper.getInstance().addFilePath(httpUrl, cachedUrl);
    }

    public void reloadMemory() {
        ImageTaskManager.getInstance().addTask(new MemoryPackCacheTask(), ImageTaskManager.WorkType.LILO);
    }

    Bitmap getFromMemoryCache(String url) {
        return mMemoryCache.get(url);
    }

    void putToMemoryCache(String url, Bitmap bitmap) {
        if (bitmap != null && bitmap.getByteCount() > 0)
            mMemoryCache.put(url, bitmap);
    }

    /**
     * 缓存类型
     */
    public enum CacheType {

        /**
         * 不缓存
         */
        None,


        /**
         * 缓存到硬盘
         */
        Disk,

        /**
         * 缓存到硬盘和内存
         */
        DiskMemory
    }

}
