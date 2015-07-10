package zhexian.app.smartcall.async;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.lib.ZHttp;
import zhexian.app.smartcall.lib.ZIO;
import zhexian.app.smartcall.lib.ZString;

/**
 * 图片加载任务
 */
public class LoadImageTask implements Runnable {
    private static final int MSG_IMAGE_LOAD_DONE = 1;
    private BaseApplication baseApp;
    private String url;
    private int width;
    private int height;
    private boolean isCache;
    private ILoadImageCallBack iLoadImageCallBack;
    private Bitmap bitmap;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_IMAGE_LOAD_DONE) {
                if (bitmap != null)
                    iLoadImageCallBack.onDone(url, bitmap, isCache);
            }
        }
    };

    public LoadImageTask(BaseApplication baseApp, String url, int width, int height, boolean isCache, ILoadImageCallBack iLoadImageCallBack) {
        this.baseApp = baseApp;
        this.url = url;
        this.width = width;
        this.height = height;
        this.isCache = isCache;
        this.iLoadImageCallBack = iLoadImageCallBack;
    }

    @Override
    public void run() {
        String cachedUrl = ZString.getFileCachedDir(url, baseApp.getFilePath());

        if (!ZIO.isExist(cachedUrl) && baseApp.isNetworkWifi()) {
            bitmap = ZHttp.getBitmap(url, width, height);

            if (bitmap != null && bitmap.getByteCount() > 0) {
                ZIO.saveBitmapToCache(bitmap, cachedUrl);
                handler.sendEmptyMessage(MSG_IMAGE_LOAD_DONE);
            }
        }
        ThreadPoolManager.getInstance().Done();
    }

    public interface ILoadImageCallBack {
        void onDone(String url, Bitmap bitmap, boolean isCache);
    }
}
