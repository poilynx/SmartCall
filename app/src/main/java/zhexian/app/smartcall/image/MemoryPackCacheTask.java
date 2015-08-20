package zhexian.app.smartcall.image;

import android.database.Cursor;

import zhexian.app.smartcall.call.ContactSQLHelper;

/**
 * 打包图片缓存类，用于在SaveImageTask更新完本地存储索引之后，再批量根据索引加载到内存中
 */
public class MemoryPackCacheTask extends BaseImageAsyncTask {
    private static final String TASK_URL = "zhexian.app.smartcall.image.MemoryPackCacheTask";

    @Override
    public int getTaskId() {
        return CACHE_PACK_IMAGE_TASK_ID;
    }

    @Override
    public String getUrl() {
        return TASK_URL;
    }

    @Override
    public void run() {
        Cursor cursor = ContactSQLHelper.getInstance().getDb(true).rawQuery("select httpPath,localPath from savedFile", null);

        while (cursor.moveToNext()) {
            String httpUrl = cursor.getString(0);
            if (ZImage.ready().getFromMemoryCache(httpUrl) == null)
                ImageTaskManager.getInstance().addTask(new MemoryCacheTask(httpUrl, cursor.getString(1)), ImageTaskManager.WorkType.LILO);
        }
        cursor.close();
        ImageTaskManager.getInstance().Done(getTaskId());
    }
}
