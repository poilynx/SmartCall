package zhexian.app.smartcall.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.lib.ZHttp;
import zhexian.app.smartcall.lib.ZIO;
import zhexian.app.smartcall.lib.ZString;
import zhexian.app.smartcall.tools.Utils;

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
    private boolean mIsCache;

    public LoadImageTask(BaseApplication baseApp, ImageView imageView, String url, int width, int height, boolean isCache) {
        this.baseApp = baseApp;
        this.url = url;
        this.width = width;
        this.height = height;
        mImageView = imageView;
        mIsCache = isCache;
    }

    @Override
    public void run() {
        String cachedUrl = ZString.getFileCachedDir(url, baseApp.getFilePath());
        Bitmap bitmap = null;

        if (ZIO.isExist(cachedUrl))
            bitmap = Utils.getScaledBitMap(cachedUrl, width, height);

        if (bitmap == null && baseApp.isNetworkWifi()) {
            bitmap = ZHttp.getBitmap(url, width, height);

            if (mIsCache && bitmap != null && bitmap.getByteCount() > 0)
                ZImage.getInstance().saveToLocal(bitmap, url, cachedUrl);
        }

        if (bitmap != null) {
            new ImageDoneHandler(baseApp.getMainLooper(), mImageView, bitmap, url, mIsCache).sendEmptyMessage(MSG_IMAGE_LOAD_DONE);
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
        boolean isCache;


        ImageDoneHandler(Looper looper, ImageView _imageView, Bitmap _bitmap, String url, boolean isCache) {
            super(looper);
            imageView = new WeakReference<>(_imageView);
            bitmap = new WeakReference<>(_bitmap);
            this.url = new WeakReference<>(url);
            this.isCache = isCache;
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

                if (isCache)
                    ZImage.getInstance().putToMemoryCache(_url, _bitmap);
            }
        }
    }
}
