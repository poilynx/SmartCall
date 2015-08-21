package zhexian.app.smartcall.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.call.ContactSQLHelper;
import zhexian.app.smartcall.lib.DBHelper;
import zhexian.app.smartcall.lib.ZHttp;

/**
 * 图片加载任务
 */
public class LoadImageTask extends BaseImageAsyncTask {

    private static final int MSG_IMAGE_LOAD_DONE = 1;
    private BaseApplication baseApp;
    private ImageView mImageView;
    private String url;
    private int width;
    private int height;
    private ZImage.CacheType mCacheType;

    public LoadImageTask(BaseApplication baseApp, ImageView imageView, String url, int width, int height, ZImage.CacheType cacheType) {
        this.baseApp = baseApp;
        this.url = url;
        this.width = width;
        this.height = height;
        mImageView = imageView;
        mCacheType = cacheType;
    }

    @Override
    public void run() {
        Bitmap bitmap = DBHelper.cache().getBitmap(url, width, height);

        if (bitmap == null && baseApp.isNetworkWifi()) {
            bitmap = ZHttp.getBitmap(url, width, height);

            boolean isCacheToDisk = mCacheType == ZImage.CacheType.Disk || mCacheType == ZImage.CacheType.DiskMemory;
            if (isCacheToDisk && bitmap != null && bitmap.getByteCount() > 0) {
                DBHelper.cache().save(url, bitmap);
                ContactSQLHelper.getInstance().addFilePath(url, DBHelper.cache().trans2Local(url));
            }
        }

        if (bitmap != null) {
            new ImageDoneHandler(baseApp.getMainLooper(), mImageView, bitmap, url, mCacheType).sendEmptyMessage(MSG_IMAGE_LOAD_DONE);
        }
        ImageTaskManager.getInstance().Done(getTaskId());
    }

    @Override
    public int getTaskId() {
        return LOAD_IMAGE_TASK_ID;
    }

    @Override
    public String getUrl() {
        return url;
    }

    static class ImageDoneHandler extends Handler {
        WeakReference<ImageView> imageView;
        WeakReference<Bitmap> bitmap;
        WeakReference<String> url;
        ZImage.CacheType cacheType;


        ImageDoneHandler(Looper looper, ImageView _imageView, Bitmap _bitmap, String url, ZImage.CacheType cacheType) {
            super(looper);
            imageView = new WeakReference<>(_imageView);
            bitmap = new WeakReference<>(_bitmap);
            this.url = new WeakReference<>(url);
            this.cacheType = cacheType;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_IMAGE_LOAD_DONE)
                return;

            ImageView _imageView = imageView.get();
            Bitmap _bitmap = bitmap.get();
            String _url = url.get();
            if (_imageView == null || _bitmap == null)
                return;

            if (_url.equals(_imageView.getTag().toString())) {
                _imageView.setImageBitmap(_bitmap);

                if (cacheType == ZImage.CacheType.DiskMemory || cacheType == ZImage.CacheType.Memory)
                    ZImage.ready().putToMemoryCache(_url, _bitmap);
            }
        }
    }
}
