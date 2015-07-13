package zhexian.app.smartcall.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

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
    private static final int MSG_IMAGE_CANCEL = 2;
    private BaseApplication baseApp;
    private String url;
    private int width;
    private int height;
    private boolean isCache;
    private ILoadImageCallBack iLoadImageCallBack;
    private Bitmap bitmap;
    private ImageView imageView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_IMAGE_LOAD_DONE) {
                if (bitmap != null)
                    iLoadImageCallBack.onDone(url, imageView, bitmap, isCache);
            } else if (msg.what == MSG_IMAGE_CANCEL) {
                imageView.setTag("");
            }
        }
    };

    public LoadImageTask(BaseApplication baseApp, ImageView imageView, String url, int width, int height, boolean isCache, ILoadImageCallBack iLoadImageCallBack) {
        this.baseApp = baseApp;
        this.url = url;
        this.width = width;
        this.height = height;
        this.isCache = isCache;
        this.iLoadImageCallBack = iLoadImageCallBack;
        this.imageView = imageView;
    }

    @Override
    public void run() {
        String cachedUrl = ZString.getFileCachedDir(url, baseApp.getFilePath());

        if (ZIO.isExist(cachedUrl))
            bitmap = Utils.getScaledBitMap(cachedUrl, width, height);

        if (bitmap == null && baseApp.isNetworkWifi()) {
            bitmap = ZHttp.getBitmap(url, width, height);

            if (bitmap != null && bitmap.getByteCount() > 0)
                ZImage.getInstance().saveToLocal(bitmap, url, cachedUrl);
        }

        handler.sendEmptyMessage(MSG_IMAGE_LOAD_DONE);
        ImageTaskManager.getInstance().Done(LOAD_IMAGE_TASK_ID);
    }

    @Override
    public int getTaskId() {
        return LOAD_IMAGE_TASK_ID;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void onCancel() {
        handler.sendEmptyMessage(MSG_IMAGE_CANCEL);
    }

    public interface ILoadImageCallBack {
        void onDone(String url, ImageView imageView, Bitmap bitmap, boolean isCache);
    }
}
