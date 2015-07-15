package zhexian.app.smartcall.image;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
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

    /**
     * 内存最大单张200KB
     */
    private static final int MAX_CACHED_IMAGE_SIZE = 200 * 1024;

    private static ZImage mZImage;
    private LruCache<String, Bitmap> mMemoryCache;

    private BaseApplication mApp;
    private Bitmap placeHolderBitmap;

    private ZImage(Activity activity) {
        mApp = (BaseApplication) activity.getApplication();
        placeHolderBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_default);
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        //申请总内存的1/8来创建图片内存池，在3G内存上，约为32MB
        int memorySize = activityManager.getMemoryClass() * 1024 * 1024 / 8;
        mMemoryCache = new LruCache<String, Bitmap>(memorySize) {
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

    private void loadEmpty(ImageView imageView) {
        imageView.setImageBitmap(placeHolderBitmap);
    }

    public void load(String url, ImageView imageView, int width, int height, boolean isCache, boolean canQueryHttp) {
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

        ImageTaskManager.getInstance().addTask(new LoadImageTask(mApp, imageView, url, width, height, isCache), ImageTaskManager.WorkType.LIFO);
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

    public void deleteFromLocal(String httpUrl) {
        mMemoryCache.remove(httpUrl);
        String cachedUrl = ZString.getFileCachedDir(httpUrl, mApp.getFilePath());
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
        if (bitmap != null && bitmap.getByteCount() < MAX_CACHED_IMAGE_SIZE)
            mMemoryCache.put(url, bitmap);
    }

}
