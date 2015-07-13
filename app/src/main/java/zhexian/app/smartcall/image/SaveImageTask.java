package zhexian.app.smartcall.image;

import android.graphics.Bitmap;

import zhexian.app.smartcall.base.BaseApplication;
import zhexian.app.smartcall.lib.ZHttp;
import zhexian.app.smartcall.lib.ZString;

/**
 * 图片下载功能
 */
public class SaveImageTask extends BaseImageAsyncTask {
    private BaseApplication baseApp;
    private String url;
    private int width;
    private int height;

    public SaveImageTask(BaseApplication baseApp, String url, int width, int height) {
        this.baseApp = baseApp;
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        String cachedUrl = ZString.getFileCachedDir(url, baseApp.getFilePath());

        if (baseApp.isNetworkWifi()) {
            Bitmap bitmap = ZHttp.getBitmap(url, width, height);

            if (bitmap != null && bitmap.getByteCount() > 0)
                ZImage.getInstance().saveToLocal(bitmap, url, cachedUrl);
        }

        ImageTaskManager.getInstance().Done(SAVE_IMAGE_TASK_ID);
    }

    @Override
    public int getTaskId() {
        return SAVE_IMAGE_TASK_ID;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
